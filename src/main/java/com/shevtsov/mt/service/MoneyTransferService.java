package com.shevtsov.mt.service;

import java.math.BigDecimal;

public interface MoneyTransferService {

    void transfer(long balanceIdFrom, long balanceIdTo, BigDecimal amount);

    BigDecimal getAmount(long balanceId);

}
