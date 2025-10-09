package com.simpaylog.generatorcore.utils;

import com.simpaylog.generatorcore.TestConfig;
import com.simpaylog.generatorcore.cache.LocationLocalCache;
import com.simpaylog.generatorcore.cache.dto.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({LocationAllocator.class, LocationLocalCache.class})
public class LocationAllocatorTest extends TestConfig {

    @Autowired
    LocationAllocator allocator;

    @Autowired
    LocationLocalCache locationCache;

    @RepeatedTest(10)
    @DisplayName("getRandomNeighborByWeight → 랜덤 neighbor 선택 검증")
    void getRandomNeighborByWeightTest(RepetitionInfo repetitionInfo) {
        int locationId = repetitionInfo.getCurrentRepetition();

        // 캐시에 해당 지역이 있는 경우만 테스트
        if (!locationCache.getCache().containsKey(locationId)) {
            System.out.printf("locationId=%d 는 캐시에 존재하지 않아 스킵%n", locationId);
            return;
        }

        Location baseLocation = locationCache.getLocation(locationId);
        List<Location.NeighborLocation> neighbors = baseLocation.neighbors();
        assertNotNull(neighbors, "neighbors 리스트는 null이면 안 됨");
        assertFalse(neighbors.isEmpty(), "neighbors 리스트는 비어 있으면 안 됨");

        // 메서드 호출
        String selectedNeighbor = allocator.getRandomLocation(locationId);
        System.out.println(selectedNeighbor);
        assertNotNull(selectedNeighbor, "반환된 neighbor는 null이면 안 됨");

        // neighbors 중 하나인지 검증
        boolean exists = neighbors.stream()
                .anyMatch(n -> n.location().equals(selectedNeighbor));
        assertTrue(exists, "반환된 neighbor는 neighbors 리스트에 반드시 포함되어야 함");
    }
}
