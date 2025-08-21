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

    public static TransactionUserDto createTransactionUserDto() {
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