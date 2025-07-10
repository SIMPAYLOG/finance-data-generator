package com.simpaylog.generatorsimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PreferenceType {
    DEFAULT("기본형"),
    CONSUMPTION_ORIENTED("소비 지향형"),
    SAVING_ORIENTED("저축 지향형"),
    UNPLANNED("무계획형"),
    INVESTMENT_ORIENTED("투자 지향형"),
    STABLE("안정 추구형");

    private final String name;
}