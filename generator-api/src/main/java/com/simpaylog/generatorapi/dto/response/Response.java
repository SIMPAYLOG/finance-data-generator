package com.simpaylog.generatorapi.dto.response;

import com.simpaylog.generatorapi.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// TODO: success 메서드 httpStatus 매개변수 필요한지 체크
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

    public static ResponseEntity<Status> clientError(String message) {
        return ResponseEntity.badRequest().body(new Status("ERROR", message, HttpStatus.BAD_REQUEST.value()));
    }
}
