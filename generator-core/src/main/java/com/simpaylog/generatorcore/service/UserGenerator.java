package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.cache.DetailOccupationLocalCache;
import com.simpaylog.generatorcore.cache.IncomeLevelLocalCache;
import com.simpaylog.generatorcore.cache.OccupationLocalCache;
import com.simpaylog.generatorcore.cache.dto.DetailOccupationInfo.*;
import com.simpaylog.generatorcore.cache.dto.IncomeLevelInfo.*;
import com.simpaylog.generatorcore.cache.dto.OccupationInfos.*;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.UserBehaviorProfile;
import com.simpaylog.generatorcore.enums.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.simpaylog.generatorcore.utils.MultinomialAllocator.normalize;
import static com.simpaylog.generatorcore.utils.MultinomialAllocator.sampleMultinomial;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserGenerator {
    private final IncomeLevelLocalCache incomeLevelLocalCache;
    private final OccupationLocalCache occupationLocalCache;
    private final DetailOccupationLocalCache detailOccupationLocalCache;
    private final Random random = new Random();

    public List<User> generateUserPool(int totalUserCnt) {
        List<User> userPool = new ArrayList<>();
        double[] ratios = occupationLocalCache.getRatios();
        double[] normalizeRatio = normalize(ratios);
        int[] numByOccupation = sampleMultinomial(normalizeRatio, totalUserCnt);
        log.info("직업당 요청된 풀 수 : {}", Arrays.toString(numByOccupation));
        for (int code = 0; code < numByOccupation.length; code++) {
            List<User> groupUser = generateUserPoolByOccupation(code + 1, numByOccupation[code]);
            userPool.addAll(groupUser);
        }
        return userPool;
    }

    private List<User> generateUserPoolByOccupation(int code, int totalUserCnt) {
        List<User> userPool = new ArrayList<>();
        Occupation occupation = occupationLocalCache.get(code);
        double[] ageGroupRatio = occupation.ageGroupInfo()
                .stream()
                .mapToDouble(AgeGroupInfo::ratio)
                .toArray();
        double[] normalizeRatio = normalize(ageGroupRatio);
        int[] totalCntByAge = sampleMultinomial(normalizeRatio, totalUserCnt);

        for (int age = 0; age < totalCntByAge.length; age++) {
            for (int num = 0; num < totalCntByAge[age]; num++) {
                userPool.add(generateUser(occupation, code, age));
            }
        }
        return userPool;
    }

    private User generateUser(Occupation occupation, int code, int age) {
        int[] dominantDeciles = occupation.ageGroupInfo().get(age).dominantDeciles(); // 연령대별 비율
        String occupationCode = occupationLocalCache.get(code).occupationCategory().substring(0, 1); // 직업 코드
        int decile = random.nextInt(dominantDeciles[1] - dominantDeciles[0] + 1) + dominantDeciles[0]; // 소득 분위
        Gender gender = Gender.values()[random.nextInt(2)]; // 성별
        BigDecimal incomeValue = BigDecimal.valueOf(occupation.decileDistribution()[decile - 1] * occupation.averageMonthlyWage()); // 월임금
        Job jobInfo = getRandomJob(occupationCode, decile);
        AssetRange assetRange = incomeLevelLocalCache.get(decile).assetRange();
        int asset = (random.nextInt(assetRange.min(), assetRange.max()) + 1) /10 * 10;

        UserBehaviorProfile profile = UserBehaviorProfile.of(incomeValue, 1, jobInfo.wageType(), 5);
        return User.of(profile, decile, (age + 1) * 10, gender, BigDecimal.valueOf(asset), decile, code, jobInfo.jobTitle());
    }

    private Job getRandomJob(String occupationCode, int decile) {
        SubOccupation subOccupation = detailOccupationLocalCache.getSubOccupationsByCodeAndDecile(occupationCode, decile); // 세부 직업 목록
        return subOccupation.jobs().get(random.nextInt(0, subOccupation.jobs().size()));
    }
}
