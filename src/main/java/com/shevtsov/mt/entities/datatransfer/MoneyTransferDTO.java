package com.shevtsov.mt.entities.datatransfer;

import java.math.BigDecimal;

import com.shevtsov.mt.entities.MoneyTransfer;

@SuppressWarnings("unused")
public class MoneyTransferDTO implements BaseDTO<MoneyTransfer> {

    private Long from;
    private Long to;
    private BigDecimal amount;

    public MoneyTransferDTO() {
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public MoneyTransfer toEntity() {
        return new MoneyTransfer(from, to, amount);
    }

}
