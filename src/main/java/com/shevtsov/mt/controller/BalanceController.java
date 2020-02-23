package com.shevtsov.mt.controller;

import com.shevtsov.mt.entities.BaseResponse;
import com.shevtsov.mt.entities.MoneyTransfer;
import com.shevtsov.mt.service.MoneyTransferService;
import com.shevtsov.mt.util.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalanceController {

    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);

    private final MoneyTransferService service;

    public BalanceController(MoneyTransferService service) {
        this.service = service;
    }

    public BaseResponse handleTransferRequest(MoneyTransfer transfer) {
        try {
            service.transfer(transfer.getBalanceIdFrom(), transfer.getBalanceIdTo(), transfer.getAmount());
        } catch (RuntimeException e) {
            logger.warn(e.getMessage(), e);
            return BaseResponse.of(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return BaseResponse.of(HttpStatus.OK);
    }

    public BaseResponse handleBalanceRequest(long balanceId) {
        try {
            return BaseResponse.of(service.getAmount(balanceId));
        } catch (RuntimeException e) {
            logger.warn(e.getMessage(), e);
            return BaseResponse.of(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
