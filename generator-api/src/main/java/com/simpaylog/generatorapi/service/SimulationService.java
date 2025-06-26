package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.configuration.OccupationalLocalCache;
import com.simpaylog.generatorapi.dto.OccupationInfos;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.enums.Gender;
import com.simpaylog.generatorcore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.simpaylog.generatorapi.utils.MultinomialAllocator.normalize;
import static com.simpaylog.generatorapi.utils.MultinomialAllocator.sampleMultinomial;

@Service
@RequiredArgsConstructor
public class SimulationService {
    private final UserService userService;
    private final OccupationalLocalCache localCache;

    public void startSimulation(int totalUserCnt) {
        // TODO: 비동기 처리 모듈과 연결하여 이후 작업 필요
        System.out.println(totalUserCnt+"명 생성 요청");
        List<User> result = generateUserPool(totalUserCnt);
        //analyzeResult(result);
        userService.deleteUserAll();
    }

    private List<User> generateUserPool(int totalUserCnt) {
        List<User> userPool = new ArrayList<>();
        double[] ratios = localCache.getRatios();
        double[] normalizeRatio = normalize(ratios);
        int[] numByOccupation = sampleMultinomial(normalizeRatio, totalUserCnt);
        System.out.println("직업당 요청된 풀 수 : " + Arrays.toString(numByOccupation));
        for (int code = 0; code < numByOccupation.length; code++) {
            List<User> groupUser = generateUserPoolByOccupation(code + 1, numByOccupation[code]);
            userPool.addAll(groupUser);
        }
        return userPool;
    }

    private List<User> generateUserPoolByOccupation(int code, int totalUserCnt) {
        List<User> userPool = new ArrayList<>();
        OccupationInfos.Occupation occupation = localCache.get(code);
        double[] ageGroupRatio = occupation.ageGroupInfo()
                .stream()
                .mapToDouble(OccupationInfos.AgeGroupInfo::ratio)
                .toArray();
        double[] normalizeRatio = normalize(ageGroupRatio);
        int[] totalCntByAge = sampleMultinomial(normalizeRatio, totalUserCnt);

        for (int age = 0; age < totalCntByAge.length; age++) {
            for (int num = 0; num < totalCntByAge[age]; num++) {
                int[] dominantDeciles = occupation.ageGroupInfo().get(age).dominantDeciles();
                Random random = new Random();
                int decile = random.nextInt(dominantDeciles[1] - dominantDeciles[0] + 1) + dominantDeciles[0];
                int genderIdx = random.nextInt(2);
                BigDecimal incomeValue = BigDecimal.valueOf(occupation.decileDistribution()[decile - 1] * occupation.averageMonthlyWage());

                userPool.add(userService.createUser((age + 1) * 10, Gender.values()[genderIdx], incomeValue, decile, code));
            }
        }
        return userPool;
    }

    public void analyzeResult(List<User> users) {
        int[] numOfAge = new int[8], numOfDecile = new int[11], numOfOccupation = new int[19], numOfGender = new int[2];
        System.out.printf("생성된 사용자 풀: %d%n", users.size());
        for(User user : users) {
            numOfAge[user.getAge() / 10]++;
            if(user.getGender().equals(Gender.F)) numOfGender[0]++;
            else numOfGender[1]++;
            numOfDecile[user.getJobNumber()]++;
            numOfOccupation[user.getOccupationCode()]++;
        }
        // 연령별 인원 수
        System.out.println("[연령별 인원 수]");
        for(int i = 1; i < numOfAge.length; i++) {
            System.out.printf("%2d대: %2d명%n", i * 10, numOfAge[i]);
        }
        // 분위별 인원 수
        System.out.println("[분위별 인원 수]");
        for(int i = 1; i < numOfDecile.length; i++) {
            System.out.printf("%2d분위: %2d명%n", i, numOfDecile[i]);
        }
        // 직업별 인원 수
        System.out.println("[직업별 인원 수]");
        for(int i = 1; i < numOfOccupation.length; i++) {
            System.out.printf("%s: %2d명%n", localCache.get(i).occupationCategory(), numOfOccupation[i]);
        }

        // 성별 별 인원 수
        System.out.println("[성별 인원 수]");
        System.out.printf("남: %2d명%n", numOfGender[1]);
        System.out.printf("여: %2d명%n", numOfGender[0]);
    }
}
