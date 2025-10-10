package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.TestConfig;
import com.simpaylog.generatorcore.dto.TransactionLog;
import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.UserBehaviorProfile;
import com.simpaylog.generatorcore.enums.*;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.AccountRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class AccountDomainServiceTest extends TestConfig {

    @Autowired
    AccountDomainService sut;
    @MockitoBean
    private AccountRepository accountRepository;

    @Nested
    class CreditTests {

        @Test
        void 입금_정상케이스_입금_시_잔액이_증가() {
            // Given
            Long userId = 1L;
            String sessionId = "test-session-id";
            LocalDateTime now = LocalDateTime.of(2025, 7, 1, 0, 0);
            BigDecimal initBalance = BigDecimal.valueOf(10000);
            BigDecimal amount = BigDecimal.valueOf(50000);
            Account mockChecking = createCheckingAccount(initBalance);
            when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.CHECKING)).thenReturn(Optional.of(mockChecking));


            // When
            TransactionLog result = sut.credit(userId, sessionId, now, amount, AccountType.CHECKING, TransactionDetailType.DEPOSIT, "입금 테스트", "test-NPC", "테스트 입금");
            // Then
            assertThat(mockChecking.getBalance()).isEqualByComparingTo("60000");
            assertThat(result).isNotNull();
            assertThat(result.balanceBefore()).isEqualByComparingTo(initBalance);
            assertThat(result.balanceAfter()).isEqualByComparingTo(mockChecking.getBalance());
        }

        @Test
        void 입금_실패케이스_잘못된_amount일_경우() {
            // Given
            Long userId = 1L;
            String sessionId = "test-session-id";
            LocalDateTime now = LocalDateTime.of(2025, 7, 1, 0, 0);

            // When & Then
            assertThatThrownBy(() -> sut.credit(userId, sessionId, now, BigDecimal.ZERO, AccountType.CHECKING, TransactionDetailType.DEPOSIT, "입금 테스트", "test-NPC", "테스트 입금")).isInstanceOf(CoreException.class)
                    .hasMessage("금액이 잘못되었습니다.");

            assertThatThrownBy(() -> sut.credit(userId, sessionId, now, BigDecimal.valueOf(-1), AccountType.CHECKING, TransactionDetailType.DEPOSIT, "입금 테스트", "test-NPC", "테스트 입금")).isInstanceOf(CoreException.class)
                    .hasMessage("금액이 잘못되었습니다.");

            assertThatThrownBy(() -> sut.credit(userId, sessionId, now, null, AccountType.CHECKING, TransactionDetailType.DEPOSIT, "입금 테스트", "test-NPC", "테스트 입금")).isInstanceOf(CoreException.class)
                    .hasMessage("금액이 잘못되었습니다.");
        }
    }

    @Nested
    class DebitTests {

        @Test
        void 출금_정상케이스() {
            // Given
            Long userId = 1L;
            String sessionId = "test-session-id";
            LocalDateTime now = LocalDateTime.of(2025, 7, 1, 0, 0);
            BigDecimal initBalance = BigDecimal.valueOf(100000);
            BigDecimal amount = BigDecimal.valueOf(40000);
            Account mockChecking = createCheckingAccount(initBalance);
            when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.CHECKING)).thenReturn(Optional.of(mockChecking));


            // When
            TransactionLog result = sut.debit(userId, sessionId, now, amount, AccountType.CHECKING, TransactionDetailType.WITHDRAWAL, ChannelType.TRANSFER,"출금 테스트", "test-NPC", "테스트 출금");
            // Then
            assertThat(mockChecking.getBalance()).isEqualByComparingTo("60000");
            assertThat(result).isNotNull();
            assertThat(result.balanceBefore()).isEqualByComparingTo(initBalance);
            assertThat(result.balanceAfter()).isEqualByComparingTo(mockChecking.getBalance());
        }

        @Test
        void 출금_실패케이스_잔액부족일_경우_잔액_변경_없음() {
            // Given
            Long userId = 1L;
            String sessionId = "test-session-id";
            LocalDateTime now = LocalDateTime.of(2025, 7, 1, 0, 0);
            BigDecimal initBalance = BigDecimal.ZERO;
            BigDecimal amount = BigDecimal.valueOf(40000);
            Account mockChecking = createCheckingAccount(initBalance);
            when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.CHECKING)).thenReturn(Optional.of(mockChecking));


            // When & Then
            assertThatThrownBy(() -> sut.debit(userId, sessionId, now, amount, AccountType.CHECKING, TransactionDetailType.WITHDRAWAL, ChannelType.TRANSFER, "출금 테스트", "test-NPC", "테스트 출금"))
                    .isInstanceOf(CoreException.class)
                    .hasMessage("잔액이 부족합니다.");
            assertThat(mockChecking.getBalance()).isEqualByComparingTo(initBalance);
        }
//
//
    }

    @Nested
    class TransferTests {
        //
        @Test
        void 이체_정상케이스() {
            // Given
            Long userId = 1L;
            String sessionId = "test-session-id";
            LocalDateTime now = LocalDateTime.of(2025, 7, 1, 0, 0);
            BigDecimal initBalance = BigDecimal.valueOf(100000);
            BigDecimal transferMoney = BigDecimal.valueOf(50000);
            Account mockChecking = createCheckingAccount(initBalance);
            Account mockSaving = createSavingsAccount(initBalance);
            User mockUser = createUser(mockChecking, mockSaving);
            mockUser.setSessionId(sessionId);

            when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.CHECKING)).thenReturn(Optional.of(mockChecking));
            when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.SAVINGS)).thenReturn(Optional.of(mockSaving));

            // When
            List<TransactionLog> logs = sut.transfer(userId, sessionId, now, transferMoney, AccountType.CHECKING, AccountType.SAVINGS);
            // Then
            assertThat(logs).hasSize(2);
            var out = logs.get(0); // WITHDRAW
            var in = logs.get(1); // DEPOSIT

            // 잔액 검증
            assertThat(mockChecking.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
            assertThat(mockSaving.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150_000));

            // 로그 타입/상세
            assertThat(out.transactionType()).isEqualTo(TransactionType.WITHDRAW);
            assertThat(out.detailType()).isEqualTo(TransactionDetailType.INTERNAL_TRANSFER_OUT);
            assertThat(in.transactionType()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(in.detailType()).isEqualTo(TransactionDetailType.INTERNAL_TRANSFER_IN);

            // balanceBefore / amount / balanceAfter 검증
            assertThat(out.balanceBefore()).isEqualByComparingTo(initBalance);
            assertThat(out.amount()).isEqualByComparingTo(transferMoney);
            assertThat(out.balanceAfter()).isEqualByComparingTo(initBalance.subtract(transferMoney));

            assertThat(in.balanceBefore()).isEqualByComparingTo(initBalance);
            assertThat(in.amount()).isEqualByComparingTo(transferMoney);
            assertThat(in.balanceAfter()).isEqualByComparingTo(initBalance.add(transferMoney));

        }

        @Test
        void 이체_실패케이스_출금계좌_잔액부족() {
            // Given
            Long userId = 1L;
            String sessionId = "test-session-id";
            LocalDateTime now = LocalDateTime.of(2025, 7, 1, 0, 0);
            BigDecimal initBalance = BigDecimal.valueOf(100000);
            BigDecimal transferMoney = BigDecimal.valueOf(150000);
            Account mockChecking = createCheckingAccount(initBalance);
            Account mockSaving = createSavingsAccount(initBalance);
            User mockUser = createUser(mockChecking, mockSaving);
            mockUser.setSessionId(sessionId);

            when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.CHECKING)).thenReturn(Optional.of(mockChecking));
            when(accountRepository.findAccountByUser_IdAndType(userId, AccountType.SAVINGS)).thenReturn(Optional.of(mockSaving));

            // When
            assertThatThrownBy(() -> sut.transfer(
                    userId, sessionId, now, transferMoney,
                    AccountType.CHECKING, AccountType.SAVINGS
            )).isInstanceOf(CoreException.class)
                    .hasMessage("잔액이 부족합니다.");

            // 잔액 변화 없음
            assertThat(mockChecking.getBalance()).isEqualByComparingTo(initBalance);
            assertThat(mockSaving.getBalance()).isEqualByComparingTo(initBalance);
        }
    }

    private User createUser(Account check, Account saving) {
        UserBehaviorProfile profile = UserBehaviorProfile.of(PreferenceType.DEFAULT, WageType.DAILY, getIncomeValue(), getAssetValue(), BigDecimal.ZERO, 1);
        User mockUser = User.of("test-name", profile, 8, 1, Gender.M, 1, "TEST-OCCUPATION", 1, List.of(check, saving));
        mockUser.setSessionId("TEST-sessionId");
        return mockUser;
    }

    private BigDecimal getIncomeValue() {
        return new BigDecimal("5500000");
    }

    private BigDecimal getAssetValue() {
        return new BigDecimal("15000000");
    }


    private Account createCheckingAccount(BigDecimal balance) {
        return Account.ofChecking(balance);
    }

    private Account createSavingsAccount(BigDecimal savingBalance) {
        return Account.ofSavings(savingBalance, BigDecimal.ZERO);
    }
}