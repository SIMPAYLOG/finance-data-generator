package com.simpaylog.generatorcore.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoreException extends RuntimeException {
    private String message;
}
