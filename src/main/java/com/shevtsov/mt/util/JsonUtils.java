package com.shevtsov.mt.util;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

public class JsonUtils {

    public static <T> T data(HttpExchange httpExchange, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = httpExchange.getRequestBody();
        return mapper.readValue(inputStream, clazz);
    }

    public static <T> String json(T data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(data);
    }

    private JsonUtils() {
    }

}
