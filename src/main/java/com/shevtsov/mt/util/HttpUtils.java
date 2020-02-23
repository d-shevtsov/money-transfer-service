package com.shevtsov.mt.util;

import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;

public class HttpUtils {

    public static Optional<HttpMethod> httpMethod(HttpExchange httpExchange) {
        return HttpMethod.of(httpExchange.getRequestMethod());
    }

    public static String lastPathParam(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private HttpUtils() {
    }

}
