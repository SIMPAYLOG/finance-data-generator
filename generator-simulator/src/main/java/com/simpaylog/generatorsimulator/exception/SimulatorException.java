package com.simpaylog.generatorsimulator.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SimulatorException extends RuntimeException {
    private String message;
}
