package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.TestConfig;
import com.simpaylog.generatorcore.dto.analyze.AgeStat;
import com.simpaylog.generatorcore.dto.analyze.OccupationNameStat;
import com.simpaylog.generatorcore.dto.response.UserAnalyzeResultResponse;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.UserBehaviorProfile;
import com.simpaylog.generatorcore.enums.Gender;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class UserAnalyzeTest extends TestConfig {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.saveAll(
                List.of(
                        User.of("김민준", UserBehaviorProfile.of(new BigDecimal("5500000"), 1, WageType.DAILY, 25), 8, 30, Gender.M, new BigDecimal("15000000"), 1, 1, "소프트웨어 엔지니어", 1),
                        User.of("김민주", UserBehaviorProfile.of(new BigDecimal("5500000"), 1, WageType.DAILY, 25), 8, 20, Gender.F, new BigDecimal("15000000"), 1, 2, "소프트웨어 엔지니어", 1),
                        User.of("김철수", UserBehaviorProfile.of(new BigDecimal("5500000"), 2, WageType.DAILY, 25), 8, 40, Gender.M, new BigDecimal("15000000"), 1, 2, "소프트웨어 엔지니어", 1),
                        User.of("최민수", UserBehaviorProfile.of(new BigDecimal("5500000"), 1, WageType.DAILY, 25), 8, 50, Gender.M, new BigDecimal("15000000"), 1, 1, "소프트웨어 엔지니어", 1),
                        User.of("민철승", UserBehaviorProfile.of(new BigDecimal("5500000"), 2, WageType.DAILY, 25), 8, 60, Gender.M, new BigDecimal("15000000"), 1, 2, "소프트웨어 엔지니어", 1),
                        User.of("한지혜", UserBehaviorProfile.of(new BigDecimal("5500000"), 2, WageType.DAILY, 25), 8, 70, Gender.F, new BigDecimal("15000000"), 1, 1, "소프트웨어 엔지니어", 1),
                        User.of("금민", UserBehaviorProfile.of(new BigDecimal("5500000"), 1, WageType.DAILY, 25), 8, 20, Gender.M, new BigDecimal("15000000"), 1, 2, "소프트웨어 엔지니어", 1),
                        User.of("김지영", UserBehaviorProfile.of(new BigDecimal("5500000"), 2, WageType.DAILY, 25), 8, 10, Gender.F, new BigDecimal("15000000"), 1, 1, "소프트웨어 엔지니어", 1),
                        User.of("강혜지", UserBehaviorProfile.of(new BigDecimal("5500000"), 2, WageType.DAILY, 25), 8, 50, Gender.F, new BigDecimal("15000000"), 1, 1, "소프트웨어 엔지니어", 1),
                        User.of("이지민", UserBehaviorProfile.of(new BigDecimal("5500000"), 2, WageType.DAILY, 25), 8, 70, Gender.F, new BigDecimal("15000000"), 1, 2, "소프트웨어 엔지니어", 1)
                )
        );
    }

    @Test
    @DisplayName("생성된 데이터에서 광업 분야에서 일하는 사람의 수와 20대 사람의 수, 남자의 수를 분석한다.")
    void analyzeUsersIntegrationTest() {
        //given
        String targetOccupation = "B.광업";
        int targetAgeGroup = 20;
        int expectedTotalUser = 10;
        int expectedTargetOccupationCnt = 5;
        int targetAgeGroupCnt = 2;
        int expectedMaleCnt = 5;

        // when
        UserAnalyzeResultResponse response = userService.analyzeUsers();
        Long realAgeGroupCnt = response.ageDistribution().stream()
                .filter(stats -> stats.ageGroup() == targetAgeGroup)
                .map(AgeStat::count).findFirst().get();
        Long realOccupationCnt = response.occupationDistribution().stream()
                .filter(stats -> targetOccupation.equals(stats.occupationCategory()))
                .map(OccupationNameStat::count).findFirst().get();

        // then
        assertThat(userRepository.count()).isEqualTo(expectedTotalUser);
        assertThat(realOccupationCnt).isEqualTo(expectedTargetOccupationCnt);
        assertThat(realAgeGroupCnt).isEqualTo(targetAgeGroupCnt);
        assertThat(response.genderDistribution().male()).isEqualTo(expectedMaleCnt);
    }
}