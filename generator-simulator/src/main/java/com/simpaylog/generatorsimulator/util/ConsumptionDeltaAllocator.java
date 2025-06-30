package com.simpaylog.generatorsimulator.util;

import com.simpaylog.generatorsimulator.dto.DailyConsumptionCost;
import com.simpaylog.generatorsimulator.dto.IncomeLevelInfos;
import com.simpaylog.generatorsimulator.dto.MonthlyConsumptionCost;
import com.simpaylog.generatorsimulator.dto.PreferenceInfos;

import java.util.*;

public class ConsumptionDeltaAllocator {

    /**
     * 각 태그별 소비증감량을 구한 후, 그 합이 총 소비증감량 합 범위와 일치하는지 확인
     * 태그별 소비증감량 총합이 총 소비증감량 합과 일치하지 않는 경우
     *
     * @param preferenceInfos 유저가 가진 성향의 소비지출 증감량 범위정보
     * @param incomeLevelInfos 유저의 소득분위 정보
     * @return result 유저의 소비지출 증감량 퍼센트를 담은 Map
     */
    public static Map<String, Integer> getRandomTagConsumeDelta(IncomeLevelInfos incomeLevelInfos, PreferenceInfos preferenceInfos) {
        List<PreferenceInfos.TagConsumeRange> tagRanges = preferenceInfos.tagConsumeRange();
        int totalMin = preferenceInfos.totalConsumeRange().min();
        int totalMax = preferenceInfos.totalConsumeRange().max();
        int MAX_RETRY = 100;
        Map<String, Integer> result = new HashMap<>();
        List<PreferenceInfos.TagConsumeRange> shuffled = new ArrayList<>(tagRanges);
        Collections.shuffle(shuffled);
        int retry = 0;
        boolean isSucceed = false;
        while (retry++ < MAX_RETRY) {
            int sum = 0;
            int putSum = 0;
            for (PreferenceInfos.TagConsumeRange tag : shuffled) {
                String type = tag.type();
                int min = tag.min();
                int max = tag.max();
                int delta = getRandomConsumeDelta(min, max);
                result.put(type, delta + incomeLevelInfos.getCost(type).intValue());
                sum += delta;
                putSum += delta + incomeLevelInfos.getCost(type).intValue();
            }
            result.put("totalChangedDelta", sum); //변화량 총합
            result.put("totalDelta", putSum); //소비량 퍼센트 총합
            if(totalMin <= sum && sum <= totalMax){
                isSucceed = true;
                break;
            }
        }
        //작업 실패 -> 작업 실패시 모든 값이 0인 result 반환
        if(!isSucceed){
            for(PreferenceInfos.TagConsumeRange tag : tagRanges){
                String type = tag.type();
                result.put(type, 0);
            }
            result.put("total", 0);
        }

        return result;
    }

    /**
     * user의 한달 소비지출 퍼센트(소비증감량)와 수입을 기반으로 월간 일별 소비지출량 계산
     *
     * @param consumptionDeltaData 유저가 사용한 한 달 소비지출 정보
     * @param income 유저 수입
     * @return result 월간 일일 지출량 정보
     */
    public static MonthlyConsumptionCost calculateConsumption(Map<String, Integer> consumptionDeltaData, long income) {
        MonthlyConsumptionCost result = new MonthlyConsumptionCost();
//        1. 유저의 소득 + 유저 성향의 평균소비성향을 기반으로 소비금액 계산
        int totalDay = 31; //총 일수
        int totalConsumptionPercent = consumptionDeltaData.get("totalDelta");
        long originalTotalConsumptionCost = (income * totalConsumptionPercent) / 100;
        long totalConsumptionCost = 0;
//        2. 상세태그별 소비증감량 확인
        List<DailyConsumptionCost> dailyConsumptionCostList = new ArrayList<>();
        for(int day = 0; day < totalDay; day++){
            DailyConsumptionCost dailyConsumptionCost = new DailyConsumptionCost();
            dailyConsumptionCost.setDate(day + ""); //임시코드, 날짜로 바꿔줘야함
            for(String key : consumptionDeltaData.keySet()){
                if(key.equals("totalDelta")) continue;
                int changeDelta = consumptionDeltaData.get(key);
                long cost = (originalTotalConsumptionCost * changeDelta) / 100; //현재 태그 월간 총 cost
                cost /= 30; //현재 태그 일간 cost 사용량, 현재 총 일수와 상관 없는 기준값으로 나누기(한 달로 나눔)

                //상세 소비타입과 이름이 같은 set 함수 호출하여 저장
                dailyConsumptionCost.setDailyCost(key, cost);

                totalConsumptionCost += cost;
            }
        }

        result.setTotalConsumptionCost(totalConsumptionCost); //월간 총 소비량
        result.setTotalSurplusCost(income - totalConsumptionCost); //월간 총 저축량
        result.setDailyConsumptionCostList(dailyConsumptionCostList); //일간 소비량 리스트
        return result;
    }

    public static int getRandomConsumeDelta (int min, int max){
        return min + (int) (Math.random() * (max - min + 1));
    }
}
