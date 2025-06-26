package com.simpaylog.generatorapi.dto.response;

import com.simpaylog.generatorapi.dto.ErrorCode;
import com.simpaylog.generatorapi.dto.Status;
import org.springframework.http.HttpStatus;

public record CommonResponse<T>(Status status, T result) {
    public static <T> CommonResponse<T> success(int httpStatus, T result) {
        return new CommonResponse<>(new Status("SUCCESS", null, httpStatus), result);
    }

    public static CommonResponse<Void> success(int httpStatus) {
        return new CommonResponse<>(new Status("SUCCESS", null, httpStatus), null);
    }

    public static CommonResponse<Void> error(ErrorCode errorCode) {
        return new CommonResponse<>(new Status(errorCode.name(), errorCode.getMessage(), errorCode.getStatus().value()), null);
    }
}
