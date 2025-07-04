package com.simpaylog.generatorapi.dto.request;

import com.simpaylog.generatorapi.validator.NumericOrMix;
import com.simpaylog.generatorcore.dto.UserGenerationCondition;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record UserGenerationConditionRequestDto(
        @Min(1)
        @Max(10000)
        int userCount,
        @NumericOrMix(message = "소비성향은 숫자 또는 MIX여야 합니다.")
        String preferenceId,
        @Pattern(regexp = "10|20|30|40|50|60|70|MIX", message = "나이대는 10,20,30,40,50,60,70 또는 MIX여야 합니다.")
        String ageGroup,
        @Pattern(regexp = "MALE|FEMALE|MIX", message = "성별은 MALE, FEMALE 또는 MIX여야 합니다.")
        String gender,
        @NumericOrMix(message = "직업군은 숫자 또는 MIX여야 합니다.")
        String occupationCode
) {
    public static UserGenerationCondition toCore(UserGenerationConditionRequestDto dto, int id) {
        return new UserGenerationCondition(
                id,
                dto.userCount(),
                dto.preferenceId(),
                dto.ageGroup(),
                dto.gender(),
                dto.occupationCode()
        );
    }
}
