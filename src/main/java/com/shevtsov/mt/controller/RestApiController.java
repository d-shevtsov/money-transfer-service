package com.shevtsov.mt.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.shevtsov.mt.HttpRequestHandler;
import com.shevtsov.mt.entities.BaseResponse;
import com.shevtsov.mt.entities.MoneyTransfer;
import com.shevtsov.mt.entities.datatransfer.BaseDTO;
import com.shevtsov.mt.entities.datatransfer.MoneyTransferDTO;
import com.shevtsov.mt.service.MoneyTransferService;
import com.shevtsov.mt.util.HttpMethod;
import com.shevtsov.mt.util.HttpStatus;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.shevtsov.mt.util.HttpMethod.GET;
import static com.shevtsov.mt.util.HttpMethod.POST;
import static com.shevtsov.mt.util.HttpUtils.httpMethod;
import static com.shevtsov.mt.util.HttpUtils.lastPathParam;
import static com.shevtsov.mt.util.JsonUtils.data;
import static com.shevtsov.mt.util.JsonUtils.json;

public class RestApiController {

    private static final Logger logger = LoggerFactory.getLogger(RestApiController.class);

    private static <T, R extends BaseDTO<T>> void handlePostRequest(HttpExchange exchange, Class<R> dtoClazz, Supplier<HttpRequestHandler<T>> handler) {
        Optional<HttpMethod> httpMethod = httpMethod(exchange);
        if (httpMethod.isPresent() && httpMethod.get() == POST) {
            T entity;
            try {
                R dataTransfer = data(exchange, dtoClazz);
                entity = dataTransfer.toEntity();
            } catch (RuntimeException | IOException e) {
                logger.error("Could not convert input data", e);
                inputDataError(exchange);
                return;
            }
            handleRequest(exchange, entity, handler.get());
        } else {
            methodNotSupported(exchange);
        }
    }

    private static <T> void handleGetRequest(HttpExchange exchange, Function<String, T> entityConverter, Supplier<HttpRequestHandler<T>> handler) {
        Optional<HttpMethod> httpMethod = httpMethod(exchange);
        if (httpMethod.isPresent() && httpMethod.get() == GET) {
            T entity;
            try {
                String lastPathParam = lastPathParam(exchange.getRequestURI().getPath());
                entity = entityConverter.apply(lastPathParam);
            } catch (RuntimeException e) {
                logger.error("Could not convert input data", e);
                inputDataError(exchange);
                return;
            }
            handleRequest(exchange, entity, handler.get());
        } else {
            methodNotSupported(exchange);
        }
    }

    private static <T> void handleRequest(HttpExchange exchange, T entity, HttpRequestHandler<T> handler) {
        BaseResponse<?> response;
        try {
            response = handler.handle(entity);
        } catch (RuntimeException e) {
            genericError(exchange);
            return;
        }

        sendResponse(exchange, response);
    }

    private static void inputDataError(HttpExchange exchange) {
        sendResponse(exchange, BaseResponse.of(HttpStatus.BAD_REQUEST));
    }

    private static void genericError(HttpExchange exchange) {
        sendResponse(exchange, BaseResponse.of(HttpStatus.SERVER_ERROR));
    }

    private static void methodNotSupported(HttpExchange exchange) {
        sendResponse(exchange, BaseResponse.of(HttpStatus.METHOD_NOT_ALLOWED));
    }

    private static void sendResponse(HttpExchange exchange, BaseResponse<?> response) {
        try {
            if (response.hasData()) {
                byte[] responseBody = json(response.getData()).getBytes();
                exchange.sendResponseHeaders(response.getHttpStatus().getCode(), responseBody.length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(responseBody);
                outputStream.flush();
            } else {
                exchange.sendResponseHeaders(response.getHttpStatus().getCode(), -1);
            }
            exchange.close();
        } catch (IOException e) {
            logger.warn("Unexpected IO exception occurs on sending response to the client", e);
        }
    }

    public static void configureRestApi(HttpServer server, MoneyTransferService moneyTransferService) {
        configureApi(server, moneyTransferService);
    }

    private static void configureApi(HttpServer server, MoneyTransferService moneyTransferService) {
        server.createContext("/api/balance",
                request -> handleGetRequest(request, Long::parseLong, () -> balanceRequestHandler(moneyTransferService)));
        server.createContext("/api/transfer",
                request -> handlePostRequest(request, MoneyTransferDTO.class, () -> transferRequestHandler(moneyTransferService)));
    }

    private static HttpRequestHandler<MoneyTransfer> transferRequestHandler(MoneyTransferService moneyTransferService) {
        BalanceController balanceController = new BalanceController(moneyTransferService);
        return balanceController::handleTransferRequest;
    }

    private static HttpRequestHandler<Long> balanceRequestHandler(MoneyTransferService moneyTransferService) {
        BalanceController balanceController = new BalanceController(moneyTransferService);
        return balanceController::handleBalanceRequest;
    }

    private RestApiController() {
    }

}
