package com.simpaylog.generatorcore.dto;

import com.simpaylog.generatorcore.enums.Gender;

public record UserSimpleTypeInfo(String name, Gender gender, Integer age, String preferenceId, String occupationName) {
}