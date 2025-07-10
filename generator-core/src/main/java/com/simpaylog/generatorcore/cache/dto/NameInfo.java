package com.simpaylog.generatorcore.cache.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NameInfo(
        @JsonProperty("Full Name")
        String fullName,

        @JsonProperty("Gender")
        String gender,

        @JsonProperty("Age")
        int age,

        @JsonProperty("Age Group")
        int ageGroup
) {
}
