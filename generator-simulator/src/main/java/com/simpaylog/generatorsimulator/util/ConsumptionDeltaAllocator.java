package com.simpaylog.generatorsimulator.util;

import com.simpaylog.generatorcore.cache.dto.IncomeLevelInfo;
import com.simpaylog.generatorcore.cache.dto.preference.ConsumptionDelta;
import com.simpaylog.generatorcore.cache.dto.preference.PreferenceInfos;
import com.simpaylog.generatorcore.cache.dto.preference.MonthlyConsumptionCost;
import com.simpaylog.generatorcore.cache.dto.preference.MonthlyConsumptionCost.DailyConsumptionCost;

import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;

public class ConsumptionDeltaAllocator {
    //공통 반복되는 태그를 TAG_FIELDS로 분리
    private static final List<String> TAG_FIELDS = List.of(
            "groceriesNonAlcoholicBeverages",
            "alcoholicBeveragesTobacco",
            "clothingFootwear",
            "housingUtilitiesFuel",
            "householdGoodsServices",
            "health",
            "transportation",
            "communication",
            "recreationCulture",
            "education",
            "foodAccommodation",
            "otherGoodsServices"
    );

    /**
     * 각 태그별 소비증감량을 구한 후, 그 합이 총 소비증감량 합 범위와 일치하는지 확인
     * 태그별 소비증감량 총합이 총 소비증감량 합과 일치하지 않는 경우
     *
     * @param preferenceInfos 유저가 가진 성향의 소비지출 증감량 범위정보
     * @param incomeLevelInfos 유저의 소득분위 정보
     * @return result 유저의 소비지출 증감량 퍼센트를 담은 Map
     */
    public static ConsumptionDelta getRandomTagConsumeDelta(IncomeLevelInfo incomeLevelInfos, PreferenceInfos preferenceInfos) {
        int totalMin = preferenceInfos.totalConsumeRange().min();
        int totalMax = preferenceInfos.totalConsumeRange().max();
        int MAX_RETRY = 100;

        Map<String, Integer> deltas = new HashMap<>(); //변화량을 담을 map 객체
        List<PreferenceInfos.TagConsumeRange> shuffled = new ArrayList<>(preferenceInfos.tagConsumeRange());
        Collections.shuffle(shuffled); //태그별 변화율 다양성을 위함

        for (int retry = 0; retry < MAX_RETRY; retry++) {
            deltas.clear();
            int totalDelta = calculateDeltas(shuffled, incomeLevelInfos, deltas);

            if (totalMin <= totalDelta && totalDelta <= totalMax) {
                return buildConsumptionDelta(deltas, totalDelta);
            }
        }

        return buildConsumptionDelta(new HashMap<>(), 0); // 실패 시 초기화된 값 반환
    }

    /**
     * 소비 변화량 퍼센트를 계산하여 Map 객체에 담은 후, 총 소비량 변화율 반환
     * getRandomTagConsumeDelta에서 호출
     *
     * @param shuffled 무작위로 태그를 섞은 리스트(태그별 변화율에 다양성을 주기 위함)
     * @param incomeLevelInfos 소득분위 정보
     * @param deltas 변화율을 담은 map 객체
     * @return totalDelta 변화율 총합
     */
    private static int calculateDeltas(List<PreferenceInfos.TagConsumeRange> shuffled,
                                       IncomeLevelInfo incomeLevelInfos,
                                       Map<String, Integer> deltas) {
        int totalDelta = 0;
        for (PreferenceInfos.TagConsumeRange tag : shuffled) {
            String tagName = tag.type();
            int delta = getRandomConsumeDelta(tag.min(), tag.max());
            int baseValue = getIncomeLevelValue(incomeLevelInfos, tagName);
            deltas.put(tagName, delta + baseValue);
            totalDelta += delta;
        }
        return totalDelta;
    }


    public static int getRandomConsumeDelta (int min, int max){
        return min + (int) (Math.random() * (max - min + 1));
    }

    /**
     * 소득분위의 각 태그별 기본값 소비량을 반환
     * calculateDeltas에서 호출
     *
     * @param info 소득분위 정보
     * @param tagName 세부 소비지출 이름
     * @return 해당 소득분위의 tagName 소비지출 기본값
     */
    private static int getIncomeLevelValue(IncomeLevelInfo info, String tagName) {
        return switch (tagName) {
            case "groceriesNonAlcoholicBeverages" -> info.groceriesNonAlcoholicBeverages().intValue();
            case "alcoholicBeveragesTobacco" -> info.alcoholicBeveragesTobacco().intValue();
            case "clothingFootwear" -> info.clothingFootwear().intValue();
            case "housingUtilitiesFuel" -> info.housingUtilitiesFuel().intValue();
            case "householdGoodsServices" -> info.householdGoodsServices().intValue();
            case "health" -> info.health().intValue();
            case "transportation" -> info.transportation().intValue();
            case "communication" -> info.communication().intValue();
            case "recreationCulture" -> info.recreationCulture().intValue();
            case "education" -> info.education().intValue();
            case "foodAccommodation" -> info.foodAccommodation().intValue();
            case "otherGoodsServices" -> info.otherGoodsServices().intValue();
            default -> 0;
        };
    }

    private static ConsumptionDelta buildConsumptionDelta(Map<String, Integer> deltas, int totalDelta) {
        return new ConsumptionDelta(
                totalDelta,
                deltas.getOrDefault("groceriesNonAlcoholicBeverages", 0),
                deltas.getOrDefault("alcoholicBeveragesTobacco", 0),
                deltas.getOrDefault("clothingFootwear", 0),
                deltas.getOrDefault("housingUtilitiesFuel", 0),
                deltas.getOrDefault("householdGoodsServices", 0),
                deltas.getOrDefault("health", 0),
                deltas.getOrDefault("transportation", 0),
                deltas.getOrDefault("communication", 0),
                deltas.getOrDefault("recreationCulture", 0),
                deltas.getOrDefault("education", 0),
                deltas.getOrDefault("foodAccommodation", 0),
                deltas.getOrDefault("otherGoodsServices", 0)
        );
    }

    /**
     * user의 한달 소비지출 퍼센트(소비증감량)와 수입을 기반으로 월간 일별 소비지출량 계산
     *
     * @param consumptionDelta 유저가 사용한 한 달 소비지출 정보
     * @param income 유저 수입
     * @param originalTotalConsumptionCost 성향이 적용되지 않은 유저의 총 소비지출 금액
     * @param yearMonth 일별 지출을 구할 날짜(년도 및 월)
     * @return result 월간 일일 지출량 정보
     */
    public static MonthlyConsumptionCost calculateConsumption(
            ConsumptionDelta consumptionDelta,
            long income,
            long originalTotalConsumptionCost,
            YearMonth yearMonth) {

        int totalDay = yearMonth.lengthOfMonth();
        long totalConsumptionCost = 0;
        List<DailyConsumptionCost> dailyConsumptionCostList = new ArrayList<>();

        Map<String, Function<ConsumptionDelta, Integer>> getters = getDeltaGetters();

        for (int day = 0; day < totalDay; day++) {
            String date = yearMonth.getMonth().toString(); //해당 월 정보만 저장, 추후 날짜 정보 추가 필요시 수정
            Map<String, Long> dailyValues = new HashMap<>();
            long dailyTotal = 0;

            for (String tag : TAG_FIELDS) {
                long cost = calcDailyTagCost(totalDay, getters.get(tag).apply(consumptionDelta), originalTotalConsumptionCost);
                dailyValues.put(tag, cost);
                dailyTotal += cost;
            }

            DailyConsumptionCost daily = new DailyConsumptionCost(
                    date,
                    dailyValues.get("groceriesNonAlcoholicBeverages"),
                    dailyValues.get("alcoholicBeveragesTobacco"),
                    dailyValues.get("clothingFootwear"),
                    dailyValues.get("housingUtilitiesFuel"),
                    dailyValues.get("householdGoodsServices"),
                    dailyValues.get("health"),
                    dailyValues.get("transportation"),
                    dailyValues.get("communication"),
                    dailyValues.get("recreationCulture"),
                    dailyValues.get("education"),
                    dailyValues.get("foodAccommodation"),
                    dailyValues.get("otherGoodsServices")
            );

            totalConsumptionCost += dailyTotal;
            dailyConsumptionCostList.add(daily);
        }

        return new MonthlyConsumptionCost(totalConsumptionCost, income - totalConsumptionCost, dailyConsumptionCostList);
    }

    public static long calcDailyTagCost(int totalDay, int changeDelta, long originalTotalConsumptionCost){
        long cost = (originalTotalConsumptionCost * changeDelta) / 100; //현재 태그 월간 총 cost
        return cost / totalDay; //현재 태그 일간 cost 사용량, 해당 달의 총 일수로 나눔
    }

    public static Map<String, Function<ConsumptionDelta, Integer>> getDeltaGetters() {
        return Map.ofEntries(
                Map.entry("groceriesNonAlcoholicBeverages", ConsumptionDelta::groceriesNonAlcoholicBeverages),
                Map.entry("alcoholicBeveragesTobacco", ConsumptionDelta::alcoholicBeveragesTobacco),
                Map.entry("clothingFootwear", ConsumptionDelta::clothingFootwear),
                Map.entry("housingUtilitiesFuel", ConsumptionDelta::housingUtilitiesFuel),
                Map.entry("householdGoodsServices", ConsumptionDelta::householdGoodsServices),
                Map.entry("health", ConsumptionDelta::health),
                Map.entry("transportation", ConsumptionDelta::transportation),
                Map.entry("communication", ConsumptionDelta::communication),
                Map.entry("recreationCulture", ConsumptionDelta::recreationCulture),
                Map.entry("education", ConsumptionDelta::education),
                Map.entry("foodAccommodation", ConsumptionDelta::foodAccommodation),
                Map.entry("otherGoodsServices", ConsumptionDelta::otherGoodsServices)
        );
    }

    /**
     * 유저의 소득분위와 월 수입을 기반으로, 한 달간의 소비지출량을 구함
     *
     * @param incomeLevelInfos 소득분위 정보
     * @param income 월급(수입)
     * @return 유저의 한 달간 소비지출 총량
     */
    public static long calcOriginalTotalConsumption(IncomeLevelInfo incomeLevelInfos, long income){
        return (income * incomeLevelInfos.avgPropensityToConsumePct().longValue()) / 100;
    }
}
