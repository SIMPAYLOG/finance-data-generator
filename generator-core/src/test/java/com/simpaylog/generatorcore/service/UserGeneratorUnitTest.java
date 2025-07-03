package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.TestConfig;
import com.simpaylog.generatorcore.cache.OccupationLocalCache;
import com.simpaylog.generatorcore.cache.dto.OccupationInfos;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.simpaylog.generatorcore.utils.MultinomialAllocator.normalize;
import static com.simpaylog.generatorcore.utils.MultinomialAllocator.sampleMultinomial;
import static org.junit.jupiter.api.Assertions.*;

class UserGeneratorUnitTest extends TestConfig {

    @Autowired
    OccupationLocalCache occupationLocalCache;

    @RepeatedTest(10)
    void 비율이_주어졌을_때_비율만큼_무작위로_사용자를_배치한다() {
        // Given
        int totalCnt = 1000;
        double[] ratios = {0.3, 0.1, 16.6, 0.7, 0.5, 7.6, 14.0, 5.5, 9.1, 3.4, 2.9, 2.7, 5.7, 5.2, 3.5, 6.8, 10.0, 1.8, 3.6};
        double[] normalizeRatio = normalize(ratios);
        // When
        int[] val = sampleMultinomial(normalizeRatio, totalCnt);

        // Then
        assertEquals(Arrays.stream(val).sum(), totalCnt);
        System.out.printf("현재 배율: %s%n", Arrays.toString(ratios));
        for (int i = 0; i < ratios.length; i++) {
            System.out.printf("그룹%2d: %2d명%n", i + 1, val[i]);
        }
    }


    @ParameterizedTest
    @MethodSource("numberProvider")
    void 직업코드가_주어지고_직업사용자풀이_정해졌을때_연령별_비율만큼_무작위로_사용자를_배치한다(int code) {
        // Given
        int totalCnt = 97;
        OccupationInfos.Occupation occupation = occupationLocalCache.get(code);
        double[] ageGroupRatio = occupation.ageGroupInfo()
                .stream()
                .mapToDouble(OccupationInfos.AgeGroupInfo::ratio)
                .toArray();
        double[] normalizeRatio = normalize(ageGroupRatio);
        // When
        int[] val = sampleMultinomial(normalizeRatio, totalCnt);

        // Then
        assertEquals(Arrays.stream(val).sum(), totalCnt);
        System.out.println("현재 그룹의 비율: " + Arrays.toString(ageGroupRatio));
        System.out.println("현재 그룹의 가중치: " + Arrays.toString(val));
        for (int i = 0; i < ageGroupRatio.length; i++) {
            System.out.printf("%2d대: %2d명%n", (i + 1) * 10, val[i]);
        }
    }

    @Test
    void 특정_직업에_할당된_유저의_성별_소득분위_임금_랜덤생성() {
        // Given
        int code = 2;
        int[] totalCntByAge = {0, 10, 12, 37, 25, 13, 0};
        char[] gender = {'M', 'F'};
        OccupationInfos.Occupation occupation = occupationLocalCache.get(code);
        List<User> result = new ArrayList<>();

        // When & Then
        for (int age = 0; age < totalCntByAge.length; age++) {
            for (int num = 0; num < totalCntByAge[age]; num++) {

                int[] dominantDeciles = occupation.ageGroupInfo().get(age).dominantDeciles();
                Random random = new Random();
                int decile = random.nextInt(dominantDeciles[1] - dominantDeciles[0] + 1) + dominantDeciles[0];
                int genderIdx = random.nextInt(2);
                BigDecimal balance = BigDecimal.valueOf(occupation.decileDistribution()[decile - 1] * occupation.averageMonthlyWage());

                result.add(new User((age + 1) * 10, gender[genderIdx], decile, balance, code));
                System.out.println(result.get(result.size() - 1));
            }
        }
    }

    private static IntStream numberProvider() {
        return IntStream.rangeClosed(1, 18);
    }

    private static class User {
        int age;
        char gender;
        int bracket;
        BigDecimal balance;
        int occupation_code;

        public User(int age, char gender, int bracket, BigDecimal balance, int occupation_code) {
            this.age = age;
            this.gender = gender;
            this.bracket = bracket;
            this.balance = balance;
            this.occupation_code = occupation_code;
        }

        @Override
        public String toString() {
            return "User{" +
                    "age=" + age +
                    ", gender=" + gender +
                    ", bracket=" + bracket +
                    ", balance=" + balance +
                    ", occupation_code=" + occupation_code +
                    '}';
        }
    }
}