package com.simpaylog.generatorsimulator.util;

import com.simpaylog.generatorcore.cache.dto.IncomeLevelInfo;
import com.simpaylog.generatorcore.cache.dto.preference.ConsumptionDeltas;
import com.simpaylog.generatorcore.cache.dto.preference.MonthlyConsumption;
import com.simpaylog.generatorcore.cache.dto.preference.MonthlyConsumption.DailyConsumption;
import com.simpaylog.generatorcore.cache.dto.preference.PreferenceInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ConsumptionAllocator {

    private static final BigDecimal BIG_DECIMAL_HUNDRED = BigDecimal.valueOf(100); //연산시 불필요한 BigDecimal 객체 생성 줄이고자 캐싱

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
     * @param preferenceInfo 유저가 가진 성향의 소비지출 증감량 범위정보
     * @return result 유저의 소비지출 증감량 퍼센트를 담은 Map
     */
    public static ConsumptionDeltas getRandomTagConsumeDelta(PreferenceInfo preferenceInfo) {
        BigDecimal min = preferenceInfo.totalConsumeRange().min();
        BigDecimal max = preferenceInfo.totalConsumeRange().max();
        int MAX_RETRY = 100;

        Map<String, BigDecimal> deltas = new HashMap<>(); //변화량을 담을 map 객체
        List<PreferenceInfo.TagConsumeRange> shuffled = new ArrayList<>(preferenceInfo.tagConsumeRange());
        Collections.shuffle(shuffled); //태그별 변화율 다양성을 위함

        for (int retry = 0; retry < MAX_RETRY; retry++) {
            deltas.clear();
            int totalDelta = calculateDeltas(shuffled, deltas);

            if (min.intValue() <= totalDelta && totalDelta <= max.intValue()) {
                return buildConsumptionDelta(deltas, totalDelta);
            }
        }

        return buildConsumptionDelta(new HashMap<>(), 0); // 실패 시 초기화된 값 반환
    }

    /**
     * 소비 변화량 퍼센트를 계산하여 Map 객체에 담은 후, 총 소비량 변화율 반환
     * getRandomTagConsumeDelta 내에서 호출
     *
     * @param shuffled 무작위로 태그를 섞은 리스트(태그별 변화율에 다양성을 주기 위함)
     * @param deltas   변화율을 담은 map 객체
     * @return totalDelta 변화율 총합
     */
    private static int calculateDeltas(List<PreferenceInfo.TagConsumeRange> shuffled, Map<String, BigDecimal> deltas) {
        int totalDelta = 0;
        for (PreferenceInfo.TagConsumeRange tag : shuffled) {
            String tagName = tag.type();
            int delta = getRandomConsumeDelta(tag.min(), tag.max());
            deltas.put(tagName, BigDecimal.valueOf(delta));
            totalDelta += delta;
        }
        return totalDelta;
    }

    /**
     * min, max 범위 안의 값인 소비변화량 생성(소수점 두 자리까지만 반환)
     *
     * @param min 변화량 최소값
     * @param max 변화량 최대값
     * @return 랜덤 소비 변화량
     */
    public static int getRandomConsumeDelta(BigDecimal min, BigDecimal max) {
        int intMin = min.intValue();
        int intMax = max.intValue();
        return ThreadLocalRandom.current().nextInt(intMin, intMax + 1);
    }

    private static ConsumptionDeltas buildConsumptionDelta(Map<String, BigDecimal> deltas, double totalDelta) {
        return new ConsumptionDeltas(
                BigDecimal.valueOf(totalDelta),
                deltas.getOrDefault("groceriesNonAlcoholicBeverages", BigDecimal.ZERO),
                deltas.getOrDefault("alcoholicBeveragesTobacco", BigDecimal.ZERO),
                deltas.getOrDefault("clothingFootwear", BigDecimal.ZERO),
                deltas.getOrDefault("housingUtilitiesFuel", BigDecimal.ZERO),
                deltas.getOrDefault("householdGoodsServices", BigDecimal.ZERO),
                deltas.getOrDefault("health", BigDecimal.ZERO),
                deltas.getOrDefault("transportation", BigDecimal.ZERO),
                deltas.getOrDefault("communication", BigDecimal.ZERO),
                deltas.getOrDefault("recreationCulture", BigDecimal.ZERO),
                deltas.getOrDefault("education", BigDecimal.ZERO),
                deltas.getOrDefault("foodAccommodation", BigDecimal.ZERO),
                deltas.getOrDefault("otherGoodsServices", BigDecimal.ZERO)
        );
    }

    /**
     * user의 한달 소비지출 퍼센트(소비증감량)와 수입을 기반으로 월간 일별 소비지출량 계산
     *
     * @param consumptionDeltas 유저가 사용한 한 달 소비지출 정보
     * @param incomeLevelInfo   유저의 소득분위 정보
     * @param income            유저 수입
     * @param yearMonth         일별 지출을 구할 날짜(년도 및 월)
     * @return result 월간 일일 지출량 정보
     */
    public static MonthlyConsumption createNewConsumption(
            ConsumptionDeltas consumptionDeltas,
            IncomeLevelInfo incomeLevelInfo,
            BigDecimal income,
            YearMonth yearMonth) {

        int days = yearMonth.lengthOfMonth();
        BigDecimal bigDecimalDays = BigDecimal.valueOf(days); //추후 계산을 위해 만드는 BigDecimal 변수. BigDecimal 객체 중복 생성 방지
        BigDecimal monthlyConsumption = BigDecimal.ZERO; //소비 변화율이 포함된 월 소비량
        BigDecimal originalMonthlyConsumption = calcOriginalMonthlyConsumption(incomeLevelInfo, income); //성향을 제외하고, 유저의 수입 + 소득분위에 알맞은 월 소비지출 총량 계산
        List<DailyConsumption> dailyConsumptionCostList = new ArrayList<>();

        for (int day = 1; day <= days; day++) {
            LocalDate date = yearMonth.atDay(day);
            Map<String, BigDecimal> dailyValues = new HashMap<>();
            BigDecimal dailyConsumption = BigDecimal.ZERO; //일별 총 지출

            for (String tag : TAG_FIELDS) {
                //각 태그 일별 지출 금액 계산
                BigDecimal tagConsumption = calcDailyTagConsumption(bigDecimalDays, getConsumptionDeltasValue(consumptionDeltas, tag),
                        getIncomeLevelValue(incomeLevelInfo, tag), originalMonthlyConsumption);
                dailyValues.put(tag, tagConsumption);
                dailyConsumption = dailyConsumption.add(tagConsumption);
            }

            DailyConsumption dailyConsumptionCost = buildDailyConsumption(date, dailyConsumption, dailyValues);
            monthlyConsumption = monthlyConsumption.add(dailyConsumption);
            dailyConsumptionCostList.add(dailyConsumptionCost);
        }

        return new MonthlyConsumption(monthlyConsumption, income.subtract(monthlyConsumption), dailyConsumptionCostList);
    }

    /**
     * 유저의 소득분위와 월 수입을 기반으로, 한 달간의 소비지출량을 구함
     *
     * @param incomeLevelInfos 소득분위 정보
     * @param income           월급(수입)
     * @return 유저의 한 달간 소비지출 총량
     */
    private static BigDecimal calcOriginalMonthlyConsumption(IncomeLevelInfo incomeLevelInfos, BigDecimal income) {
        return income.multiply(incomeLevelInfos.consumptionExpenditure()).divide(BIG_DECIMAL_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /**
     * 각 소비태그별 일일 소비량 계산하여 반환
     *
     * @param days                     해당 월이 총 며칠인지 확인
     * @param changeDelta              소비 변화량 홗인
     * @param originalTotalConsumption 성향이 적용되지 않은 소비량
     * @return 각 상세태그의 일일 소비량
     */
    private static BigDecimal calcDailyTagConsumption(BigDecimal days, BigDecimal changeDelta, BigDecimal incomeLevelDelta, BigDecimal originalTotalConsumption) {
        //각 소비태그 월 소비량 계산
        BigDecimal monthlyTagConsumption = originalTotalConsumption.multiply(incomeLevelDelta.add(changeDelta)).divide(BIG_DECIMAL_HUNDRED, 10, RoundingMode.DOWN);
        return monthlyTagConsumption.divide(days, 2, RoundingMode.DOWN); // 소수점 둘째 자리 반올림
    }

    private static DailyConsumption buildDailyConsumption(LocalDate date, BigDecimal dailyTotalConsumption, Map<String, BigDecimal> dailyValues) {
        return new DailyConsumption(
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
                dailyValues.get("otherGoodsServices"),
                dailyTotalConsumption
        );
    }

    /**
     * 소비증감량 정보 객체에서 각 소비태그에 해당하는 값을 반환
     *
     * @param consumptionDeltas 유저의 소비지출량 변화율 정보
     * @param tagName           소비지출 태그명
     * @return tagName에 해당하는 소비지출 변화량
     */
    public static BigDecimal getConsumptionDeltasValue(ConsumptionDeltas consumptionDeltas, String tagName) {
        return switch (tagName) {
            case "groceriesNonAlcoholicBeverages" -> consumptionDeltas.groceriesNonAlcoholicBeverages();
            case "alcoholicBeveragesTobacco" -> consumptionDeltas.alcoholicBeveragesTobacco();
            case "clothingFootwear" -> consumptionDeltas.clothingFootwear();
            case "housingUtilitiesFuel" -> consumptionDeltas.housingUtilitiesFuel();
            case "householdGoodsServices" -> consumptionDeltas.householdGoodsServices();
            case "health" -> consumptionDeltas.health();
            case "transportation" -> consumptionDeltas.transportation();
            case "communication" -> consumptionDeltas.communication();
            case "recreationCulture" -> consumptionDeltas.recreationCulture();
            case "education" -> consumptionDeltas.education();
            case "foodAccommodation" -> consumptionDeltas.foodAccommodation();
            case "otherGoodsServices" -> consumptionDeltas.otherGoodsServices();
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 소득분위별 각 상세소비 태그별 소비지출량(비율) 반환
     *
     * @param incomeLevelInfo 소득분위 정보
     * @param tagName         소비지출 태그명
     * @return tagName에 해당하는 소비지출 변화량
     */
    public static BigDecimal getIncomeLevelValue(IncomeLevelInfo incomeLevelInfo, String tagName) {
        return switch (tagName) {
            case "groceriesNonAlcoholicBeverages" -> incomeLevelInfo.groceriesNonAlcoholicBeverages();
            case "alcoholicBeveragesTobacco" -> incomeLevelInfo.alcoholicBeveragesTobacco();
            case "clothingFootwear" -> incomeLevelInfo.clothingFootwear();
            case "housingUtilitiesFuel" -> incomeLevelInfo.housingUtilitiesFuel();
            case "householdGoodsServices" -> incomeLevelInfo.householdGoodsServices();
            case "health" -> incomeLevelInfo.health();
            case "transportation" -> incomeLevelInfo.transportation();
            case "communication" -> incomeLevelInfo.communication();
            case "recreationCulture" -> incomeLevelInfo.recreationCulture();
            case "education" -> incomeLevelInfo.education();
            case "foodAccommodation" -> incomeLevelInfo.foodAccommodation();
            case "otherGoodsServices" -> incomeLevelInfo.otherGoodsServices();
            default -> BigDecimal.ZERO;
        };
    }
}
