package com.simpaylog.generatorapi.dto.request;

import java.util.List;

public record ExportRequest(
        String sessionId,
        String format, // "csv" or "json"
        String durationStart,
        String durationEnd,
        List<String> columns,
        boolean isAggregated,
        boolean isMasked
) { }
