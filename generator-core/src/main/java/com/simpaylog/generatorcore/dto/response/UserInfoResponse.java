package com.simpaylog.generatorcore.dto.response;

import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.enums.Gender;

public record UserInfoResponse(
        String name,
        Gender gender,
        Integer age,
        String preferenceId,  // TODO: 필드 이름 변경(프론트도 같이)
        String occupationName) {

    public static UserInfoResponse userToUserInfoResponse(User user) {
        return new UserInfoResponse(
                user.getName(),
                user.getGender(),
                user.getAge(),
                user.getUserBehaviorProfile().getPreferenceType().getName(),
                user.getOccupationName()
        );
    }
}