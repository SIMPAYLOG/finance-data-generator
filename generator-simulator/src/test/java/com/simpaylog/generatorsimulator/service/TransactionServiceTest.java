package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.repository.PaydayCache;
import com.simpaylog.generatorcore.service.UserService;
import com.simpaylog.generatorsimulator.TestConfig;
import com.simpaylog.generatorsimulator.dto.CategoryType;
import com.simpaylog.generatorsimulator.dto.DailyTransactionResult;
import com.simpaylog.generatorsimulator.dto.PreferenceType;
import com.simpaylog.generatorsimulator.dto.TransactionLog;
import com.simpaylog.generatorsimulator.exception.SimulatorException;
import com.simpaylog.generatorsimulator.producer.TransactionLogProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    TransactionGenerator transactionGenerator;
    @MockitoBean
    UserService userService;
    @MockitoBean
    PaydayCache paydayCache;

    @Test
    void 트랜잭션이_정상적으로_생성되면_총합과_카운트가_일치한다() {
        // Given
        TransactionUserDto mockUser = mockUser(3, WageType.REGULAR);
        LocalDate date = LocalDate.of(2025, 7, 1);
        doNothing().when(transactionLogProducer).send(any());
        when(transactionGenerator.pickOneCategory(any(LocalDateTime.class), any(PreferenceType.class), anyMap())).thenReturn(Optional.of(CategoryType.ALCOHOLIC_BEVERAGES_TOBACCO));
        // When
        DailyTransactionResult result = transactionService.generateTransaction(mockUser, date);

        // Then
        assertTrue(result.success());
        assertEquals(mockUser.userId(), result.userId());
        assertEquals(date, result.date());
        assertTrue(result.totalSpending() > 0);
        assertTrue(result.spendingTransactionCount() + result.incomeTransactionCount() > 0);
        assertFalse(result.spendingByCategory().isEmpty());

        long categorySum = result.spendingByCategory().values().stream().mapToLong(Long::longValue).sum();
        assertEquals(result.totalSpending(), categorySum);

        verify(transactionLogProducer, atLeastOnce()).send(any());
        verify(userService, times(1)).updateUserBalance(eq(mockUser.userId()), any());
    }

    @Test
    void 예외가_발생하면_fail_결과를_반환한다() {
        // Given
        TransactionUserDto mockUser = mockUser(3, WageType.REGULAR);
        LocalDate date = LocalDate.of(2025, 7, 1);
        when(transactionGenerator.pickOneCategory(any(LocalDateTime.class), any(PreferenceType.class), anyMap())).thenReturn(Optional.of(CategoryType.ALCOHOLIC_BEVERAGES_TOBACCO));
        doThrow(new SimulatorException("카프카 데이터 전송 실패")).when(transactionLogProducer).send(any());
        // When
        DailyTransactionResult result = transactionService.generateTransaction(mockUser, date);
        System.out.println(result);
        // Then
        assertFalse(result.success());
        assertEquals(mockUser.userId(), result.userId());
        assertEquals(date, result.date());
        assertNotNull(result.errorMessage());
        assertTrue(result.spendingTransactionCount() + result.incomeTransactionCount() > 0); // 중간까지 성공한 경우에도 결과 반환하는지 확인
        assertFalse(result.spendingByCategory().isEmpty());

        verify(userService, never()).updateUserBalance(eq(mockUser.userId()), any());
    }

    @Test
    void 해당일자가_급여일일_경우_입금관련_트랜잭션_로그를_발생시킨다() {
        // Given
        TransactionUserDto mockUser = mockUser(3, WageType.REGULAR);
        LocalDate date = LocalDate.of(2025, 7, 25);
        LocalDate paymentDay = LocalDate.of(2025, 7, 25);
        when(paydayCache.isPayday(mockUser.userId(), YearMonth.of(2025, 7), paymentDay)).thenReturn(true);
        when(paydayCache.numberOfPaydays(mockUser.userId(), YearMonth.of(2025, 7))).thenReturn(1);
        // When
        DailyTransactionResult result = transactionService.generateTransaction(mockUser, date);
        // Then
        verify(transactionLogProducer, times(1)).send(argThat(log ->
                log.transactionType() == TransactionLog.TransactionType.DEPOSIT &&
                        log.description().equals("급여 입금")
        ));
        assertTrue(result.success());
        assertEquals(mockUser.userId(), result.userId());
        assertEquals(date, result.date());
        assertTrue(result.totalIncome() > 0);
        assertTrue(result.spendingTransactionCount() + result.incomeTransactionCount() > 0);
    }

    public static TransactionUserDto mockUser(int decile, WageType wageType) {
        return new TransactionUserDto(
                1L,
                decile,
                BigDecimal.valueOf(10000000),
                1,
                wageType,
                10,
                "TEST-active-hour",
                BigDecimal.valueOf(3000000)
        );
    }

}