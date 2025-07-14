package com.simpaylog.generatorcore.dto.response;

// 연령대 코드와 포맷된 문자열을 함께 담을 레코드
public record AgeGroupDetailResponse(
    String id,
    String group
) {
}