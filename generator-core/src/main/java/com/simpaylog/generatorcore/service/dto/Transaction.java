package com.simpaylog.generatorcore.service.dto;

public record Transaction(
        String tradeName,
        int cost
) {}