package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.dto.DailyTransactionResult;
import com.simpaylog.generatorcore.dto.TransactionLog;
import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.AccountType;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.repository.redis.RedisPaydayRepository;
import com.simpaylog.generatorcore.service.AccountService;
import com.simpaylog.generatorsimulator.TestConfig;
import com.simpaylog.generatorsimulator.cache.DecileStatsLocalCache;
import com.simpaylog.generatorsimulator.dto.Trade;
import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorsimulator.kafka.producer.DailyTransactionResultProducer;
import com.simpaylog.generatorsimulator.kafka.producer.TransactionLogProducer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import({TransactionGenerator.class, TradeGenerator.class, TransactionService.class})
class TransactionServiceTest extends TestConfig {

    @Autowired
    TransactionService transactionService;
    @MockitoBean
    TransactionLogProducer transactionLogProducer;
    @MockitoBean
    DailyTransactionResultProducer dailyTransactionResultProducer;
    @MockitoBean
    DecileStatsLocalCache decileStatsLocalCache;
    @MockitoBean
    TransactionGenerator transactionGenerator;
    @MockitoBean
    TradeGenerator tradeGenerator;
    @MockitoBean
    AccountService accountService;
    @MockitoBean
    RedisPaydayRepository redisPaydayRepository;

    // 0. 정상 케이스
    @Test
    void 정상케이스_최대한_예산금액에_맞춰서_결제_금액을_생성한다() {
        // Given
        int userDecile = 1;
        int budget = 310000;
        TransactionUserDto mockUser = mockUser(userDecile, WageType.REGULAR);
        LocalDate from = LocalDate.of(2025, 7, 1);
        LocalDate to = LocalDate.of(2025, 7, 3);
        int days = Math.toIntExact(ChronoUnit.DAYS.between(from, to) + 1);
        // 1. 월 예산 설정
        var monthlyStats = new EnumMap<CategoryType, BigDecimal>(CategoryType.class);
        monthlyStats.put(CategoryType.GROCERIES_NON_ALCOHOLIC_BEVERAGES, BigDecimal.valueOf(budget));
        when(decileStatsLocalCache.getDecileStat(anyInt())).thenReturn(monthlyStats);

        // 2. 소비 횟수 설정
        when(tradeGenerator.estimateCounts(anyInt(), anyMap()))
                .thenAnswer(invocation -> {
                            Map<CategoryType, BigDecimal> segBudget = invocation.getArgument(1);
                            Map<CategoryType, Integer> cnt = new EnumMap<>(CategoryType.class);
                            segBudget.forEach((k, v) -> {
                                int n = v.divide(BigDecimal.valueOf(20000), 0, RoundingMode.DOWN).intValue();
                                cnt.put(k, Math.max(1, n));
                            });
                            return cnt;
                        }
                );

        // 3. 카테고리 설정
        when(transactionGenerator.pickOneCategory(any(), any(), anyMap()))
                .thenReturn(Optional.of(CategoryType.GROCERIES_NON_ALCOHOLIC_BEVERAGES));
        // 4. 상품 설정
        when(tradeGenerator.generateTrade(anyInt(), any()))
                .thenReturn(new Trade("과일세트", BigDecimal.valueOf(20000)));
        // 5. 금액 체크
        when(accountService.getAccountByType(anyLong(), eq(AccountType.CHECKING)))
                .thenReturn(createCheckingAccount(BigDecimal.valueOf(100000), BigDecimal.ZERO));
        // 6. 결제 성공
        when(accountService.withdraw(anyLong(), any(BigDecimal.class), any(LocalDateTime.class)))
                .thenReturn(true);
        // When
        transactionService.generate(mockUser, from, to);
        // Then

        // 출금된 금액 체크
        ArgumentCaptor<BigDecimal> captor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(accountService, atLeastOnce()).withdraw(any(), captor.capture(), any());
        BigDecimal spent = captor.getAllValues().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        // 세그먼트 예산 = 월예산 × (3 / 31) -> 5%이내의 오차
        BigDecimal segBudget = new BigDecimal(budget)
                .multiply(BigDecimal.valueOf((double) days / (double) from.getDayOfMonth()));
        assertTrue(spent.compareTo(segBudget.multiply(new BigDecimal("1.05"))) <= 0, "spent should be ≤ segBudget(±5%)"); // 전체 지출 금액은 예산의 5%이내에 수렴

        // 결과 전송 호출여부
        verify(dailyTransactionResultProducer, atLeastOnce()).send(any(DailyTransactionResult.class));
    }

    // 1. 고정 이벤트 처리 여부

    // 2. 뽑힌 카테고리가 없을 경우
    @Test
    void 실패케이스_뽑힌_카테고리가_없을경우_결제가_일어나지_않는다() {
        // Given
        int userDecile = 1;
        int budget = 310000;
        TransactionUserDto mockUser = mockUser(userDecile, WageType.REGULAR);
        LocalDate from = LocalDate.of(2025, 7, 1);
        LocalDate to = LocalDate.of(2025, 7, 3);
        // 1. 월 예산 설정
        var monthlyStats = new EnumMap<CategoryType, BigDecimal>(CategoryType.class);
        monthlyStats.put(CategoryType.GROCERIES_NON_ALCOHOLIC_BEVERAGES, BigDecimal.valueOf(budget));
        when(decileStatsLocalCache.getDecileStat(anyInt())).thenReturn(monthlyStats);

        // 2. 소비 횟수 설정
        when(tradeGenerator.estimateCounts(anyInt(), anyMap()))
                .thenAnswer(invocation -> {
                            Map<CategoryType, BigDecimal> segBudget = invocation.getArgument(1);
                            Map<CategoryType, Integer> cnt = new EnumMap<>(CategoryType.class);
                            segBudget.forEach((k, v) -> {
                                int n = v.divide(BigDecimal.valueOf(20000), 0, RoundingMode.DOWN).intValue();
                                cnt.put(k, Math.max(1, n));
                            });
                            return cnt;
                        }
                );
        when(transactionGenerator.pickOneCategory(any(), any(), anyMap()))
                .thenReturn(Optional.empty());

        // When
        transactionService.generate(mockUser, from, to);

        // Then
        verify(transactionLogProducer, never()).send(any(TransactionLog.class));
        verify(dailyTransactionResultProducer, atLeastOnce()).send(any(DailyTransactionResult.class));

    }

    @Test
    void 실패케이스_출금_실패시_롤백한다() {
        // Given
        int userDecile = 1;
        int budget = 310000;
        TransactionUserDto mockUser = mockUser(userDecile, WageType.REGULAR);
        LocalDate from = LocalDate.of(2025, 7, 1);
        LocalDate to = LocalDate.of(2025, 7, 3);
        // 1. 월 예산 설정
        var monthlyStats = new EnumMap<CategoryType, BigDecimal>(CategoryType.class);
        monthlyStats.put(CategoryType.GROCERIES_NON_ALCOHOLIC_BEVERAGES, BigDecimal.valueOf(budget));
        when(decileStatsLocalCache.getDecileStat(anyInt())).thenReturn(monthlyStats);

        // 2. 소비 횟수 설정
        when(tradeGenerator.estimateCounts(anyInt(), anyMap()))
                .thenAnswer(invocation -> {
                            Map<CategoryType, BigDecimal> segBudget = invocation.getArgument(1);
                            Map<CategoryType, Integer> cnt = new EnumMap<>(CategoryType.class);
                            segBudget.forEach((k, v) -> {
                                int n = v.divide(BigDecimal.valueOf(20000), 0, RoundingMode.DOWN).intValue();
                                cnt.put(k, Math.max(1, n));
                            });
                            return cnt;
                        }
                );

        // 3. 카테고리 설정
        when(transactionGenerator.pickOneCategory(any(), any(), anyMap()))
                .thenReturn(Optional.of(CategoryType.GROCERIES_NON_ALCOHOLIC_BEVERAGES));
        // 4. 상품 설정
        when(tradeGenerator.generateTrade(anyInt(), any()))
                .thenReturn(new Trade("과일세트", BigDecimal.valueOf(20000)));
        // 5. 금액 체크
        when(accountService.getAccountByType(anyLong(), eq(AccountType.CHECKING)))
                .thenReturn(createCheckingAccount(BigDecimal.valueOf(10000), BigDecimal.ZERO));
        // When
        transactionService.generate(mockUser, from, to);

        // Then
        verify(transactionLogProducer, never()).send(any(TransactionLog.class));
        verify(dailyTransactionResultProducer, atLeastOnce()).send(any(DailyTransactionResult.class));
    }

    private TransactionUserDto mockUser(int decile, WageType wageType) {
        return new TransactionUserDto(
                1L,
                "test-sessionId",
                decile,
                PreferenceType.DEFAULT,
                wageType,
                10,
                "TEST-active-hour",
                BigDecimal.valueOf(3000000),
                BigDecimal.ZERO
        );
    }

    private Account createCheckingAccount(BigDecimal balance, BigDecimal overDraftLimit) {
        return Account.ofChecking(balance, overDraftLimit);
    }


}