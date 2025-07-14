package com.simpaylog.generatorcore.dto;

import com.simpaylog.generatorcore.enums.Gender;

public record UserInfoDto(String name, Gender gender, Integer age, Integer preferenceId, String occupationName) {
}