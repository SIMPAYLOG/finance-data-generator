package com.simpaylog.generatorcore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PreferenceType {
    DEFAULT(0, "기본형"),
    CONSUMPTION_ORIENTED(1, "소비 지향형"),
    SAVING_ORIENTED(2, "저축 지향형"),
    UNPLANNED(3, "무계획형"),
    INVESTMENT_ORIENTED(4, "투자 지향형"),
    STABLE(5, "안정 추구형");

    private final int key;
    private final String name;

    public static PreferenceType fromKey(int key) {
        return switch (key) {
            case 1 -> CONSUMPTION_ORIENTED;
            case 2 -> SAVING_ORIENTED;
            case 3 -> UNPLANNED;
            case 4 -> INVESTMENT_ORIENTED;
            case 5 -> STABLE;
            default -> DEFAULT;
        };
    }
}