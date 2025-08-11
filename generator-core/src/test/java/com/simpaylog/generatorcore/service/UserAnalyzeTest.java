package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.TestConfig;
import com.simpaylog.generatorcore.dto.analyze.AgeStat;
import com.simpaylog.generatorcore.dto.analyze.OccupationNameStat;
import com.simpaylog.generatorcore.dto.response.UserAnalyzeResultResponse;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.UserBehaviorProfile;
import com.simpaylog.generatorcore.enums.Gender;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.repository.UserRepository;
import com.simpaylog.generatorcore.repository.redis.RedisSessionRepository;
import com.simpaylog.generatorcore.session.SimulationSession;
import com.simpaylog.generatorcore.utils.AccountFactory;
import com.simpaylog.generatorcore.utils.SavingRateCalculator;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Transactional
public class UserAnalyzeTest extends TestConfig {
    @Autowired
    private UserService userService;
    @MockitoBean
    private RedisSessionRepository redisSessionRepository;
    @Autowired
    private UserRepository userRepository;
    private final AccountFactory accountFactory = new AccountFactory();
    private final String sessionId = "TEST-sessionId";

    @BeforeEach
    void setup() {
        userRepository.saveAll(
                List.of(
                        createUser("test-name1", 30, Gender.M, 1, PreferenceType.CONSUMPTION_ORIENTED),
                        createUser("test-name2", 20, Gender.F, 1, PreferenceType.SAVING_ORIENTED),
                        createUser("test-name3", 40, Gender.M, 1, PreferenceType.SAVING_ORIENTED),
                        createUser("test-name4", 50, Gender.M, 1, PreferenceType.CONSUMPTION_ORIENTED),
                        createUser("test-name5", 60, Gender.M, 1, PreferenceType.SAVING_ORIENTED),
                        createUser("test-name6", 70, Gender.F, 2, PreferenceType.SAVING_ORIENTED),
                        createUser("test-name7", 20, Gender.M, 2, PreferenceType.CONSUMPTION_ORIENTED),
                        createUser("test-name8", 10, Gender.F, 2, PreferenceType.SAVING_ORIENTED),
                        createUser("test-name9", 50, Gender.F, 2, PreferenceType.SAVING_ORIENTED),
                        createUser("test-name10", 70, Gender.F, 2, PreferenceType.SAVING_ORIENTED)
                )
        );
    }

    @Test
    @DisplayName("생성된 데이터에서 광업 분야에서 일하는 사람의 수와 20대 사람의 수, 남자의 수를 분석한다.")
    void analyzeUsersIntegrationTest() {
        //given
        String targetOccupation = "광업";
        int targetAgeGroup = 20;
        int expectedTotalUser = 10;
        int expectedTargetOccupationCnt = 5;
        int targetAgeGroupCnt = 2;
        int expectedMaleCnt = 5;
        when(redisSessionRepository.find(eq(sessionId))).thenReturn(Optional.of(new SimulationSession(sessionId, LocalDateTime.now())));
        // when
        UserAnalyzeResultResponse response = userService.analyzeUsers(sessionId);
        Long realAgeGroupCnt = response.ageDistribution().stream()
                .filter(stats -> stats.ageGroup() == targetAgeGroup)
                .map(AgeStat::count).findFirst().get();

        Long realOccupationCnt = response.occupationDistribution().stream()
                .filter(stats -> targetOccupation.equals(stats.occupationCategory()))
                .map(OccupationNameStat::count)
                .findFirst().get();

        System.out.println(realAgeGroupCnt + " " + realOccupationCnt);
        // then
        assertThat(userRepository.count()).isEqualTo(expectedTotalUser);
        assertThat(realOccupationCnt).isEqualTo(expectedTargetOccupationCnt);
        assertThat(realAgeGroupCnt).isEqualTo(targetAgeGroupCnt);
        assertThat(response.genderDistribution().male()).isEqualTo(expectedMaleCnt);
    }

    private User createUser(
            String name,
            Integer age,
            Gender gender,
            int occupationCode,
            PreferenceType preferenceType
    ) {
        UserBehaviorProfile profile = UserBehaviorProfile.of(preferenceType, WageType.DAILY, 25, getIncomeValue(), getAssetValue(), SavingRateCalculator.calculateSavingRate(8, age, preferenceType));
        User mockUser = User.of(name, profile, 8, age, gender, occupationCode, "TEST-OCCUPATION", 1, accountFactory.generateAccountsFor(getIncomeValue(), getAssetValue(), age, 8, preferenceType));
        mockUser.setSessionId("TEST-sessionId");
        return mockUser;
    }

    private BigDecimal getIncomeValue() {
        return new BigDecimal("5500000");
    }

    private BigDecimal getAssetValue() {
        return new BigDecimal("15000000");
    }
}