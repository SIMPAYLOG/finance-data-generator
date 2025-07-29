package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.repository.redis.RedisPaydayRepository;
import com.simpaylog.generatorcore.service.UserService;
import com.simpaylog.generatorsimulator.TestConfig;
import com.simpaylog.generatorsimulator.kafka.producer.DailyTransactionResultProducer;
import com.simpaylog.generatorsimulator.kafka.producer.TransactionLogProducer;
import com.simpaylog.generatorsimulator.dto.CategoryType;
import com.simpaylog.generatorsimulator.dto.PreferenceType;
import com.simpaylog.generatorsimulator.dto.TransactionLog;
import com.simpaylog.generatorsimulator.exception.SimulatorException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

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
    TransactionGenerator transactionGenerator;
    @MockitoBean
    UserService userService;
    @MockitoBean
    RedisPaydayRepository redisPaydayRepository;

    @Test
    void 트랜잭션이_정상적으로_생성되면_총합과_카운트가_일치한다() {
        // Given
        TransactionUserDto mockUser = mockUser(3, WageType.REGULAR);
        LocalDate date = LocalDate.of(2025, 7, 1);
        doNothing().when(transactionLogProducer).send(any());
        when(transactionGenerator.pickOneCategory(any(LocalDateTime.class), any(PreferenceType.class), anyMap())).thenReturn(Optional.of(CategoryType.ALCOHOLIC_BEVERAGES_TOBACCO));
        // When
        transactionService.generateTransaction(mockUser, date);

        // Then
        verify(transactionLogProducer, atLeastOnce()).send(any());
        verify(dailyTransactionResultProducer, times(1)).send(any());
        verify(userService, times(1)).updateUserBalance(anyString(), eq(mockUser.userId()), any());
    }

    @Test
    void 사용자의_잔액이_동일하다면_잔액을_업데이트_하지않는다() {
        // Given
        TransactionUserDto mockUser = mockUser(3, WageType.REGULAR);
        LocalDate date = LocalDate.of(2025, 7, 1);
        doNothing().when(transactionLogProducer).send(any());
        when(transactionGenerator.pickOneCategory(any(LocalDateTime.class), any(PreferenceType.class), anyMap())).thenReturn(Optional.empty());
        // When
        transactionService.generateTransaction(mockUser, date);

        // Then
        verify(transactionLogProducer, never()).send(any());
        verify(dailyTransactionResultProducer, times(1)).send(any());
        verify(userService, never()).updateUserBalance(anyString(), eq(mockUser.userId()), any());
    }

    @Test
    void 예외가_발생하면_fail_결과를_반환한다() {
        // Given
        TransactionUserDto mockUser = mockUser(3, WageType.REGULAR);
        LocalDate date = LocalDate.of(2025, 7, 1);
        when(transactionGenerator.pickOneCategory(any(LocalDateTime.class), any(PreferenceType.class), anyMap())).thenReturn(Optional.of(CategoryType.ALCOHOLIC_BEVERAGES_TOBACCO));
        doThrow(new SimulatorException("카프카 데이터 전송 실패")).when(transactionLogProducer).send(any());
        // When
        transactionService.generateTransaction(mockUser, date);

        // Then
        verify(userService, never()).updateUserBalance(anyString(), eq(mockUser.userId()), any());
    }

    @Test
    void 해당일자가_급여일일_경우_입금관련_트랜잭션_로그를_발생시킨다() {
        // Given
        TransactionUserDto mockUser = mockUser(3, WageType.REGULAR);
        LocalDate date = LocalDate.of(2025, 7, 25);
        LocalDate paymentDay = LocalDate.of(2025, 7, 25);
        when(redisPaydayRepository.isPayDay(anyString(), eq(mockUser.userId()), eq(YearMonth.of(2025, 7)), eq(paymentDay))).thenReturn(true);
        when(redisPaydayRepository.numberOfPayDays(anyString(), eq(mockUser.userId()), eq(YearMonth.of(2025, 7)))).thenReturn(1);
        // When
        transactionService.generateTransaction(mockUser, date);
        // Then
        verify(transactionLogProducer, times(1)).send(argThat(log ->
                log.transactionType() == TransactionLog.TransactionType.DEPOSIT &&
                        log.description().equals("급여 입금")
        ));
    }

    public static TransactionUserDto mockUser(int decile, WageType wageType) {
        return new TransactionUserDto(
                1L,
                "sessionId",
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