package com.shevtsov.mt.entities;

import com.shevtsov.mt.util.HttpStatus;

public class BaseResponse<T> {

    private T data;
    private HttpStatus httpStatus;

    public BaseResponse(T data, HttpStatus httpStatus) {
        this.data = data;
        this.httpStatus = httpStatus;
    }

    public BaseResponse(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public static <T> BaseResponse<T> of(T data) {
        return new BaseResponse<>(data, HttpStatus.OK);
    }

    public static <T> BaseResponse<T> of(T data, HttpStatus httpStatus) {
        return new BaseResponse<>(data, httpStatus);
    }

    public static BaseResponse of(HttpStatus httpStatus) {
        return new BaseResponse<>(httpStatus);
    }

    public T getData() {
        return data;
    }

    public boolean hasData() {
        return data != null;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
