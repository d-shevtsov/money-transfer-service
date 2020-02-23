package com.shevtsov.mt.entities;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class MoneyTransfer {

    private final long balanceIdFrom;
    private final long balanceIdTo;
    private final BigDecimal amount;

    public MoneyTransfer(Long balanceIdFrom, Long balanceIdTo, BigDecimal amount) {
        this.balanceIdFrom = requireNonNull(balanceIdFrom);
        this.balanceIdTo = requireNonNull(balanceIdTo);
        this.amount = requireNonNull(amount);
    }

    public long getBalanceIdFrom() {
        return balanceIdFrom;
    }

    public long getBalanceIdTo() {
        return balanceIdTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

}
