package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.TestConfig;
import com.simpaylog.generatorapi.exception.ApiException;
import com.simpaylog.generatorapi.exception.ErrorCode;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Import(SimulationService.class)
class SimulationServiceTest extends TestConfig {
    @Autowired
    private SimulationService simulationService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private TransactionProgressTracker transactionProgressTracker;
    @MockitoBean
    private TransactionSimulationExecutor transactionSimulationExecutor;

    @Test
    void sessionId와_기간이_잘_들어갔을_때_정상동작() {
        // Given
        String sessionId = "test-sessionId";
        LocalDate from = LocalDate.of(2025, 7, 1);
        LocalDate to = LocalDate.of(2025, 7, 31);
        when(userService.findAllTransactionUserBySessionId(anyString())).thenReturn(List.of(createTransactionUserDto()));
        // When
        simulationService.startSimulation(sessionId, from, to);
        // Then
        verify(transactionProgressTracker, times(1)).initProgress(eq(sessionId), anyInt());
        verify(transactionSimulationExecutor, times(1)).simulateTransaction(anyList(), any(), any());

    }

    @Test
    void 존재하지_않는_sessionId를_받았을_때_예외_발생_및_로직_중단() {
        // Given
        String sessionId = "test-SessionId";
        LocalDate from = LocalDate.of(2024, 7, 1);
        LocalDate to = LocalDate.of(2024, 7, 31);
        when(userService.findAllTransactionUserBySessionId(anyString())).thenThrow(new CoreException("해당 sessionId를 찾을 수 없습니다."));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> {
            simulationService.startSimulation(sessionId, from, to);
        });
        assertEquals(ErrorCode.SESSION_ID_NOT_FOUND, exception.getErrorCode());
        verify(transactionProgressTracker, never()).initProgress(anyString(), anyInt());
        verify(transactionSimulationExecutor, never()).simulateTransaction(anyList(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void 유저가_존재하지_않는_세션일_경우_204예외_반환() {
        // Given
        String sessionId = "empty-user-session";
        LocalDate from = LocalDate.of(2024, 7, 1);
        LocalDate to = LocalDate.of(2024, 7, 31);

        when(userService.findAllTransactionUserBySessionId(anyString()))
                .thenReturn(Collections.emptyList());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                simulationService.startSimulation(sessionId, from, to)
        );

        assertEquals(ErrorCode.NO_USERS_FOUND, exception.getErrorCode());
        assertEquals("해당 session에 유저가 없습니다.", exception.getMessage());

        verify(transactionProgressTracker, never()).initProgress(any(), anyInt());
        verify(transactionSimulationExecutor, never()).simulateTransaction(any(), any(), any());
    }

    private TransactionUserDto createTransactionUserDto() {
        return new TransactionUserDto(
                1L,
                "test-sessionId",
                1,
                10,
                PreferenceType.DEFAULT,
                WageType.REGULAR,
                "TEST-active-hour",
                BigDecimal.valueOf(3000000),
                BigDecimal.ZERO
        );
    }

}