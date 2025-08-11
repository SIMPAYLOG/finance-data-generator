package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.TestConfig;
import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.enums.AccountType;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AccountServiceTest extends TestConfig {

    @Autowired
    private AccountService accountService;
    @MockitoBean
    private AccountRepository accountRepository;

    @Test
    void 인출_정상케이스() {
        // Given
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.of(2025, 7, 1, 0 ,0);
        BigDecimal amount = BigDecimal.valueOf(50000);
        Account mockChecking = createCheckingAccount(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000));
        Account mockSaving = createSavingsAccount(BigDecimal.ZERO);
        when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.CHECKING)).thenReturn(Optional.of(mockChecking));
        when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.SAVINGS)).thenReturn(Optional.of(mockSaving));

        // When
        boolean result = accountService.withdraw(userId, amount, now);
        // Then
        assertTrue(result);
        assertEquals(BigDecimal.valueOf(50000), mockChecking.getBalance());
    }

    @Test
    void 인출_마이너스금액이지만_한도_내이므로_정상케이스() {
        // Given
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.of(2025, 7, 1, 0 ,0);
        BigDecimal amount = BigDecimal.valueOf(130000);
        Account mockChecking = createCheckingAccount(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000));
        Account mockSaving = createSavingsAccount(BigDecimal.ZERO);
        when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.CHECKING)).thenReturn(Optional.of(mockChecking));
        when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.SAVINGS)).thenReturn(Optional.of(mockSaving));

        // When
        boolean result = accountService.withdraw(userId, amount, now);
        // Then
        assertTrue(result);
        assertEquals(BigDecimal.valueOf(-30000), mockChecking.getBalance());
    }

    @Test
    void 인출_음수의_금액이_넘어왔을_때_에러반환() {
        // Given
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.of(2025, 7, 1, 0 ,0);
        BigDecimal amount = BigDecimal.valueOf(-50000);
        // When & Then
        assertThatThrownBy(() -> accountService.withdraw(userId, amount, now))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("금액이 잘못되었습니다.");
    }

    @Test
    void 인출_할때_한도를_넘어가면_false반환() {
        // Given
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.of(2025, 7, 1, 0 ,0);
        BigDecimal originBalance = BigDecimal.valueOf(100000);
        BigDecimal amount = BigDecimal.valueOf(160000);
        Account mockChecking = createCheckingAccount(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000));
        Account mockSaving = createSavingsAccount(BigDecimal.ZERO);
        when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.CHECKING)).thenReturn(Optional.of(mockChecking));
        when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.SAVINGS)).thenReturn(Optional.of(mockSaving));
        // When
        boolean result = accountService.withdraw(userId, amount, now);
        // Then
        assertFalse(result);
        assertEquals(originBalance, mockChecking.getBalance());
    }

    @Test
    void 인출_할때_돈이_모자르면_입출금통장에서_송금_후_인출() {
        // Given
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.of(2025, 7, 1, 0 ,0);
        BigDecimal amount = BigDecimal.valueOf(160000); // 결제 금액
        BigDecimal expectedCheckingAmount = BigDecimal.valueOf(-50000);
        BigDecimal expectedSavingAmount = BigDecimal.valueOf(20000);
        Account mockChecking = createCheckingAccount(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000));
        Account mockSaving = createSavingsAccount(BigDecimal.valueOf(30000));

        when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.CHECKING)).thenReturn(Optional.of(mockChecking));
        when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.SAVINGS)).thenReturn(Optional.of(mockSaving));
        // When
        boolean result = accountService.withdraw(userId, amount, now);
        // Then
        assertTrue(result);

        assertEquals(expectedCheckingAmount, mockChecking.getBalance());
        assertEquals(expectedSavingAmount, mockSaving.getBalance());
    }

    private Account createCheckingAccount(BigDecimal balance, BigDecimal overDraftLimit) {
        return Account.ofChecking(balance, overDraftLimit);
    }

    private Account createSavingsAccount(BigDecimal savingBalance) {
        return Account.ofSavings(savingBalance, BigDecimal.ZERO); // 계좌 잔고 없음
    }

}