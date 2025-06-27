package com.simpaylog.generatorapi.dto.response;

import com.simpaylog.generatorapi.dto.ErrorCode;
import com.simpaylog.generatorapi.dto.Status;

public record Response<T>(Status status, T result) {
    public static <T> Response<T> success(int httpStatus, T result) {
        return new Response<>(new Status("SUCCESS", null, httpStatus), result);
    }

    public static Response<Void> success(int httpStatus) {
        return new Response<>(new Status("SUCCESS", null, httpStatus), null);
    }

    public static Response<Void> error(ErrorCode errorCode) {
        return new Response<>(new Status(errorCode.name(), errorCode.getMessage(), errorCode.getStatus().value()), null);
    }
}
