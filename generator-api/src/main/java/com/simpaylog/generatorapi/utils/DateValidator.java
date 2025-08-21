package com.simpaylog.generatorapi.utils;

import com.simpaylog.generatorapi.exception.ApiException;
import com.simpaylog.generatorapi.exception.ErrorCode;

import java.time.LocalDate;

public class DateValidator {

    public static void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ApiException(ErrorCode.INVALID_DATE_SETTING, "날짜 범위(from, to)는 null일 수 없습니다.");
        }

        if (from.isAfter(to)) {
            throw new ApiException(ErrorCode.INVALID_DATE_RANGE, "시작일(from)은 종료일(to)보다 이후일 수 없습니다.");
        }
    }
}
