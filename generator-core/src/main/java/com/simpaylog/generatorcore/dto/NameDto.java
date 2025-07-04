package com.simpaylog.generatorcore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NameDto {
    @JsonProperty("Full Name")
    private String fullName;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("Age")
    private int age;

    @JsonProperty("Age Group")
    private int ageGroup;

    public String getFullName() { return fullName; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public int getAgeGroup() { return ageGroup; }
}
