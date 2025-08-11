package com.simpaylog.generatorcore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;

import java.time.LocalDate;

public record TransactionRequestEvent(
        TransactionUserDto transactionUserDto,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate from,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate to
) {
}
