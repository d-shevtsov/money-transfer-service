package com.shevtsov.mt.util;

import java.util.Arrays;
import java.util.Optional;

public enum HttpMethod {
    GET,
    POST;

    public static Optional<HttpMethod> of(String httpMethod) {
        return Arrays.stream(HttpMethod.values()).filter(method -> method.name().equals(httpMethod)).findAny();
    }

}
