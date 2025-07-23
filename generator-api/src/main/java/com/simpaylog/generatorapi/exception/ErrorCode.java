package com.simpaylog.generatorapi.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Request is invalid"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server error"),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식니다."),
    INVALID_DATE_SETTING(HttpStatus.BAD_REQUEST, "시작 날짜 또는 종료 날짜가 설정되지 않았습니다.");

    final private HttpStatus status;
    final private String message;
}
