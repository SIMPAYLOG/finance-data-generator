package com.simpaylog.generatorcore.dto;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Getter
public enum LocationType {
    SEOUL(1, "서울"),
    BUSAN(2, "부산"),
    DAEGU(3, "대구"),
    INCHEON(4, "인천"),
    DAEJEON(5, "대전"),
    GWANGJU(6, "광주"),
    ULSAN(7, "울산"),
    SEJONG(8, "세종"),
    JEJU(9, "제주도");

    private final int id;
    private final String name;

    LocationType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // 전체 목록
    private static final List<LocationType> VALUES = Arrays.asList(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    // 무작위 지역 반환
    public static LocationType random() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
