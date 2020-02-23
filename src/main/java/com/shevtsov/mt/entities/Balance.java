package com.shevtsov.mt.entities;

import java.math.BigDecimal;
import java.util.Objects;

public class Balance {

    private static final String WITHDRAW_ERROR = "Cannot withdraw the amount which exceeds the current balance.";

    private final long balanceId;
    private volatile BigDecimal money;

    public Balance(Long balanceId, BigDecimal money) {
        this.balanceId = Objects.requireNonNull(balanceId);
        this.money = money;
    }

    public void add(BigDecimal amount) {
        synchronized (this) {
            money = money.add(amount);
        }
    }

    public void subtract(BigDecimal amount) {
        // avoid sync if amount is higher than money available
        if (money.compareTo(amount) < 0) {
            throw new IllegalArgumentException(WITHDRAW_ERROR);
        }
        synchronized (this) {
            if (money.compareTo(amount) < 0) {
                throw new IllegalArgumentException(WITHDRAW_ERROR);
            }
            money = money.subtract(amount);
        }
    }

    public long getBalanceId() {
        return balanceId;
    }

    public BigDecimal getMoney() {
        return BigDecimal.valueOf(money.doubleValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Balance balance = (Balance) o;
        return balanceId == balance.balanceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(balanceId);
    }

}
