package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.cache.DetailOccupationLocalCache;
import com.simpaylog.generatorcore.cache.IncomeLevelLocalCache;
import com.simpaylog.generatorcore.cache.OccupationLocalCache;
import com.simpaylog.generatorcore.cache.PreferenceLocalCache;
import com.simpaylog.generatorcore.cache.dto.DetailOccupationInfo.Job;
import com.simpaylog.generatorcore.cache.dto.DetailOccupationInfo.SubOccupation;
import com.simpaylog.generatorcore.cache.dto.IncomeLevelInfo.AssetRange;
import com.simpaylog.generatorcore.cache.dto.OccupationInfos.AgeGroupInfo;
import com.simpaylog.generatorcore.cache.dto.OccupationInfos.Occupation;
import com.simpaylog.generatorcore.dto.UserGenerationCondition;
import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.UserBehaviorProfile;
import com.simpaylog.generatorcore.enums.Gender;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.utils.AccountFactory;
import com.simpaylog.generatorcore.utils.MoneyUtil;
import com.simpaylog.generatorcore.utils.NameUtil;
import com.simpaylog.generatorcore.utils.SavingRateCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final PreferenceLocalCache preferenceLocalCache;
    private final Random random = new Random();
    private final AccountFactory accountFactory = new AccountFactory();
    private final NameUtil nameUtil;

    public List<User> generateUserPool(UserGenerationCondition condition) {
        List<User> userPool = new ArrayList<>();
        double[] ratios = setRatioByCondition(condition.occupationCode());
        double[] normalizeRatio = normalize(ratios);
        int[] numByOccupation = sampleMultinomial(normalizeRatio, condition.userCount());

        for (int code = 0; code < numByOccupation.length; code++) {
            if (numByOccupation[code] == 0) continue;
            List<User> groupUser = generateUserPoolByOccupation(condition, code + 1, numByOccupation[code]);
            userPool.addAll(groupUser);
        }
        return userPool;
    }

    // 직업카테고리로 인원 나눈 부분
    private List<User> generateUserPoolByOccupation(UserGenerationCondition condition, int occupationCode, int totalUserCnt) {
        List<User> userPool = new ArrayList<>();
        Occupation occupation = occupationLocalCache.get(occupationCode);
        double[] ageGroupRatio = setAgeGroupByCondition(occupation, condition.ageGroup());
        double[] normalizeRatio = normalize(ageGroupRatio);
        int[] totalCntByAge = sampleMultinomial(normalizeRatio, totalUserCnt);

        for (int age = 0; age < totalCntByAge.length; age++) {
            for (int num = 0; num < totalCntByAge[age]; num++) {
                userPool.add(generateUser(occupation, condition, occupationCode, age));
            }
        }
        return userPool;
    }

    private User generateUser(Occupation occupation, UserGenerationCondition condition, int code, int age) {
        int[] dominantDeciles = occupation.ageGroupInfo().get(age).dominantDeciles(); // 연령대별 비율
        String occupationCode = occupationLocalCache.get(code).occupationCategory().substring(0, 1); // 직업 코드
        int decile = random.nextInt(dominantDeciles[1] - dominantDeciles[0] + 1) + dominantDeciles[0]; // 소득 분위
        Gender gender = setGenderByCondition(condition.gender());
        String name = (gender == Gender.M) ?
                nameUtil.getRandomName('M', (age + 1) * 10)
                : nameUtil.getRandomName('F', (age + 1) * 10);
        PreferenceType preferenceType = PreferenceType.fromKey(setPreferenceIdByCondition(condition.preferenceId()));
        BigDecimal incomeValue = MoneyUtil.roundTo10(BigDecimal.valueOf(occupation.decileDistribution()[decile - 1] * occupation.averageMonthlyWage())); // 월급여
        Job jobInfo = getRandomJob(occupationCode, decile);
        AssetRange assetRange = incomeLevelLocalCache.get(decile).assetRange();
        BigDecimal assetValue = MoneyUtil.roundTo10(BigDecimal.valueOf((random.nextInt(assetRange.min(), assetRange.max()) + 1) / 10 * 10));
        int autoTransferDayOfMonth = random.nextInt(28) + 1; // 공과금
        BigDecimal savingRate = SavingRateCalculator.calculateSavingRate(decile, age, preferenceType);
        List<Account> accounts = accountFactory.generateAccountsFor(incomeValue, assetValue, age, decile, preferenceType);
        UserBehaviorProfile profile = UserBehaviorProfile.of(preferenceType, jobInfo.wageType(), autoTransferDayOfMonth, incomeValue, assetValue, savingRate);
        return User.of(name, profile, decile, (age + 1) * 10, gender, code, jobInfo.jobTitle(), condition.id(), accounts);
    }

    private Job getRandomJob(String occupationCode, int decile) {
        SubOccupation subOccupation = detailOccupationLocalCache.getSubOccupationsByCodeAndDecile(occupationCode, decile); // 세부 직업 목록
        return subOccupation.jobs().get(random.nextInt(0, subOccupation.jobs().size()));
    }

    // 직업 배율 설정
    private double[] setRatioByCondition(String occupationCode) {
        if ("MIX".equalsIgnoreCase(occupationCode)) {
            return occupationLocalCache.getRatios();
        }
        try {
            int occupationSize = occupationLocalCache.getRatios().length;
            int code = Integer.parseInt(occupationCode) - 1;
            if (0 > code || code > occupationSize)
                throw new CoreException(String.format("존재하지 않는 직업 코드: %s", occupationCode));
            double[] result = new double[occupationSize];
            result[code] = 100.0;
            return result;
        } catch (NumberFormatException e) {
            throw new CoreException(String.format("존재하지 않는 직업 코드: %s", occupationCode));
        }
    }

    // 나이 배율 설정
    private double[] setAgeGroupByCondition(Occupation occupation, String ageGroup) {
        double[] ageGroupRatio = occupation.ageGroupInfo()
                .stream()
                .mapToDouble(AgeGroupInfo::ratio)
                .toArray();

        if ("MIX".equalsIgnoreCase(ageGroup)) {
            return ageGroupRatio;
        }
        try {
            int ageGroupSize = ageGroupRatio.length;
            int age = (Integer.parseInt(ageGroup) / 10) - 1;
            if (0 > age || age > ageGroupSize)
                throw new CoreException(String.format("존재하지 않는 직업 코드: %s", ageGroup));
            double[] result = new double[ageGroupSize];
            result[age] = 100.0;
            return result;
        } catch (NumberFormatException e) {
            throw new CoreException(String.format("존재하지 않는 연령대: %s대", ageGroup));
        }
    }

    // 성별 설정
    private Gender setGenderByCondition(String gender) {
        if ("MIX".equalsIgnoreCase(gender)) {
            return Gender.values()[random.nextInt(2)];
        }
        if ("MALE".equalsIgnoreCase(gender)) return Gender.M;
        else return Gender.F;
    }

    // 성향 설정
    private int setPreferenceIdByCondition(String preferenceId) {
        int preferenceCnt = preferenceLocalCache.getKeySize();
        if ("MIX".equalsIgnoreCase(preferenceId)) {
            return random.nextInt(preferenceCnt) + 1;
        }
        int result = Integer.parseInt(preferenceId);
        if (1 <= result && result <= preferenceCnt) return result;
        log.warn("존재하지 않는 성향 아이디: {} -> 기본형으로 대체", result);
        return 0;
    }


}
