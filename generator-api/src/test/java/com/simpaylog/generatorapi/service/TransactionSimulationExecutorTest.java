package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.TestConfig;
import com.simpaylog.generatorapi.kafka.producer.TransactionGenerationRequestProducer;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.WageType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Import(TransactionSimulationExecutor.class)
class TransactionSimulationExecutorTest extends TestConfig {

    @Autowired
    private TransactionSimulationExecutor executor;
    @MockitoBean
    private TransactionGenerationRequestProducer producer;

    @Test
    void 정해진_날짜와_유저에대해_정확히_생성된다() {
        // Given
        TransactionUserDto user1 = new TransactionUserDto(1L, 5, BigDecimal.TEN, 1, WageType.REGULAR, 25, "TEST-active-hour", BigDecimal.valueOf(3000000));
        TransactionUserDto user2 = new TransactionUserDto(2L, 7, BigDecimal.valueOf(5000), 2, WageType.REGULAR, 15, "TEST-active-hour", BigDecimal.valueOf(3500000));
        List<TransactionUserDto> users = List.of(user1, user2);

        LocalDate from = LocalDate.of(2025, 7, 1);
        LocalDate to = LocalDate.of(2025, 7, 3); // 3일간

        // When
        executor.simulateTransaction(users, from, to);

        // Then
        // 2명 × 3일 = 6번 전송
        verify(producer, times(6)).send(any(TransactionGenerationRequest.class));
    }
}