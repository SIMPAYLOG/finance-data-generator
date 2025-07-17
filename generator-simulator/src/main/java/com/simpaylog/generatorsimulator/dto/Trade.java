package com.simpaylog.generatorsimulator.dto;

import java.math.BigDecimal;

public record Trade(
        String tradeName,
        BigDecimal cost
) {}