package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.TestConfig;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorsimulator.dto.DailyTransactionResult;
import com.simpaylog.generatorsimulator.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Import(TransactionSimulationExecutor.class)
class TransactionSimulationExecutorTest extends TestConfig {

    @Autowired
    private TransactionSimulationExecutor executor;
    @MockitoBean
    private TransactionService transactionService;

    @Test
    void 모든_사용자에_대해_트랜잭션을_생성했을때_정상적으로_성공을_반환하는지() {
        //Given
        List<TransactionUserDto> users = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> new TransactionUserDto((long) i, 5, BigDecimal.valueOf(1000000), 0, WageType.REGULAR, 25, "TEST-activeHour", BigDecimal.valueOf(300000)))
                .toList();

        LocalDate from = LocalDate.of(2024, 7, 1);
        LocalDate to = LocalDate.of(2024, 7, 3);
        when(transactionService.generateTransaction(any(), any()))
                .thenAnswer(invocation -> {
                    TransactionUserDto user = invocation.getArgument(0);
                    LocalDate date = invocation.getArgument(1);
                    return DailyTransactionResult.success(user.userId(), date, 1, 10000L, Map.of(), 1, 300000L);
                });

        //When
        executor.simulateTransaction(users, from, to);

        // Then: 총 30번 메서드가 호출되었는지 검증
        verify(transactionService, times(users.size() * 3)).generateTransaction(any(), any());
    }

    @Test
    void 트랜잭션_데이터_생성_중_예외가_발생하면_실패_결과를_반환한다() {
        // Given
        List<TransactionUserDto> users = List.of(new TransactionUserDto(1L, 1, BigDecimal.TEN, 1, WageType.BI_WEEKLY, 25, "TEST-ACTIVE-HOUR", BigDecimal.valueOf(1000)));
        LocalDate testDate = LocalDate.of(2025, 7, 15);

        when(transactionService.generateTransaction(any(), eq(testDate))).thenReturn(DailyTransactionResult.fail(1L, testDate, 0, 0, Map.of(), 0, 0, "테스트 에러 메시지"));
        // When
        executor.simulateTransaction(users, testDate, testDate);
        // Then
        verify(transactionService, times(1)).generateTransaction(any(), eq(testDate));
    }

    @Test
    void 유저_리스트가_비어있을_때_예외없이_정상_종료되는지() {
        // Given
        List<TransactionUserDto> users = List.of(); // 빈 리스트
        LocalDate from = LocalDate.of(2024, 7, 1);
        LocalDate to = LocalDate.of(2024, 7, 3);

        // When & Then
        assertDoesNotThrow(() -> executor.simulateTransaction(users, from, to));

        // 호출이 없어야 하므로, generateTransaction 호출 안됨
        verify(transactionService, never()).generateTransaction(any(), any());
    }

}