package com.simpaylog.generatorapi.dto.response;

import com.simpaylog.generatorapi.dto.enums.EventType;

public record TransactionResultResponse(String message, EventType eventType) {}