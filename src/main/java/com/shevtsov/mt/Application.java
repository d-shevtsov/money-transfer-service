package com.shevtsov.mt;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;

import com.shevtsov.mt.controller.RestApiController;
import com.shevtsov.mt.entities.Balance;
import com.shevtsov.mt.service.InMemoryMoneyTransferService;
import com.shevtsov.mt.service.MoneyTransferService;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toMap;

/**
 * Api accepts balance id of the balance on behalf of the user as one of the parameter(s) of the methods.
 * Therefore, it is assumed to be used in a secure way by internal services and is not publicity available.
 * This assumption leads to the design of the api in which we can provide information to the client about errors, like:
 * 1. Cannot withdraw the amount which exceeds the current balance.
 * 2. Balance with the id 'id' is not found.
 *
 * <p> If this API should be publicity available, another security considerations should be taken into account.
 */
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static final int SERVER_PORT = 8000;
    private static final long BALANCES_COUNT = 1_000_000;
    private static final BigDecimal DEFAULT_BALANCE_MONEY = BigDecimal.valueOf(200_000);

    public static void main(String[] args) {
        try {
            configureHttpServerAndStart();
            logger.info("Server started successfully");
        } catch (IOException e) {
            logger.error("Http Server could not be started", e);
        }
    }

    static void configureHttpServerAndStart() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);

        // Load tests which were run with one client machine does not show increasing of performance by increasing number of threads.
        // Probably should be changed after load tests with several client machines.
        int nThreads = Runtime.getRuntime().availableProcessors();
        Executor executor = Executors.newFixedThreadPool(nThreads);
        httpServer.setExecutor(executor);

        MoneyTransferService moneyTransferService = new InMemoryMoneyTransferService(loadBalances());
        RestApiController.configureRestApi(httpServer, moneyTransferService);
        httpServer.start();
    }

    private static Map<Long, Balance> loadBalances() {
        return LongStream.range(0, BALANCES_COUNT)
                .mapToObj(i -> new Balance(i, DEFAULT_BALANCE_MONEY))
                .collect(toMap(Balance::getBalanceId, v -> v));
    }

}
