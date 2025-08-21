package com.simpaylog.generatorapi.dto.request;

import java.time.LocalDate;
import java.util.List;

public record TransactionHistoryRequest(
        String sessionId,
        LocalDate durationStart,
        LocalDate durationEnd,
        Integer userId,
        List<String> searchAfter
) {
}
