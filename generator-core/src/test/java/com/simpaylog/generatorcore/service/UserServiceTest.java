package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.TestConfig;
import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.UserBehaviorProfile;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.AccountType;
import com.simpaylog.generatorcore.enums.Gender;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.UserRepository;
import com.simpaylog.generatorcore.repository.redis.RedisPaydayRepository;
import com.simpaylog.generatorcore.repository.redis.RedisSessionRepository;
import com.simpaylog.generatorcore.session.SimulationSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTest extends TestConfig {
    @Autowired
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private RedisSessionRepository redisSessionRepository;
    @MockitoBean
    private RedisPaydayRepository redisPaydayRepository;

    @Test
    void 세션아이디가_주어지고_유저_삭제시_유저_유저프로필_계좌정보_전체삭제() {
        // Given
        String sessionId = "test-sessionId";
        when(redisSessionRepository.find(eq(sessionId))).thenReturn(Optional.of(new SimulationSession(sessionId, LocalDateTime.now())));
        // When
        userService.deleteUsersBySessionId(sessionId);
        // Then
        verify(redisSessionRepository, times(1)).delete(eq(sessionId));
        verify(userRepository, times(1)).deleteUsersBySessionId(eq(sessionId));
    }

    @Test
    void 기간이_3달이라면_유저별_월급일정_등록을_3번한다() {
        // Given
        String sessionId = "test-sessionId";
        List<TransactionUserDto> mockList = List.of(createTransactionUserDto(), createTransactionUserDto());
        LocalDate from = LocalDate.of(2025, 7, 1);
        LocalDate to = LocalDate.of(2025, 9, 30);
        int months = Math.toIntExact(ChronoUnit.MONTHS.between(from, to) + 1);
        System.out.println(months);
        when(redisSessionRepository.find(eq(sessionId))).thenReturn(Optional.of(new SimulationSession(sessionId, LocalDateTime.now())));
        when(userRepository.findAllTransactionUserDtosBySessionId(eq(sessionId))).thenReturn(mockList);
        // When
        userService.initPaydayCache(sessionId, from, to);
        // Then
        verify(redisPaydayRepository, times(1)).init(sessionId, mockList, from, to);
        verify(redisPaydayRepository, times(months * mockList.size())).register(anyString(), anyLong(), any(), anyList());
    }


    @Test
    void 인출_정상케이스() {
        // Given
        String sessionId = "test-sessionId";
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(50000);
        List<Account> accounts = createUserAccount(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000), BigDecimal.ZERO);
        Account check = accounts.stream().filter(a -> a.getType() == AccountType.CHECKING).findFirst().orElse(null);
        User mockUser = createUser(accounts);
        when(userRepository.findUserBySessionIdAndId(sessionId, userId)).thenReturn(Optional.of(mockUser));
        // When
        boolean result = userService.withdraw(sessionId, userId, amount);
        // Then
        assertTrue(result);
        assertNotNull(check);
        assertEquals(BigDecimal.valueOf(50000), check.getBalance());
    }

    @Test
    void 인출_마이너스금액이지만_한도_내이므로_정상케이스() {
        // Given
        String sessionId = "test-sessionId";
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(130000);
        List<Account> accounts = createUserAccount(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000), BigDecimal.ZERO);
        Account check = accounts.stream().filter(a -> a.getType() == AccountType.CHECKING).findFirst().orElse(null);
        User mockUser = createUser(accounts);
        when(userRepository.findUserBySessionIdAndId(sessionId, userId)).thenReturn(Optional.of(mockUser));
        // When
        boolean result = userService.withdraw(sessionId, userId, amount);
        // Then
        assertTrue(result);
        assertNotNull(check);
        assertEquals(BigDecimal.valueOf(-30000), check.getBalance());
    }

    @Test
    void 인출_음수의_금액이_넘어왔을_때_에러반환() {
        // Given
        String sessionId = "test-sessionId";
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(-50000);
        // When & Then
        assertThatThrownBy(() -> userService.withdraw(sessionId, userId, amount))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("금액이 잘못되었습니다.");
    }

    @Test
    void 인출_할때_한도를_넘어가면_false반환() {
        // Given
        String sessionId = "test-sessionId";
        Long userId = 1L;
        BigDecimal originBalance = BigDecimal.valueOf(100000);
        BigDecimal amount = BigDecimal.valueOf(160000);
        List<Account> accounts = createUserAccount(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000), BigDecimal.ZERO);
        Account check = accounts.stream().filter(a -> a.getType() == AccountType.CHECKING).findFirst().orElse(null);
        User mockUser = createUser(accounts);
        when(userRepository.findUserBySessionIdAndId(sessionId, userId)).thenReturn(Optional.of(mockUser));
        // When
        boolean result = userService.withdraw(sessionId, userId, amount);
        // Then
        assertFalse(result);
        assertNotNull(check);
        assertEquals(originBalance, check.getBalance());
    }

    @Test
    void 인출_할때_돈이_모자르면_입출금통장에서_송금_후_인출() {
        // Given
        String sessionId = "test-sessionId";
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(160000); // 결제 금액
        BigDecimal expectedCheckingAmount = BigDecimal.valueOf(-50000);
        BigDecimal expectedSavingAmount = BigDecimal.valueOf(20000);

        List<Account> accounts = createUserAccount(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000), BigDecimal.valueOf(30000));
        Account check = accounts.stream().filter(a -> a.getType() == AccountType.CHECKING).findFirst().orElse(null);
        Account saving = accounts.stream().filter(a -> a.getType() == AccountType.SAVINGS).findFirst().orElse(null);
        User mockUser = createUser(accounts);
        when(userRepository.findUserBySessionIdAndId(sessionId, userId)).thenReturn(Optional.of(mockUser));
        // When
        boolean result = userService.withdraw(sessionId, userId, amount);
        // Then
        assertTrue(result);
        assertNotNull(check);
        assertNotNull(saving);
        assertEquals(expectedCheckingAmount, check.getBalance());
        assertEquals(expectedSavingAmount, saving.getBalance());
    }


    public static TransactionUserDto createTransactionUserDto() {
        return new TransactionUserDto(
                1L,
                "test-sessionId",
                1,
                PreferenceType.DEFAULT,
                WageType.REGULAR,
                10,
                "TEST-active-hour",
                BigDecimal.valueOf(3000000),
                BigDecimal.ZERO
        );
    }

    private User createUser(List<Account> accounts) {
        PreferenceType preferenceType = PreferenceType.DEFAULT;
        UserBehaviorProfile profile = UserBehaviorProfile.of(preferenceType, WageType.DAILY, 25, getIncomeValue(), getAssetValue(), BigDecimal.ZERO);
        User mockUser = User.of("test-name", profile, 8, 10, Gender.M, 1, "TEST-OCCUPATION", 1, accounts);
        mockUser.setSessionId("TEST-sessionId");
        return mockUser;
    }

    private List<Account> createUserAccount(BigDecimal balance, BigDecimal overDraftLimit, BigDecimal savingBalance) {
        List<Account> accounts = new ArrayList<>();
        accounts.add(Account.ofChecking(balance, overDraftLimit));
        accounts.add(Account.ofSavings(savingBalance, BigDecimal.ZERO)); // 계좌 잔고 없음
        return accounts;
    }

    private BigDecimal getIncomeValue() {
        return new BigDecimal("5500000");
    }

    private BigDecimal getAssetValue() {
        return new BigDecimal("15000000");
    }

}