package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorsimulator.cache.CategoryPreferenceWeightLocalCache;
import com.simpaylog.generatorsimulator.cache.CategorySpendingPatternLocalCache;
import com.simpaylog.generatorsimulator.cache.dto.CategorySpendingPattern;
import com.simpaylog.generatorsimulator.dto.CategorySpendingWeight;
import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorcore.enums.PreferenceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionGenerator {
    private final CategorySpendingPatternLocalCache categorySpendingPatternLocalCache;
    private final CategoryPreferenceWeightLocalCache categoryPreferenceWeightLocalCache;
    private static final double MAX_PRODUCT = 1.3;
    private static final double EPS = 1e-9; // 부동 소수점 오차 제거용

    /*
    [1단계] 지금 시각에 실행 가능한 카테고리(리스트) 뽑기
    [2단계] 그 중에서 소비할 카테고리 1개 고르기
    [3단계] 정말 이 시간에 소비할 것인지 최종 확률 결정하기
     */

    public Optional<CategoryType> pickOneCategory(LocalDateTime dateTime, PreferenceType preferenceType, Map<CategoryType, LocalDateTime> repeated) {
        List<CategoryType> available = getAvailableCategories(dateTime, preferenceType, repeated); // 현재 시간에 가능한 카테고리
        if (available.isEmpty()) return Optional.empty();
        CategoryType pickSpendingCategory = pickSpendingCategory(available, preferenceType);
        if (shouldSpend(dateTime, pickSpendingCategory, preferenceType)) return Optional.of(pickSpendingCategory);
        return Optional.empty();
    }

    /**
     * [1단계] 현재 시간에 이용 가능한 카테고리를 선택한다.
     *
     * @param dateTime       현재 시간
     * @param preferenceType 사용자의 성향
     * @return 현재 시간에 가능한 카테고리 리스트
     */
    private List<CategoryType> getAvailableCategories(
            LocalDateTime dateTime,
            PreferenceType preferenceType,
            Map<CategoryType, LocalDateTime> repeated
    ) {
        int hour = dateTime.getHour();
        int weekday = dateTime.getDayOfWeek().getValue() - 1; // 0: 월요일 ~ 6: 일요일

        List<CategoryType> available = new ArrayList<>();
        for (CategoryType categoryType : CategoryType.values()) {
            if (categoryType.equals(CategoryType.COMMUNICATION) || categoryType.equals(CategoryType.HOUSING_UTILITIES_FUEL)) {
                // TODO: 이 둘은 언제 처리할 것인지?
                continue;
            }
            CategorySpendingPattern.Pattern userPattern = findPattern(categoryType, preferenceType);
            if(repeated.containsKey(categoryType)) {
                LocalDateTime lastUsed = repeated.getOrDefault(categoryType, LocalDateTime.MIN);
                long minutesSinceLast = Duration.between(lastUsed, dateTime).toMinutes();
                if(minutesSinceLast < categoryType.getMinIntervalMinutes()) continue;
            }
            double product = userPattern.hourWeights().get(hour) * userPattern.weekdayWeights().get(weekday);
            double threshold = userPattern.activeThreshold();
            if (product + EPS >= threshold) {
                available.add(categoryType);
            }
        }
        return available;
    }

    /**
     * [2단계] 그 중에서 소비할 카테고리 1개 고르기
     * @param availableCategories 카테고리 리스트
     * @param preferenceType      사용자 성향
     * @return 선택된 하나의 카테고리
     */
    private CategoryType pickSpendingCategory(
            List<CategoryType> availableCategories,
            PreferenceType preferenceType
    ) {
        CategorySpendingWeight categorySpendingWeight = categoryPreferenceWeightLocalCache.getCategorySpendingWeight(preferenceType.name());

        // 해당 성향의 카테고리별 가중치가 담긴 Map
        Map<CategoryType, Double> filteredWeights = availableCategories.stream().collect(Collectors.toMap(
                Function.identity(), // key: CategoryType
                categorySpendingWeight::getValueByCategoryType
        ));
        return pickRandomCategory(filteredWeights);
    }

    private CategoryType pickRandomCategory(Map<CategoryType, Double> filteredWeights) {
        List<Map.Entry<CategoryType, Double>> entries = new ArrayList<>(filteredWeights.entrySet());
        entries.removeIf(e -> e.getValue() <= 0);
        Collections.shuffle(entries);

        double totalWeight = filteredWeights.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight; // 0 ~ totalWeight 사이의 임의 값
        double sum = 0.0;
        for (var entry : entries) {
            sum += entry.getValue();
            if (randomValue <= sum) return entry.getKey();
        }

        // fallback
        return filteredWeights.keySet().stream().toList().get(ThreadLocalRandom.current().nextInt(filteredWeights.size()));
    }


    /**
     * [3단계] 정말 이 시간에 소비할 것인지 최종 확률 결정하기
     * @param dateTime       현재 날짜와 시간 정보
     * @param categoryType   소비 카테고리
     * @param preferenceType 성향 카테고리
     * @return 해당시간에 해당 카테고리의 발생 가능성
     */
    private boolean shouldSpend(
            LocalDateTime dateTime,
            CategoryType categoryType,
            PreferenceType preferenceType
    ) {
        int hour = dateTime.getHour();
        int weekday = dateTime.getDayOfWeek().getValue() - 1; // 0: 월요일 ~ 6: 일요일

        CategorySpendingPattern.Pattern userPattern = findPattern(categoryType, preferenceType);
        double score = userPattern.hourWeights().get(hour) * userPattern.weekdayWeights().get(weekday);
        double probability = (score - userPattern.activeThreshold()) / (MAX_PRODUCT - userPattern.activeThreshold());
        probability = Math.max(0.0, Math.min(probability, 1.0));

        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    private CategorySpendingPattern.Pattern findPattern(CategoryType categoryType, PreferenceType preferenceType) {
        CategorySpendingPattern userCategorySpendingPattern = categorySpendingPatternLocalCache.getCategorySpendingPattern();
        return userCategorySpendingPattern.getEffectivePattern(categoryType, preferenceType);
    }
}
