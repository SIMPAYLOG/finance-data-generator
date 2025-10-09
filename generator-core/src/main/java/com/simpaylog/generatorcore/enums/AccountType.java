package com.simpaylog.generatorcore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountType {
    CHECKING("입출금 통장"),
    SAVINGS("저축 통장");

    private final String name;
}
