package com.simpaylog.generatorcore.dto.response;

import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.enums.Gender;

public record UserInfoResponse(String name, Gender gender, Integer age, String preferenceId, String occupationName) {
    public static UserInfoResponse userToUserInfoResponse(User user, String preferenceName) {
        return new UserInfoResponse(
                user.getName(),
                user.getGender(),
                user.getAge(),
                preferenceName,
                user.getOccupationName()
        );
    }
}