package com.shevtsov.mt.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.shevtsov.mt.entities.Balance;

public class InMemoryMoneyTransferService implements MoneyTransferService {

    private static final String BALANCE_NOT_FOUND = "Balance with the id %s is not found.";

    // Modify to concurrent if write operations are required, otherwise HashMap is sufficient
    // for concurrent reads happens after map is initialized.
    private final Map<Long, Balance> balanceMap = new HashMap<>();

    public InMemoryMoneyTransferService(Map<Long, Balance> initialState) {
        balanceMap.putAll(initialState);
    }

    @Override
    public void transfer(long balanceIdFrom, long balanceIdTo, BigDecimal amount) {
        if (balanceIdFrom == balanceIdTo) {
            throw new IllegalArgumentException("Transfers between the same account are not supported.");
        }

        Balance balanceFrom = Objects.requireNonNull(balanceMap.get(balanceIdFrom), String.format(BALANCE_NOT_FOUND, balanceIdFrom));
        Balance balanceTo = Objects.requireNonNull(balanceMap.get(balanceIdTo), String.format(BALANCE_NOT_FOUND, balanceIdTo));

        balanceFrom.subtract(amount);
        balanceTo.add(amount);
    }

    @Override
    public BigDecimal getAmount(long balanceId) {
        Balance balance = Objects.requireNonNull(balanceMap.get(balanceId), String.format(BALANCE_NOT_FOUND, balanceId));
        return balance.getMoney();
    }

}
