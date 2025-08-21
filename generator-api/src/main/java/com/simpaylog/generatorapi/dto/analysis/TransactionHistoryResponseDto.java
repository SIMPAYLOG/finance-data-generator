package com.simpaylog.generatorapi.dto.analysis;

import java.util.List;

public record TransactionHistoryResponseDto(
        List<TransactionHistoryDataDto> transactions,
        List<String> nextSearchAfter
) {
}
