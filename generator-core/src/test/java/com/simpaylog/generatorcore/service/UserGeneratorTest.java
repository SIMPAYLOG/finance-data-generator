package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.TestConfig;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.enums.Gender;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.dto.UserGenerationCondition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserGeneratorTest extends TestConfig {
    @Autowired
    UserGenerator userGenerator;

    @Test
    void 주어진_조건이_전부_MIX일때_골고루_저장한다() {
        // Given
        int id = 1;
        int userCount = 10;
        UserGenerationCondition mockCondition = new UserGenerationCondition(id, userCount, "MIX", "MIX", "MIX", "MIX");
        // When
        List<User> result = userGenerator.generateUserPool(mockCondition);
        // Then(한 종류 이상인지 체크)
        assertEquals(userCount, result.size());
        assertThat(result.stream().map(user -> user.getUserBehaviorProfile().getPreferenceType()).distinct().count()).isGreaterThan(1);
        assertThat(result.stream().map(User::getAge).distinct().count()).isGreaterThan(1);
        assertThat(result.stream().map(User::getGender).distinct().count()).isGreaterThan(1);
        assertThat(result.stream().map(User::getOccupationCode).distinct().count()).isGreaterThan(1);
        result.forEach(user -> System.out.println(toString(user)));

    }

    @Test
    void 특정_연령대_성별이_주어졌을때_연령대와_성별을_제외하고_골고루_저장한다() {
        // Given
        int id = 1;
        int userCount = 10;
        String ageGroup = "30";
        String gender = "FEMALE";
        UserGenerationCondition mockCondition = new UserGenerationCondition(id, userCount, "MIX", ageGroup, gender, "MIX");
        // When
        List<User> result = userGenerator.generateUserPool(mockCondition);
        // Then
        assertEquals(userCount, result.size());
        assertThat(result.stream().map(user -> user.getUserBehaviorProfile().getPreferenceType()).distinct().count()).isGreaterThan(1);
        assertThat(result).allMatch(user -> user.getConditionId() == 1);
        assertThat(result).allMatch(user -> user.getAge() == 30);
        assertThat(result).allMatch(user -> user.getGender() == Gender.F);
        assertThat(result.stream().map(User::getOccupationCode).distinct().count()).isGreaterThan(1);

        result.forEach(user -> System.out.println(toString(user)));
    }

    @Test
    void 특정_연령대_성별_직업군_소비성향이_주어졌을때_연령대와_성별을_제외하고_골고루_저장한다() {
        // Given
        int id = 1;
        int userCount = 10;
        String preferenceId = "3";
        String ageGroup = "30";
        String gender = "MALE";
        String occupationCode = "14";
        UserGenerationCondition mockCondition = new UserGenerationCondition(id, userCount, preferenceId, ageGroup, gender, occupationCode);
        // When
        List<User> result = userGenerator.generateUserPool(mockCondition);
        // Then
        assertEquals(userCount, result.size());
        assertThat(result).allMatch(user -> user.getConditionId() == 1);
        assertThat(result).allMatch(user -> user.getUserBehaviorProfile().getPreferenceType().getKey() == 3);
        assertThat(result).allMatch(user -> user.getAge() == 30);
        assertThat(result).allMatch(user -> user.getGender() == Gender.M);
        assertThat(result).allMatch(user -> user.getOccupationCode() == Integer.parseInt(occupationCode));

        result.forEach(user -> System.out.println(toString(user)));
    }

    @Test
    void 존재하지_않는_직업코드를_넣으면_에러반환() {
        // Given
        int id = 1;
        int userCount = 10;
        String preferenceId = "3";
        String ageGroup = "30";
        String gender = "MALE";
        String occupationCode = "999";
        UserGenerationCondition mockCondition = new UserGenerationCondition(id, userCount, preferenceId, ageGroup, gender, occupationCode);
        // When
        assertThatThrownBy(() -> userGenerator.generateUserPool(mockCondition))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("존재하지 않는 직업 코드");
    }

    @Test
    void 존재하지_않는_성향ID를_넣으면_기본형으로_대체() {
        // Given
        int id = 1;
        int userCount = 10;
        String preferenceId = "10";
        String ageGroup = "30";
        String gender = "MALE";
        String occupationCode = "10";
        UserGenerationCondition mockCondition = new UserGenerationCondition(id, userCount, preferenceId, ageGroup, gender, occupationCode);
        // When
        List<User> result = userGenerator.generateUserPool(mockCondition);
        assertTrue(result.getFirst().getUserBehaviorProfile().getPreferenceType() == PreferenceType.DEFAULT);
    }

    public String toString(User user) {
        int age = user.getAge();
        PreferenceType preferenceType = user.getUserBehaviorProfile().getPreferenceType();
        String gender = user.getGender().name();
        int occupationCode = user.getOccupationCode();
        return String.format("연령대: %d, 소비성향: %s, 성별: %s 직업코드: %d", age, preferenceType, gender, occupationCode);
    }

}