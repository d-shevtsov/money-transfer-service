package com.shevtsov.mt.util;

public enum HttpStatus {

    OK(200),

    BAD_REQUEST(400),
    METHOD_NOT_ALLOWED(405),

    SERVER_ERROR(500);

    private final int code;

    HttpStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
