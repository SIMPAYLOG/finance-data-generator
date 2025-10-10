package com.simpaylog.generatorcore.utils;

import com.simpaylog.generatorcore.cache.LocationLocalCache;
import com.simpaylog.generatorcore.cache.dto.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class LocationAllocator {

    private final LocationLocalCache locationLocalCache;
    private final Random random = new Random(); // 🔹 추가

    /**
     * 지역 ID를 기반으로 가중치에 따라 무작위로 인근 지역 선택
     * @param locationId 선택 기준이 되는 지역 ID
     * @return 선택된 인근 지역 이름
     */
    public String getRandomLocation(int locationId) {
        Location baseLocation = locationLocalCache.getLocation(locationId);
        List<Location.NeighborLocation> neighbors = baseLocation.neighbors();

        if (neighbors == null || neighbors.isEmpty()) {
            throw new IllegalStateException("No neighbors found for location id=" + locationId);
        }

        // 전체 가중치 합산
        BigDecimal totalWeight = neighbors.stream()
                .map(Location.NeighborLocation::weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 랜덤 값 생성 (0 ~ totalWeight)
        double rand = random.nextDouble() * totalWeight.doubleValue();

        // 누적 가중치 기반 선택
        double cumulative = 0.0;
        for (Location.NeighborLocation neighbor : neighbors) {
            cumulative += neighbor.weight().doubleValue();
            if (rand <= cumulative) {
                return neighbor.location();
            }
        }

        // 누적 오차 등으로 인해 선택 실패 시 마지막 항목 반환 (보호 코드)
        return neighbors.get(neighbors.size() - 1).location();
    }
}
