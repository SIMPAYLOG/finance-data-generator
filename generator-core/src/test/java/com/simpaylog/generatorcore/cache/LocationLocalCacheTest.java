package com.simpaylog.generatorcore.cache;

import com.simpaylog.generatorcore.TestConfig;
import com.simpaylog.generatorcore.cache.dto.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@Import(LocationLocalCache.class)
public class LocationLocalCacheTest extends TestConfig {

    private final String[] locationNames = new String[] {
            "", "서울", "인천", "수원", "대전", "천안", "청주", "전주", "광주", "부산", "대구"
    };

    @Autowired
    LocationLocalCache locationCache;

    @RepeatedTest(value = 10)
    @DisplayName("locations.json → 객체 매핑 검증")
    void loadLocationsTest(RepetitionInfo repetitionInfo) {
        int locationId = repetitionInfo.getCurrentRepetition();

        // 캐시가 정상적으로 로드되었는지
        assertNotNull(locationCache.getCache(), "캐시 맵은 null이면 안 됨");
        assertFalse(locationCache.getCache().isEmpty(), "캐시는 비어 있으면 안 됨");

        // 해당 ID가 존재하는 경우만 테스트
        if (locationCache.getCache().containsKey(locationId)) {
            Location loc = locationCache.getLocation(locationId);
            assertNotNull(loc, "location 객체는 null이면 안 됨");
            assertEquals(locationNames[locationId], loc.location(), "ID에 맞는 지역명이 로드되어야 함");

            // neighbors 검증
            assertNotNull(loc.neighbors(), "neighbors 리스트는 null이면 안 됨");
            assertFalse(loc.neighbors().isEmpty(), "neighbors 리스트는 비어 있으면 안 됨");

            // 자기 자신 지역 포함 검증
            assertTrue(
                    loc.neighbors().stream().anyMatch(n -> n.location().equals(loc.location())),
                    "neighbors 중 자기 자신 지역이 포함되어야 함"
            );
        } else {
            // ID가 존재하지 않으면 테스트 스킵
            System.out.printf("⚠️ locationId=%d 는 현재 캐시에 존재하지 않아 스킵%n", locationId);
        }
    }
}
