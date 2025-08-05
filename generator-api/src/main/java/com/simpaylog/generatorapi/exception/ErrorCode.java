package com.simpaylog.generatorapi.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Request is invalid"),
    SESSION_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "sessionId not found"),
    NO_USERS_FOUND(HttpStatus.NO_CONTENT, "해당 session에 유저가 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server error"),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식입니다."),
    INVALID_DATE_SETTING(HttpStatus.BAD_REQUEST, "시작 날짜 또는 종료 날짜가 설정되지 않았습니다."),
    FILE_WRITE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다"),
    INVALID_EXPORT_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 데이터 요청 형식입니다."),
    ELASTICSEARCH_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Elasticsearch 서버 통신 중 오류가 발생했습니다.");


    final private HttpStatus status;
    final private String message;
}
