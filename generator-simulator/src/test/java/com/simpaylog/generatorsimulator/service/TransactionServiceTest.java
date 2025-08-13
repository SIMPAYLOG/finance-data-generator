package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.dto.TransactionLog;
import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.AccountType;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.enums.WageType;
import com.simpaylog.generatorcore.repository.redis.RedisPaydayRepository;
import com.simpaylog.generatorcore.service.AccountService;
import com.simpaylog.generatorsimulator.TestConfig;
import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorsimulator.kafka.producer.DailyTransactionResultProducer;
import com.simpaylog.generatorsimulator.kafka.producer.TransactionLogProducer;
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
    AccountService accountService;
    @MockitoBean
    RedisPaydayRepository redisPaydayRepository;

    @Test
    void 트랜잭션이_정상적으로_생성되면_총합과_카운트가_일치한다() {
        // Given
        TransactionUserDto mockUser = mockUser(3, WageType.REGULAR);
        LocalDate date = LocalDate.of(2025, 7, 1);
        when(accountService.getAccountByType(mockUser.userId(), AccountType.CHECKING)).thenReturn(createCheckingAccount(BigDecimal.valueOf(50000), BigDecimal.ZERO));
        when(transactionGenerator.pickOneCategory(any(LocalDateTime.class), any(PreferenceType.class), anyMap())).thenReturn(Optional.of(CategoryType.ALCOHOLIC_BEVERAGES_TOBACCO));
        when(accountService.withdraw(anyLong(), any(BigDecimal.class), any())).thenReturn(true);
        // When
        transactionService.generateTransaction(mockUser, date);

        // Then
        verify(transactionLogProducer, atLeastOnce()).send(any());
        verify(dailyTransactionResultProducer, times(1)).send(any());
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

    private TransactionUserDto mockUser(int decile, WageType wageType) {
        return new TransactionUserDto(
                1L,
                "test-sessionId",
                decile,
                PreferenceType.DEFAULT,
                wageType,
                10,
                "TEST-active-hour",
                BigDecimal.valueOf(3000000),
                BigDecimal.ZERO
        );
    }
    private Account createCheckingAccount(BigDecimal balance, BigDecimal overDraftLimit) {
        return Account.ofChecking(balance, overDraftLimit);
    }


}