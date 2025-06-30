package com.simpaylog.generatorcore.strategy.impl;

import com.simpaylog.generatorcore.strategy.WageDateStrategy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomWageDateStrategy implements WageDateStrategy {
    private static final int MIN_PAYOUTS = 0;
    private static final int MAX_PAYOUTS = 6;
    private static final Random random = new Random();

    @Override
    public List<LocalDate> getPayOutDates(LocalDate baseDate) {
        int daysInMonth = baseDate.lengthOfMonth();

        // 지급 횟수 랜덤 결정 (0~6)
        int payoutCount = random.nextInt(MAX_PAYOUTS - MIN_PAYOUTS + 1) + MIN_PAYOUTS;

        // 날짜 후보 생성
        List<LocalDate> candidates = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            candidates.add(baseDate.withDayOfMonth(day));
        }

        // 랜덤 섞기
        Collections.shuffle(candidates, random);

        // 앞에서 n개만 추출
        return candidates.subList(0, payoutCount).stream()
                .sorted()
                .toList();
    }
}
