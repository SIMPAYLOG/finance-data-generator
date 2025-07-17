package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.service.UserService;
import com.simpaylog.generatorsimulator.dto.*;
import com.simpaylog.generatorsimulator.producer.TransactionLogProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final UserService userService;
    private final TradeGenerator tradeGenerator;
    private final TransactionGenerator transactionGenerator;
    private final TransactionLogProducer transactionLogProducer;

    public DailyTransactionResult generateTransaction(User user, LocalDate date) {
        Map<CategoryType, Long> spendingByCategory = new HashMap<>();
        long totalSpending = 0L;
        int totalCnt = 0;

        try {
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.atTime(23, 59);
            Map<CategoryType, LocalDateTime> lastUsedMap = new HashMap<>();
            PreferenceType userPreference = PreferenceType.fromKey(user.getUserBehaviorProfile().getPreferenceId());
            int userDecile = user.getDecile();
            BigDecimal userBalance = user.getBalance();

            long hours = ChronoUnit.HOURS.between(from, to);
            for (int hour = 0; hour <= hours; hour++) {
                // TODO: 공과금, 통신비, 월급 처리
                int[] minutes = getRandomMinutes();
                for (int mIdx = 0; mIdx < minutes.length; mIdx++) {
                    LocalDateTime curTime = from.plusHours(hour).plusMinutes(minutes[mIdx]);
                    CategoryType picked = transactionGenerator.pickOneCategory(curTime, userPreference, lastUsedMap).orElse(null);
                    if (picked == null) { // 해당 시간에 데이터가 발생하지 않음
                        continue;
                    }

                    lastUsedMap.put(picked, curTime);
                    Trade userTrade = tradeGenerator.generateTrade(userDecile, picked);
                    updateMap(spendingByCategory, picked, userTrade.cost()); // 리턴용: 현재 상황 업데이트
                    TransactionLog withdrawLog = TransactionLog.withdraw(
                            user.getId(),
                            curTime,
                            userTrade.tradeName(),
                            userTrade.cost(),
                            userBalance,
                            userBalance.subtract(userTrade.cost())
                    );
                    userBalance = userBalance.subtract(userTrade.cost());
                    totalSpending += userTrade.cost().longValue();
                    totalCnt++;

                    transactionLogProducer.send(withdrawLog);

                }
            }
            userService.updateUserBalance(user.getId(), userBalance);
            return DailyTransactionResult.success(user.getId(), date, totalCnt, totalSpending, spendingByCategory);
        } catch (Exception e) {
            return DailyTransactionResult.fail(user.getId(), date, totalCnt, totalSpending, spendingByCategory, e.getMessage());
        }
    }

    // 최대 3개의 분 뽑기
    private int[] getRandomMinutes() {
        int cnt = ThreadLocalRandom.current().nextInt(4);
        int[] minutes = new int[cnt];
        for (int i = 0; i < cnt; i++) minutes[i] = ThreadLocalRandom.current().nextInt(60);
        Arrays.sort(minutes);
        return minutes;
    }

    private void updateMap(Map<CategoryType, Long> spendingByCategory, CategoryType categoryType, BigDecimal spending) {
        spendingByCategory.put(categoryType, spendingByCategory.getOrDefault(categoryType, 0L) + spending.longValue());
    }

}
