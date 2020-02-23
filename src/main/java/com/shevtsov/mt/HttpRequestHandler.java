package com.shevtsov.mt;

import com.shevtsov.mt.entities.BaseResponse;

public interface HttpRequestHandler<T> {

    BaseResponse handle(T entity);

}
