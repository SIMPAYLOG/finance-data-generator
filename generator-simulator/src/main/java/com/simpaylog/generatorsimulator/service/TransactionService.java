package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.repository.PaydayCache;
import com.simpaylog.generatorcore.service.UserService;
import com.simpaylog.generatorsimulator.dto.*;
import com.simpaylog.generatorsimulator.exception.SimulatorException;
import com.simpaylog.generatorsimulator.producer.TransactionLogProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final UserService userService;
    private final TradeGenerator tradeGenerator;
    private final TransactionGenerator transactionGenerator;
    private final TransactionLogProducer transactionLogProducer;
    private final PaydayCache paydayCache;

    public DailyTransactionResult generateTransaction(TransactionUserDto dto, LocalDate date) {
        Map<CategoryType, Long> spendingByCategory = new HashMap<>();
        long totalSpending = 0L;
        int totalSpendingCnt = 0;
        long totalIncome = 0L;
        int totalIncomeCnt = 0;
        boolean salaryFlag = false;
        try {
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.atTime(23, 59);
            Map<CategoryType, LocalDateTime> lastUsedMap = new HashMap<>();
            PreferenceType userPreference = PreferenceType.fromKey(dto.preferenceId());
            int userDecile = dto.decile();
            BigDecimal userBalance = dto.balance();

            long hours = ChronoUnit.HOURS.between(from, to);
            for (int hour = 0; hour <= hours; hour++) {
                // TODO: 공과금, 통신비
                int[] minutes = getRandomMinutes();
                for (int mIdx = 0; mIdx < minutes.length; mIdx++) {
                    LocalDateTime curTime = from.plusHours(hour).plusMinutes(minutes[mIdx]);

                    if (!salaryFlag && curTime.getHour() >= 8) { // 급여 지급 결정, 8시 이후로 지급
                        BigDecimal newBalance = handleSalary(dto, curTime);
                        totalIncome += newBalance.longValue();
                        totalIncomeCnt++;
                        salaryFlag = !newBalance.equals(userBalance);
                        userBalance = newBalance;
                    }

                    CategoryType picked = transactionGenerator.pickOneCategory(curTime, userPreference, lastUsedMap).orElse(null);
                    if (picked == null) { // 해당 시간에 데이터가 발생하지 않음
                        continue;
                    }
                    // TODO: 부채가 있다는 가정하에 가중치를 줄여 소비를 덜하게 하기
                    lastUsedMap.put(picked, curTime);
                    Trade userTrade = tradeGenerator.generateTrade(userDecile, picked);
                    updateMap(spendingByCategory, picked, userTrade.cost()); // 리턴용: 현재 상황 업데이트

                    userBalance = userBalance.subtract(userTrade.cost());
                    totalSpending += userTrade.cost().longValue();
                    totalSpendingCnt++;
                    generateMessage(dto.userId(), curTime, TransactionLog.TransactionType.WITHDRAW, userTrade.tradeName(), userTrade.cost(), userBalance, userBalance.subtract(userTrade.cost()));
                }
            }
            userService.updateUserBalance(dto.userId(), userBalance);
            return DailyTransactionResult.success(dto.userId(), date, totalSpendingCnt, totalSpending, spendingByCategory, totalIncomeCnt, totalIncome);
        } catch (Exception e) {
            return DailyTransactionResult.fail(dto.userId(), date, totalSpendingCnt, totalSpending, spendingByCategory, totalIncomeCnt, totalIncome, e.getMessage());
        }
    }


    private BigDecimal handleSalary(TransactionUserDto user, LocalDateTime current) {
        int numberOfPaydays = paydayCache.numberOfPaydays(user.userId(), YearMonth.from(current));
        if (numberOfPaydays == 0 || !paydayCache.isPayday(user.userId(), YearMonth.from(current), LocalDate.from(current)))
            return user.balance();

        double averageSalary = user.incomeValue().doubleValue();
        WageType userWageType = user.wageType();

        BigDecimal wage = BigDecimal.valueOf(gaussianRandom(averageSalary / numberOfPaydays, averageSalary * userWageType.getVolatility() / numberOfPaydays));
        BigDecimal updatedBalance = user.balance().add(wage);
        generateMessage(user.userId(), current, TransactionLog.TransactionType.DEPOSIT, "급여 입금", wage, user.balance(), updatedBalance);
        return updatedBalance;
    }

    private void generateMessage(Long userId, LocalDateTime timestamp, TransactionLog.TransactionType type, String description, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        TransactionLog transactionLog = TransactionLog.of(
                userId,
                timestamp,
                type,
                description,
                amount,
                balanceBefore,
                balanceAfter
        );
        try {
            transactionLogProducer.send(transactionLog);
        } catch (Exception e) {
            // TODO: 필요 시 fallback 로직: DB 적재, 재시도 큐, 알림 등
            log.error("[Kafka Send Fail] userId={}, type={}, msg={}", userId, type, e.getMessage());
            throw new SimulatorException("카프카 데이터 전송 실패");
        }
    }

    /**
     * @param mean   평균 값
     * @param stdDev 조정 수치(작을수록: 대부분의 값이 평균에 가까움 (안정적, 예측 가능) | 클수록: 값들이 평균에서 멀리 퍼짐 (변동성 높음, 불규칙))
     * @return 급여 금액
     */
    private double gaussianRandom(double mean, double stdDev) {
        return mean + ThreadLocalRandom.current().nextGaussian() * stdDev;
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
        long cost = spending.longValue();
        spendingByCategory.merge(categoryType, cost, Long::sum);
    }

}
