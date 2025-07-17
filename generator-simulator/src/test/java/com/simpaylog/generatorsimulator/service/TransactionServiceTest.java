package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.UserBehaviorProfile;
import com.simpaylog.generatorcore.enums.Gender;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.service.UserService;
import com.simpaylog.generatorsimulator.TestConfig;
import com.simpaylog.generatorsimulator.dto.DailyTransactionResult;
import com.simpaylog.generatorsimulator.exception.SimulatorException;
import com.simpaylog.generatorsimulator.producer.TransactionLogProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    UserService userService;

    @Test
    void 트랜잭션이_정상적으로_생성되면_총합과_카운트가_일치한다() {
        // Given
        User userFixture = userFixture(userBehaviorProfileFixture(), 3);
        LocalDate date = LocalDate.of(2025, 7, 1);

        // When
        DailyTransactionResult result = transactionService.generateTransaction(userFixture, date);

        // Then
        assertTrue(result.success());
        assertEquals(userFixture.getId(), result.userId());
        assertEquals(date, result.date());
        assertTrue(result.totalSpending() > 0);
        assertTrue(result.transactionCount() > 0);
        assertFalse(result.spendingByCategory().isEmpty());

        long categorySum = result.spendingByCategory().values().stream().mapToLong(Long::longValue).sum();
        assertEquals(result.totalSpending(), categorySum);

        verify(transactionLogProducer, atLeastOnce()).send(any());
        verify(userService, times(1)).updateUserBalance(eq(userFixture.getId()), any());
    }

    @Test
    void 예외가_발생하면_fail_결과를_반환한다() {
        // Given
        User userFixture = userFixture(userBehaviorProfileFixture(), 3);
        LocalDate date = LocalDate.of(2025, 7, 1);
        doThrow(new SimulatorException("카프카 데이터 전송 실패")).when(transactionLogProducer).send(any());
        // When
        DailyTransactionResult result = transactionService.generateTransaction(userFixture, date);

        // Then
        assertFalse(result.success());
        assertEquals(userFixture.getId(), result.userId());
        assertEquals(date, result.date());
        assertNotNull(result.errorMessage());
        assertTrue(result.transactionCount() > 0); // 중간까지 성공한 경우에도 결과 반환하는지 확인
        assertFalse(result.spendingByCategory().isEmpty());

        verify(userService, never()).updateUserBalance(eq(userFixture.getId()), any());
    }

    public static User userFixture(UserBehaviorProfile userBehaviorProfile, int decile) {
        return User.of(userBehaviorProfile, decile, 30, Gender.F, BigDecimal.valueOf(10000000), 1, 1, "TEST-OccupationName");
    }

    public static UserBehaviorProfile userBehaviorProfileFixture() {
        return UserBehaviorProfile.of(BigDecimal.valueOf(3000000), 1, WageType.REGULAR, 25);
    }
}