package com.simpaylog.generatorapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Status {
    String code;
    String message;
    int httpStatus;
}
