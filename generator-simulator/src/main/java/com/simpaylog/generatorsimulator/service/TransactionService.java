package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorcore.dto.DailyTransactionResult;
import com.simpaylog.generatorcore.dto.FixedObligation;
import com.simpaylog.generatorcore.dto.TransactionLog;
import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.AccountType;
import com.simpaylog.generatorcore.enums.TransactionType;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.repository.redis.FixedObligationRepository;
import com.simpaylog.generatorcore.repository.redis.RedisPaydayRepository;
import com.simpaylog.generatorcore.service.AccountService;
import com.simpaylog.generatorcore.cache.DecileStatsLocalCache;
import com.simpaylog.generatorcore.cache.dto.DecileStat;
import com.simpaylog.generatorsimulator.dto.Trade;
import com.simpaylog.generatorsimulator.kafka.producer.DailyTransactionResultProducer;
import com.simpaylog.generatorsimulator.kafka.producer.TransactionLogProducer;
import com.simpaylog.generatorcore.utils.MoneyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.simpaylog.generatorsimulator.utils.TransactionSegmentsUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountService accountService;
    private final DailyTransactionResultProducer dailyTransactionResultProducer;
    private final TransactionLogProducer transactionLogProducer;
    private final DecileStatsLocalCache decileStatsLocalCache;
    private final RedisPaydayRepository redisPaydayRepository;
    private final TransactionGenerator transactionGenerator;
    private final FixedObligationRepository fixedObligationRepository;
    private final TradeGenerator tradeGenerator;

    private static final double OVERDRAFT_BASE_PROB = 0.8;
    private static final double OVERDRAFT_PROB_DELTA = 0.5;

    public void generate(TransactionUserDto dto, LocalDate from, LocalDate to) {
        for (MonthSegment seg : splitByMonth(from, to)) {
            Map<CategoryType, BigDecimal> monthlyBudget = readjustSegBudget(decileStatsLocalCache.getDecileStat(dto.decile()), dto.incomeValue());
            Map<CategoryType, BigDecimal> segBudget = scaleBudget(monthlyBudget, seg.factor());

            Map<CategoryType, Integer> eventsCnt = tradeGenerator.estimateCounts(dto.decile(), segBudget);
            SimpleEnvelopeScaler scaler = new SimpleEnvelopeScaler(segBudget, eventsCnt);
            for (CategoryType ct : CategoryType.values()) {
                log.info("{}: 예산={}, 횟수={}", ct.getLabel(), scaler.getRemainingBudget(ct), scaler.getRemainingEvents(ct));
            }
            for (LocalDate date = seg.start(); !date.isAfter(seg.end()); date = date.plusDays(1)) {
                generateTransaction(dto, date, scaler);
            }
            for (CategoryType ct : CategoryType.values()) {
                log.info("{}: 예산={}, 횟수={}", ct.getLabel(), scaler.getRemainingBudget(ct), scaler.getRemainingEvents(ct));
            }
        }
    }

    // TODO: 필요하면 성향 정보 추가
    // 유저의 평균 급여, segBudget
    public static Map<CategoryType, BigDecimal> readjustSegBudget(DecileStat decileStat, BigDecimal incomeValue) {
        Objects.requireNonNull(decileStat, "decileStat is required");
        Objects.requireNonNull(incomeValue, "incomeValue is required");
        // 1. 카테고리별 총 소비 금액 가져오기
        Map<CategoryType, BigDecimal> base = decileStat.byCategory();
        if (base == null || base.isEmpty()) {
            return new EnumMap<>(CategoryType.class); // 빈 EnumMap 안전 반환
        }
        BigDecimal baseTotal = base.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (baseTotal.signum() == 0) return new EnumMap<>(base);

        // 2. 비율 계산하기
        BigDecimal avgDisposable = decileStat.averageDisposableIncome();    // 처분 가능 소득
        if (avgDisposable == null || avgDisposable.signum() <= 0) {
            throw new IllegalStateException("averageDisposableIncome must be positive to compute APC ratio.");
        }
        BigDecimal ratio = baseTotal.divide(avgDisposable, 8, RoundingMode.HALF_UP);

        // 3. 사용자 월 급여에 비례하는 소비 총액 설정
        BigDecimal target = incomeValue.max(BigDecimal.ZERO).multiply(ratio).setScale(8, RoundingMode.HALF_UP);
        if (target.signum() == 0) {
            return new EnumMap<>(CategoryType.class);
        }

        BigDecimal lo = baseTotal.multiply(new BigDecimal("0.5"));
        BigDecimal hi = baseTotal.multiply(new BigDecimal("1.8"));
        if (target.compareTo(lo) < 0 || target.compareTo(hi) > 0) {
            log.warn("Target clamped. income={}, ratio={}, target(before)={}", incomeValue, ratio, target);
            target = target.min(hi).max(lo);
        }

        // 4. 총 금액을 조절한 새로운 예산 설정
        BigDecimal scale = target.divide(baseTotal, 8, RoundingMode.HALF_UP);
        Map<CategoryType, BigDecimal> scaledBudget = new EnumMap<>(CategoryType.class);
        for (Map.Entry<CategoryType, BigDecimal> entry : base.entrySet()) {
            BigDecimal newVal = entry.getValue().multiply(scale).setScale(8, RoundingMode.HALF_UP);
            scaledBudget.put(entry.getKey(), newVal);
        }
        return scaledBudget;
    }

    private void generateTransaction(TransactionUserDto dto, LocalDate date, SimpleEnvelopeScaler scaler) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.atTime(23, 59);
        List<OneTimeEvent> fixedEvents = prepareOneTimeEvents(dto, date);
        Map<CategoryType, LocalDateTime> lastUsedMap = new HashMap<>();

        long hours = ChronoUnit.HOURS.between(from, to);
        for (int hour = 0; hour <= hours; hour++) {
            LocalDateTime curTime = from.plusHours(hour);
            List<Integer> minutes = getRandomMinutes(curTime.getHour(), fixedEvents);
            LocalDateTime hourStart = from.plusHours(hour);

            for (int minute : minutes) {
                curTime = hourStart.plusMinutes(minute);

                // 1. 고정 이벤트 처리
                if (drainFixedEvents(fixedEvents, curTime)) {
                    continue;
                }
                // 2. 해당 시간에 맞는 카테고리 선별하기
                CategoryType picked = transactionGenerator.pickOneCategory(curTime, dto.preferenceType(), lastUsedMap).orElse(null);
                if (picked == null) { // 해당 시간에 선택된 카테고리가 없음
                    continue;
                }

                // 3. 발생 가능한 소비인지 점검하기
                Account checking = accountService.getAccountByType(dto.userId(), AccountType.CHECKING);
                if (shouldSkipByOverdraft(checking)) continue;

                // 4. 유저가 해당 카테고리에서 소비한 상품 및 금액 추출
                Trade userTrade = tradeGenerator.generateTrade(dto.decile(), picked);
                BigDecimal scaledAmount = scaler.scale(picked, userTrade.cost());
                if (scaledAmount.signum() <= 0) continue; // 0원일 경우 건너뜀
                // 5. 결제 요청
                if (accountService.withdraw(dto.userId(), scaledAmount, curTime)) { // 잔액 체크 후 해당 카테고리 소비 -> true일 경우
                    lastUsedMap.put(picked, curTime);
                    generateMessage(TransactionLog.of(dto.userId(), dto.sessionId(), curTime, TransactionType.WITHDRAW, userTrade.tradeName(), scaledAmount));
                } else {
                    scaler.rollback(picked, scaledAmount);
                }
            }
        }
        dailyTransactionResultProducer.send(new DailyTransactionResult(dto.sessionId(), dto.userId(), true, date)); // 웹소켓 결과용 | 유저 한명에 대한 하루 작업 종료
    }

    private boolean drainFixedEvents(List<OneTimeEvent> events, LocalDateTime curTime) {
        boolean flag = false;
        while (!events.isEmpty() && events.getFirst().time().isEqual(curTime)) {
            events.removeFirst().run();
            flag = true;
        }
        return flag;
    }

    private List<OneTimeEvent> prepareOneTimeEvents(TransactionUserDto user, LocalDate date) {
        List<OneTimeEvent> events = new ArrayList<>();
        events.addAll(preparePaydayEvents(user, date));
        events.addAll(prepareInterestEvents(user, date));
        events.addAll(prepareFixedObligationEvents(user, date));
        events.sort(Comparator.comparing(OneTimeEvent::time)); // 시간순 정렬
        return events;
    }

    // 1. 급여 입금 + 저축 로직
    private List<OneTimeEvent> preparePaydayEvents(TransactionUserDto user, LocalDate date) {
        List<OneTimeEvent> events = new ArrayList<>();
        YearMonth ym = YearMonth.from(date);
        int numberOfPaydays = redisPaydayRepository.numberOfPayDays(user.sessionId(), user.userId(), ym);
        if (!redisPaydayRepository.isPayDay(user.sessionId(), user.userId(), ym, date)) return events;

        double avg = user.incomeValue().doubleValue();
        WageType type = user.wageType();
        BigDecimal wage = BigDecimal.valueOf(
                gaussianRandom(avg / numberOfPaydays, avg * type.getVolatility() / numberOfPaydays)
        );
        BigDecimal finalWage = MoneyUtil.roundTo10(wage);

        LocalDateTime payTime = date.atTime(ThreadLocalRandom.current().nextInt(7) + 8,
                ThreadLocalRandom.current().nextInt(60));
        events.add(new TimedEvent(
                payTime,
                () -> {
                    accountService.deposit(user.userId(), finalWage, payTime);
                    generateMessage(TransactionLog.of(
                            user.userId(), user.sessionId(), payTime, TransactionType.DEPOSIT, "급여 입금", finalWage
                    ));
                }
        ));
        LocalDateTime saveTime = payTime.plusMinutes(ThreadLocalRandom.current().nextInt(30) + 1);
        events.add(new TimedEvent(
                saveTime,
                () -> accountService.transferToSavings(user.userId(), finalWage, user.savingRate(), saveTime)
        ));

        return events;
    }
    // 2. 이자 발생(월말 발생)
    private List<OneTimeEvent> prepareInterestEvents(TransactionUserDto user, LocalDate date) {
        if (!date.equals(YearMonth.from(date).atEndOfMonth())) return List.of();

        LocalDateTime interestTime = date.atTime(ThreadLocalRandom.current().nextInt(4) + 8,
                ThreadLocalRandom.current().nextInt(60));
        return List.of(new TimedEvent(
                interestTime,
                () -> accountService.applyMonthlyInterest(user.userId(), interestTime)
        ));
    }

    // 3. 고정 지출/수입(Redis에서 가져오기)
    private List<OneTimeEvent> prepareFixedObligationEvents(TransactionUserDto user, LocalDate date) {
        List<FixedObligation> items = fixedObligationRepository.findAll(user.sessionId(), user.userId());
        if (items.isEmpty()) return List.of();

        List<OneTimeEvent> events = new ArrayList<>();
        for (FixedObligation item : items) {
            if (!isWithinEffective(item, date)) continue;
            if (!isRecurrenceHit(item, date)) continue; // 현재는 dayOfMonth만 체크

            LocalDateTime time = date.atTime(9, 30);
            if (item.transactionType() == TransactionType.DEPOSIT) {
                events.add(new TimedEvent(
                        time, () -> accountService.deposit(user.userId(), item.amount(), time)
                ));
            } else {
                events.add(new TimedEvent(
                        time, () -> {
                    accountService.withdraw(user.userId(), item.amount(), time);
                    generateMessage(TransactionLog.of(
                            user.userId(), user.sessionId(), time, TransactionType.WITHDRAW,
                            item.description(), item.amount()
                    ));
                }
                ));
            }
        }
        return events;
    }

    private boolean isWithinEffective(FixedObligation item, LocalDate date) {
        LocalDate from = LocalDate.parse(item.effectiveFrom());
        if (date.isBefore(from)) return false;
        if (item.effectiveTo() != null && !item.effectiveTo().isBlank()) {
            LocalDate to = LocalDate.parse(item.effectiveTo());
            if (date.isAfter(to)) return false;
        }
        return true;
    }

    private boolean isRecurrenceHit(FixedObligation item, LocalDate date) {
        Integer dom = item.recurrence().dayOfMonth();
        return dom != null && dom == date.getDayOfMonth();
    }

    private void generateMessage(TransactionLog transactionLog) {
        try {
            transactionLogProducer.send(transactionLog);
            log.info("{}",transactionLog);
        } catch (Exception e) {
            // TODO: 필요 시 fallback 로직: DB 적재, 재시도 큐, 알림 등
            log.error("[Kafka Send Fail] userId={}, type={}, time={}, error={}", transactionLog.userId(), transactionLog.transactionType(), transactionLog.timestamp(), e.getMessage());
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
        Set<Integer> fixed = new HashSet<>(events.stream().filter(e -> e.time().getHour() == hour).map(e -> e.time().getMinute()).toList());
        List<Integer> minutes = new ArrayList<>(fixed);

        int cnt = ThreadLocalRandom.current().nextInt(4);
        while (cnt-- > 0) {
            int minute = ThreadLocalRandom.current().nextInt(60);
            if (fixed.add(minute)) minutes.add(minute);
        }
        Collections.sort(minutes);
        return minutes;
    }

    // 통장 잔고 마이너스일 시 가중치 높여 제한
    private boolean shouldSkipByOverdraft(Account checking) {
        if (checking.getBalance().compareTo((BigDecimal.ZERO)) >= 0) return false;  // 돈이 여유가 있는 경우
        BigDecimal overDraftLimit = checking.getOverdraftLimit();                   // 마이너스 한도 체크
        double limit = (overDraftLimit == null) ? 0.0 : overDraftLimit.doubleValue();
        limit = Math.max(1.0, limit);                                               // 0으로 나누기 방지
        double ratio = Math.min(checking.getBalance().abs().doubleValue() / limit, 1.0);
        double prob = OVERDRAFT_BASE_PROB - (OVERDRAFT_PROB_DELTA * ratio);         // 최대 0.3까지 낮춤

        return ThreadLocalRandom.current().nextDouble() > prob;
    }

}
