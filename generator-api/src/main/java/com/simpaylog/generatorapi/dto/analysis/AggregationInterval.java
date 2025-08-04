package com.simpaylog.generatorapi.dto.analysis;

import com.simpaylog.generatorapi.exception.ApiException;
import com.simpaylog.generatorapi.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AggregationInterval {
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String calendarInterval;

    public static AggregationInterval from(String value) {
        return Arrays.stream(values())
                .filter(i -> i.getCalendarInterval().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REQUEST, "Invalid interval: " + value));
    }
}
