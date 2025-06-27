package com.simpaylog.generatorsimulator.util;

import com.simpaylog.generatorsimulator.configuration.PreferenceLocalCache;
import com.simpaylog.generatorsimulator.dto.PreferenceInfos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConsumptionDeltaAllocatorTest {
    @Autowired
    private PreferenceLocalCache preferenceLocalCache;
    Map<Integer, PreferenceInfos> preferences;

    @BeforeAll
    void init() {
        preferences = preferenceLocalCache.getAll();
    }

    @Test
    @DisplayName("소비증감량 랜덤 추출 로직 점검")
    void totalConsumeRangeTest() {
        for (int i = 1; i <= 5; i++) {
            PreferenceInfos preferenceInfos = preferences.get(i);
            int min = preferenceInfos.totalConsumeRange().min();
            int max = preferenceInfos.totalConsumeRange().max();
            for (int tc = 0; tc < 100; tc++) {
                int totalConsumeChange = getRandomConsumeDelta(min, max);
                assertTrue(min <= totalConsumeChange && totalConsumeChange <= max);
            }
        }
    }

    @Test
    @DisplayName("각 성향별 상세태그 소비 증감량 정하기")
    void tagConsumeRangeTest() {
        Map<String, Integer> tagDeltas;
        for (int i = 1; i <= 5; i++) {
            PreferenceInfos preferenceInfos = preferences.get(i);
            int min = preferenceInfos.totalConsumeRange().min();
            int max = preferenceInfos.totalConsumeRange().max();
            List<PreferenceInfos.TagConsumeRange> tagRanges = preferenceInfos.tagConsumeRange();
            for (int tc = 0; tc < 1000; tc++) {
                tagDeltas = getRandomTagConsumeDelta(tagRanges, i);
                int sum = 0;
                assertNotNull(tagDeltas);
                for (int t = 0; t < 12; t++) {
                    int tagDelta = tagDeltas.get(tagRanges.get(t).type());
                    assertTrue(tagRanges.get(t).min() <= tagDelta && tagDelta <= tagRanges.get(t).max());
                    sum += tagDelta;
                }
                int totalDelta = tagDeltas.get("total");
                assertEquals(sum, totalDelta);
                assertTrue(min <= totalDelta && totalDelta <= max);
            }
        }
    }

    /**
     * 각 태그별 소비증감량을 구한 후, 그 합이 총 소비증감량 합 범위와 일치하는지 확인
     * 태그별 소비증감량 총합이총 소비증감량 합과 일치하지 않는 경우
     * @param tagRanges
     * @param preferenceId
     * @return
     */
    Map<String, Integer> getRandomTagConsumeDelta(List<PreferenceInfos.TagConsumeRange> tagRanges, int preferenceId) {
        int totalMin = preferences.get(preferenceId).totalConsumeRange().min();
        int totalMax = preferences.get(preferenceId).totalConsumeRange().max();
        int MAX_RETRY = 100;
        Map<String, Integer> result = new HashMap<>();
        List<PreferenceInfos.TagConsumeRange> shuffled = new ArrayList<>(tagRanges);
        Collections.shuffle(shuffled);
        int retry = 0;
        boolean isSuceeded = false;
        while (retry++ < MAX_RETRY) {
            int sum = 0;

            for (int i = 0; i < shuffled.size(); i++) {
                PreferenceInfos.TagConsumeRange tag = shuffled.get(i);
                String type = tag.type();

                int min = tag.min();
                int max = tag.max();

                int delta = getRandomConsumeDelta(min, max);
                result.put(type, delta);
                sum += delta;
            }

            result.put("total", sum);
            if(totalMin <= sum && sum <= totalMax){
                isSuceeded = true;
                break;
            }
        }

        //작업 실패 -> 작업 실패시 모든 값이 0인 result 반환
        if(!isSuceeded){
            for(PreferenceInfos.TagConsumeRange tag : tagRanges){
                String type = tag.type();
                result.put(type, 0);
            }
            result.put("total", 0);
        }

        return result;
    }

    /**
     * 범위 내의 랜덤한 증감량 도출
     * @param min
     * @param max
     * @return 범위 내랜덤증감량
     */
    int getRandomConsumeDelta (int min, int max){
        return min + (int) (Math.random() * (max - min + 1));
    }
}
