package com.shevtsov.mt.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.shevtsov.mt.entities.Balance;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InMemoryMoneyTransferServiceTest {

    private MoneyTransferService moneyTransferService;

    private void configureMoneyTransferService() {
        Map<Long, Balance> balanceMap = new HashMap<>();
        balanceMap.put(0L, new Balance(0L, BigDecimal.valueOf(1_000_000.0)));
        balanceMap.put(1L, new Balance(1L, BigDecimal.valueOf(1_200_000.0)));
        moneyTransferService = new InMemoryMoneyTransferService(balanceMap);
    }

    // Concurrency

    @Test
    public void transferConcurrentModificationsAreCorrect() throws InterruptedException {
        configureMoneyTransferService();
        CountDownLatch latch = new CountDownLatch(16);
        BigDecimal amountBalance1To2 = BigDecimal.valueOf(1.0 / 10);
        BigDecimal amountBalance2To1 = BigDecimal.valueOf(2.0 / 10);
        for (int i = 0; i < 8; i++) {
            Thread modifier1 = new Thread(new Modifier(0L, 1L, amountBalance1To2, moneyTransferService, latch));
            Thread modifier2 = new Thread(new Modifier(1L, 0L, amountBalance2To1, moneyTransferService, latch));
            modifier1.start();
            modifier2.start();
        }

        latch.await();
        assertEquals(BigDecimal.valueOf(1_800_000.0), moneyTransferService.getAmount(0L));
        assertEquals(BigDecimal.valueOf(400_000.0), moneyTransferService.getAmount(1L));
    }

    @Test
    public void transferConcurrentDoesNotReachStateBelowZero() throws InterruptedException {
        configureMoneyTransferService();
        CountDownLatch latch = new CountDownLatch(24);
        BigDecimal amountBalance1To2 = BigDecimal.valueOf(1.0 / 10);
        for (int i = 0; i < 12; i++) {
            Thread modifier1 = new Thread(new Modifier(0L, 1L, amountBalance1To2, moneyTransferService, latch));
            Thread modifier2 = new Thread(new Modifier(0L, 1L, amountBalance1To2, moneyTransferService, latch));
            modifier1.start();
            modifier2.start();
        }

        latch.await();
        assertEquals(BigDecimal.valueOf(0.0), moneyTransferService.getAmount(0L));
        assertEquals(BigDecimal.valueOf(2_200_000.0), moneyTransferService.getAmount(1L));
    }

    // Correctness

    @Test
    public void transferBetweenAccountsIsCorrect() {
        Map<Long, Balance> balanceMap = new HashMap<>();
        Balance balance1 = new Balance(0L, BigDecimal.valueOf(100.0));
        Balance balance2 = new Balance(1L, BigDecimal.valueOf(200.0));
        balanceMap.put(balance1.getBalanceId(), balance1);
        balanceMap.put(balance2.getBalanceId(), balance2);
        moneyTransferService = new InMemoryMoneyTransferService(balanceMap);

        moneyTransferService.transfer(0L, 1L, BigDecimal.valueOf(50.0));

        assertEquals(BigDecimal.valueOf(50.0), balance1.getMoney());
        assertEquals(BigDecimal.valueOf(250.0), balance2.getMoney());
        assertEquals(BigDecimal.valueOf(50.0), moneyTransferService.getAmount(0L));
        assertEquals(BigDecimal.valueOf(250.0), moneyTransferService.getAmount(1L));
    }

    @Test
    public void chainOfTransfersBetweenAccountsAreCorrect() {
        Map<Long, Balance> balanceMap = new HashMap<>();
        Balance balance1 = new Balance(0L, BigDecimal.valueOf(100.0));
        Balance balance2 = new Balance(1L, BigDecimal.valueOf(200.0));
        Balance balance3 = new Balance(2L, BigDecimal.valueOf(300.0));
        balanceMap.put(balance1.getBalanceId(), balance1);
        balanceMap.put(balance2.getBalanceId(), balance2);
        balanceMap.put(balance3.getBalanceId(), balance3);
        moneyTransferService = new InMemoryMoneyTransferService(balanceMap);

        for (long i = 0; i < 3; i++) {
            long indexTo = i == 2 ? 0 : i + 1;
            moneyTransferService.transfer(i, indexTo, BigDecimal.valueOf(i * 100));
        }

        assertEquals(BigDecimal.valueOf(300.0), balance1.getMoney());
        assertEquals(BigDecimal.valueOf(100.0), balance2.getMoney());
        assertEquals(BigDecimal.valueOf(200.0), balance3.getMoney());
        assertEquals(BigDecimal.valueOf(300.0), moneyTransferService.getAmount(0L));
        assertEquals(BigDecimal.valueOf(100.0), moneyTransferService.getAmount(1L));
        assertEquals(BigDecimal.valueOf(200.0), moneyTransferService.getAmount(2L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void transferIsNotAllowedForAmountGreaterThanCurrentBalance() {
        Map<Long, Balance> balanceMap = new HashMap<>();
        Balance balance1 = new Balance(0L, BigDecimal.valueOf(100.0));
        Balance balance2 = new Balance(1L, BigDecimal.valueOf(200.0));
        balanceMap.put(balance1.getBalanceId(), balance1);
        balanceMap.put(balance2.getBalanceId(), balance2);
        moneyTransferService = new InMemoryMoneyTransferService(balanceMap);

        moneyTransferService.transfer(0L, 1L, BigDecimal.valueOf(120.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void transferIsNotAllowedWithinOneBalance() {
        Map<Long, Balance> balanceMap = new HashMap<>();
        Balance balance1 = new Balance(0L, BigDecimal.valueOf(100.0));
        balanceMap.put(balance1.getBalanceId(), balance1);
        moneyTransferService = new InMemoryMoneyTransferService(balanceMap);

        moneyTransferService.transfer(0L, 0L, BigDecimal.valueOf(20.0));
    }

    private static final class Modifier implements Runnable {

        private static final int OPERATIONS_COUNT = 1_000_000;

        private final Long from;
        private final Long to;
        private final BigDecimal amount;
        private final MoneyTransferService moneyTransferService;
        private final CountDownLatch latch;

        public Modifier(Long from, Long to, BigDecimal amount, MoneyTransferService moneyTransferService, CountDownLatch latch) {
            this.from = from;
            this.to = to;
            this.amount = amount;
            this.moneyTransferService = moneyTransferService;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < OPERATIONS_COUNT; i++) {
                    try {
                        moneyTransferService.transfer(from, to, amount);
                    } catch (RuntimeException e) {
                        // IGNORE
                    }
                }
                latch.countDown();
            } catch (Exception e) {
                latch.countDown();
            }
        }

    }

}