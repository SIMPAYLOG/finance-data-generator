package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.dto.DailyTransactionResult;
import com.simpaylog.generatorcore.dto.TransactionLog;
import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.AccountType;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.repository.redis.RedisPaydayRepository;
import com.simpaylog.generatorcore.service.AccountService;
import com.simpaylog.generatorsimulator.dto.CategoryType;
import com.simpaylog.generatorsimulator.dto.Trade;
import com.simpaylog.generatorsimulator.kafka.producer.DailyTransactionResultProducer;
import com.simpaylog.generatorsimulator.kafka.producer.TransactionLogProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountService accountService;
    private final TradeGenerator tradeGenerator;
    private final TransactionGenerator transactionGenerator;
    private final TransactionLogProducer transactionLogProducer;
    private final DailyTransactionResultProducer dailyTransactionResultProducer;
    private final RedisPaydayRepository redisPaydayRepository;

    public void generate(TransactionUserDto dto, LocalDate from, LocalDate to) {
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            generateTransaction(dto, date);
        }
    }

    public void generateTransaction(TransactionUserDto dto, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.atTime(23, 59);
        List<OneTimeEvent> fixedEvents = prepareOneTimeEvents(dto, date);
        Map<CategoryType, LocalDateTime> lastUsedMap = new HashMap<>();

        long hours = ChronoUnit.HOURS.between(from, to);
        for (int hour = 0; hour <= hours; hour++) {
            // TODO: 공과금, 통신비
            LocalDateTime curTime = from.plusHours(hour);
            List<Integer> minutes = getRandomMinutes(curTime.getHour(), fixedEvents);
            LocalDateTime hourStart = from.plusHours(hour);
            for (int minute : minutes) {
                curTime = hourStart.plusMinutes(minute);
                boolean fixedTriggered = false;
                if (!fixedEvents.isEmpty() && fixedEvents.getFirst().time().isEqual(curTime)) {
                    fixedEvents.removeFirst().run();
                    fixedTriggered = true;
                }
                if (fixedTriggered) continue;

                CategoryType picked = transactionGenerator.pickOneCategory(curTime, dto.preferenceType(), lastUsedMap).orElse(null);
                if (picked == null) { // 해당 시간에 선택된 카테고리가 없음
                    continue;
                }
                // 통장 잔고 마이너스일 시 가중치 높여 제한
                Account checking = accountService.getAccountByType(dto.userId(), AccountType.CHECKING);
                if (checking.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                    double ratio = Math.min(checking.getBalance().abs().doubleValue() / checking.getOverdraftLimit().doubleValue(), 1.0);
                    double prob = 0.8 - (0.5 * ratio); // 최대 0.3까지 낮춤
                    if (ThreadLocalRandom.current().nextDouble() > prob) {
//                        log.info("{} {}원 소비 스킵 prob: {}", dto.userId(), checking.getBalance(), prob);
                        continue; // 소비 스킵
                    }
                }

                // 유저가 해당 카테고리에서 소비한 상품 및 서비스 추출
                Trade userTrade = tradeGenerator.generateTrade(dto.decile(), picked);
                if (accountService.withdraw(dto.userId(), userTrade.cost(), curTime)) { // 잔액 체크 후 해당 카테고리 소비 -> true일 경우
                    lastUsedMap.put(picked, curTime);
                    generateMessage(TransactionLog.of(dto.userId(), dto.sessionId(), curTime, TransactionLog.TransactionType.WITHDRAW, userTrade.tradeName(), userTrade.cost()));
                }
            }
        }
        dailyTransactionResultProducer.send(new DailyTransactionResult(dto.sessionId(), dto.userId(), true, date)); // 웹소켓 결과용 | 유저 한명에 대한 하루 작업 종료
    }

    private List<OneTimeEvent> prepareOneTimeEvents(TransactionUserDto user, LocalDate date) {
        List<OneTimeEvent> events = new ArrayList<>();
        // 유저ID, 시간, 입출금 타입, 설명, 금액|(입금 전 금액, 입금 후 금액은 추가 필요)
        // 1. 급여 입금 + 저축 로직
        int numberOfPaydays = redisPaydayRepository.numberOfPayDays(user.sessionId(), user.userId(), YearMonth.from(date));
        if (redisPaydayRepository.isPayDay(user.sessionId(), user.userId(), YearMonth.from(date), date)) {
            double averageSalary = user.incomeValue().doubleValue();
            WageType userWageType = user.wageType();
            BigDecimal wage = BigDecimal.valueOf(gaussianRandom(averageSalary / numberOfPaydays, averageSalary * userWageType.getVolatility() / numberOfPaydays));
            LocalDateTime payTime = date.atTime(ThreadLocalRandom.current().nextInt(7) + 8, ThreadLocalRandom.current().nextInt(60));
            TimedEvent wageEvent = new TimedEvent(
                    payTime,
                    () -> {
                        accountService.deposit(user.userId(), wage, payTime);
                        generateMessage(TransactionLog.of(user.userId(), user.sessionId(), payTime, TransactionLog.TransactionType.DEPOSIT, "급여 입금", wage));
                    }
            );
            events.add(wageEvent);
            LocalDateTime saveTime = date.atTime(LocalTime.from(wageEvent.time().plusMinutes(ThreadLocalRandom.current().nextInt(30) + 1)));
            TimedEvent saveEvent = new TimedEvent(
                    saveTime,
                    () -> accountService.transferToSavings(user.userId(), wage, user.savingRate(), saveTime)
            );
            events.add(saveEvent);
        }

        // 2. 이자 발생(월말 발생)
        if (date.isEqual(YearMonth.of(date.getYear(), date.getMonth()).atEndOfMonth())) {
            LocalDateTime interestTime = date.atTime(ThreadLocalRandom.current().nextInt(4) + 8, ThreadLocalRandom.current().nextInt(60));
            TimedEvent interestEvent = new TimedEvent(
                    interestTime,
                    () -> accountService.applyMonthlyInterest(user.userId(), interestTime)
            );
            events.add(interestEvent);
        }
        events.sort(Comparator.comparing(OneTimeEvent::time)); // 시간순 정렬
        return events;
    }

    private void generateMessage(TransactionLog transactionLog) {
        try {
//            log.info("로그: {}", transactionLog);
            transactionLogProducer.send(transactionLog);
        } catch (Exception e) {
            // TODO: 필요 시 fallback 로직: DB 적재, 재시도 큐, 알림 등
            log.error("[Kafka Send Fail] userId={}, type={}, time={}, error={}", transactionLog.userId(), transactionLog.transactionType(), transactionLog.timestamp(), e.getMessage());
            //throw new SimulatorException("카프카 데이터 전송 실패");
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

    private List<Integer> getRandomMinutes(int hour, List<OneTimeEvent> events) {
        List<Integer> minutes = new ArrayList<>(events.stream().filter(e -> e.time().getHour() == hour).map(e -> e.time().getMinute()).toList());
        int cnt = ThreadLocalRandom.current().nextInt(4);
        for (int i = 0; i < cnt; i++) minutes.add(ThreadLocalRandom.current().nextInt(60));
        Collections.sort(minutes);
        return minutes;
    }

}
