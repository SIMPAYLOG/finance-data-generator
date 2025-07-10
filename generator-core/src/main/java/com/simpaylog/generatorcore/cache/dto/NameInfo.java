package com.simpaylog.generatorcore.cache.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NameInfo {
    @JsonProperty("Full Name")
    private String fullName;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("Age")
    private int age;

    @JsonProperty("Age Group")
    private int ageGroup;
}
