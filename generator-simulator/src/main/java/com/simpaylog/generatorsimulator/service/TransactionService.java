package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.dto.DailyTransactionResult;
import com.simpaylog.generatorcore.dto.TransactionLog;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.repository.redis.RedisPaydayRepository;
import com.simpaylog.generatorcore.service.UserService;
import com.simpaylog.generatorsimulator.dto.CategoryType;
import com.simpaylog.generatorsimulator.dto.PreferenceType;
import com.simpaylog.generatorsimulator.dto.Trade;
import com.simpaylog.generatorsimulator.exception.SimulatorException;
import com.simpaylog.generatorsimulator.kafka.producer.DailyTransactionResultProducer;
import com.simpaylog.generatorsimulator.kafka.producer.TransactionLogProducer;
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
    private final DailyTransactionResultProducer dailyTransactionResultProducer;
    private final RedisPaydayRepository redisPaydayRepository;

    public void generateTransaction(TransactionUserDto dto, LocalDate date) {
        boolean salaryFlag = false;


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
                try {
                    LocalDateTime curTime = from.plusHours(hour).plusMinutes(minutes[mIdx]);

                    // 급여 지급 결정, 8시 이후로 지급
                    if (!salaryFlag && curTime.getHour() >= 8) {
                        BigDecimal newBalance = handleSalary(dto, curTime);
                        salaryFlag = !newBalance.equals(userBalance);
                        userBalance = newBalance;
                    }

                    CategoryType picked = transactionGenerator.pickOneCategory(curTime, userPreference, lastUsedMap).orElse(null);
                    if (picked == null) { // 해당 시간에 선택된 카테고리가 없음
                        continue;
                    }
                    // TODO: 부채가 있다는 가정하에 가중치를 줄여 소비를 덜하게 하기

                    // 유저가 해당 카테고리에서 소비한 상품 및 서비스 추출
                    Trade userTrade = tradeGenerator.generateTrade(userDecile, picked);
                    generateMessage(dto.userId(), dto.sessionId(), curTime, TransactionLog.TransactionType.WITHDRAW, userTrade.tradeName(), userTrade.cost(), userBalance, userBalance.subtract(userTrade.cost()));

                    lastUsedMap.put(picked, curTime);
                    userBalance = userBalance.subtract(userTrade.cost());
                } catch (Exception e) {
                    dailyTransactionResultProducer.send(new DailyTransactionResult(dto.sessionId(), dto.userId(), false, date));
                }

            }
        }
        if (!dto.balance().equals(userBalance)) {
            userService.updateUserBalance(dto.sessionId(), dto.userId(), userBalance);
        }
        dailyTransactionResultProducer.send(new DailyTransactionResult(dto.sessionId(), dto.userId(), true, date)); // 웹소켓 결과용
    }

    private BigDecimal handleSalary(TransactionUserDto user, LocalDateTime current) {
        int numberOfPaydays = redisPaydayRepository.numberOfPayDays(user.sessionId(), user.userId(), YearMonth.from(current));
        if (numberOfPaydays == 0 || !redisPaydayRepository.isPayDay(user.sessionId(), user.userId(), YearMonth.from(current), LocalDate.from(current)))
            return user.balance();

        double averageSalary = user.incomeValue().doubleValue();
        WageType userWageType = user.wageType();

        BigDecimal wage = BigDecimal.valueOf(gaussianRandom(averageSalary / numberOfPaydays, averageSalary * userWageType.getVolatility() / numberOfPaydays));
        BigDecimal updatedBalance = user.balance().add(wage);
        generateMessage(user.userId(), user.sessionId(), current, TransactionLog.TransactionType.DEPOSIT, "급여 입금", wage, user.balance(), updatedBalance);
        return updatedBalance;
    }

    private void generateMessage(Long userId, String sessionId, LocalDateTime timestamp, TransactionLog.TransactionType type, String description, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        TransactionLog transactionLog = TransactionLog.of(
                userId,
                sessionId,
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
            log.error("[Kafka Send Fail] userId={}, type={}, time={}, error={}", userId, type, timestamp, e.getMessage());
            throw new SimulatorException("카프카 데이터 전송 실패");
        }
    }

    /**
     * @param mean   평균 값
     * @param stdDev 조정 수치(작을수록: 대부분의 값이 평균에 가까움 (안정적, 예측 가능) | 클수록: 값들이 평균에서 멀리 퍼짐 (변동성 높음, 불규칙))
     * @return 급여 금액
     */
    private long gaussianRandom(double mean, double stdDev) {
        return Math.round(mean + ThreadLocalRandom.current().nextGaussian() * stdDev);
    }

    // 최대 3개의 분 뽑기
    private int[] getRandomMinutes() {
        int cnt = ThreadLocalRandom.current().nextInt(4);
        int[] minutes = new int[cnt];
        for (int i = 0; i < cnt; i++) minutes[i] = ThreadLocalRandom.current().nextInt(60);
        Arrays.sort(minutes);
        return minutes;
    }

}
