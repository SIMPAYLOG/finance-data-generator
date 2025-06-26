package com.simpaylog.generatorapi.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class SimulationStartRequestDto {
    private Long userCount;
    private LocalDateTime durationStart;
    private LocalDateTime durationEnd;
}
