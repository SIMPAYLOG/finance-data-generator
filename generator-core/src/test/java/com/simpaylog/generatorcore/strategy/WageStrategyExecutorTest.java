package com.simpaylog.generatorcore.strategy;

import com.simpaylog.generatorcore.TestConfig;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.UserBehaviorProfile;
import com.simpaylog.generatorcore.enums.Gender;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.enums.WageType;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class WageStrategyExecutorTest extends TestConfig {
    private final WageStrategyExecutor executor = new WageStrategyExecutor();

    @Test
    void DAILY_타입인_사람이_2025년_7월에_받는_급여일수는_23일이다() {
        // Given
        int day = 23;
        LocalDate baseDate = LocalDate.of(2025, 7, 1);
        BigDecimal wage = BigDecimal.valueOf(2300000);
        UserBehaviorProfile profileFixture = profileFixture(wage, WageType.DAILY);
        User userFixture = userFixture(profileFixture);
        // When
        List<LocalDate> result = executor.decidePayout(userFixture, baseDate);
        // Then
        assertEquals(day, result.size());
    }

    @Test
    void WEEKLY_타입인_사람이_2025년_8월에_받는_월급일은_1_8_14_22_29일이다() {
        // Given
        LocalDate baseDate = LocalDate.of(2025, 8, 1);
        BigDecimal wage = BigDecimal.valueOf(2300000);
        UserBehaviorProfile profileFixture = profileFixture(wage, WageType.WEEKLY);
        User userFixture = userFixture(profileFixture);
        // When
        List<LocalDate> result = executor.decidePayout(userFixture, baseDate);
        // Then
        assertEquals(5, result.size());
        assertThat(result).containsAll(List.of(
                        LocalDate.of(2025, 8, 1),
                        LocalDate.of(2025, 8, 8),
                        LocalDate.of(2025, 8, 14),
                        LocalDate.of(2025, 8, 22),
                        LocalDate.of(2025, 8, 29)
                )
        );
    }

    @Test
    void BIWEEKLY_타입인_사람이_2025년_8월에_받는_월급일은_1_14_29일이다() {
        // Given
        LocalDate baseDate = LocalDate.of(2025, 8, 1);
        BigDecimal wage = BigDecimal.valueOf(2300000);
        UserBehaviorProfile profileFixture = profileFixture(wage, WageType.BI_WEEKLY);
        User userFixture = userFixture(profileFixture);
        // When
        List<LocalDate> result = executor.decidePayout(userFixture, baseDate);
        // Then
        assertEquals(3, result.size());
        assertThat(result).containsAll(List.of(
                        LocalDate.of(2025, 8, 1),
                        LocalDate.of(2025, 8, 14),
                        LocalDate.of(2025, 8, 29)
                )
        );
    }
    @Test
    void REGULAR_타입인_사람이_2025년_10월에_받는_월급일은_25일이_아닌_24일이다() {
        // Given
        LocalDate baseDate = LocalDate.of(2025, 10, 1);
        BigDecimal wage = BigDecimal.valueOf(2300000);
        UserBehaviorProfile profileFixture = profileFixture(wage, WageType.REGULAR);
        User userFixture = userFixture(profileFixture);
        // When
        List<LocalDate> result = executor.decidePayout(userFixture, baseDate);
        // Then
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2025, 10, 24), result.getFirst());
    }

    @RepeatedTest(10)
    void RANDOM_타입인_사람이_2025년_10월에_받는_급여일은_6일이하이다() {
        // Given
        LocalDate baseDate = LocalDate.of(2025, 10, 1);
        BigDecimal wage = BigDecimal.valueOf(2300000);
        UserBehaviorProfile profileFixture = profileFixture(wage, WageType.RANDOM);
        User userFixture = userFixture(profileFixture);
        // When
        List<LocalDate> result = executor.decidePayout(userFixture, baseDate);
        // Then
        assertThat(result.size()).isBetween(0, 6);
    }

    public static UserBehaviorProfile profileFixture(BigDecimal wage, WageType wageType) {
        return UserBehaviorProfile.of(PreferenceType.DEFAULT, wageType, wage, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static User userFixture(UserBehaviorProfile profile) {
        return User.of("test", profile, 1, 20, Gender.M, 1, "TEST-OCCUPATION", 1, List.of());
    }
}