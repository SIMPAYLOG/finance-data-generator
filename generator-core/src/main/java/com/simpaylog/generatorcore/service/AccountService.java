package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.dto.TransactionLog;
import com.simpaylog.generatorcore.dto.TransactionResult;
import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.enums.AccountType;
import com.simpaylog.generatorcore.enums.TransactionDetailType;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountDomainService accountDomainService;
    private final AccountRepository accountRepository;

    /* 결제 */
    // [거래]입출금 통장 -> 결제 요청
    public TransactionResult spendCard(Long userId, String sessionId, LocalDateTime localDateTime,
                                       BigDecimal amount, String merchant, String memo) {
        var logs = new ArrayList<TransactionLog>();

        try {
            // 1) 바로 출금 시도 (CHECKING)
            var out = accountDomainService.debit(userId, sessionId, localDateTime, amount, AccountType.CHECKING,
                    TransactionDetailType.CARD_PAYMENT, "체크카드 결제 - " + merchant, merchant, memo);
            logs.add(out);
            return TransactionResult.ok(logs);

        } catch (CoreException e) {
            if (!"INSUFFICIENT_FUNDS".equals(e.getMessage())) return TransactionResult.fail(e.getMessage());

            // 2) 부족하면 SAVINGS → CHECKING 내부이체로 보충 후 다시 결제
            BigDecimal checkingBalance = getBalance(userId, AccountType.CHECKING);
            BigDecimal deficit = amount.subtract(checkingBalance);
            if (getBalance(userId, AccountType.SAVINGS).compareTo(deficit) < 0) {
                return TransactionResult.fail("INSUFFICIENT_FUNDS");
            }

            logs.addAll(accountDomainService.transfer(userId, sessionId, localDateTime, deficit,
                    AccountType.SAVINGS, AccountType.CHECKING, "세이빙 스윕(부족분 충당)", "자동 충당"));

            var out = accountDomainService.debit(userId, sessionId, localDateTime, amount, AccountType.CHECKING,
                     TransactionDetailType.CARD_PAYMENT, "체크카드 결제 - " + merchant, merchant, memo);
            logs.add(out);

            return TransactionResult.ok(logs);
        }
    }
    // [자동이체]
    public TransactionResult paySubscription(Long userId, String sessionId, LocalDateTime ts,
                                             BigDecimal amount, String biller, String memo) {
        try {
            var out = accountDomainService.debit(userId, sessionId, ts, amount, AccountType.CHECKING,
                    TransactionDetailType.AUTO_PAYMENT, "자동이체 - " + biller, biller, memo);
            return TransactionResult.ok(out);
        } catch (CoreException e) {
            return TransactionResult.fail(e.getMessage());
        }
    }
    // 이자
    private BigDecimal getBalance(Long userId, AccountType accountType) {
        return getAccountByType(userId, accountType).getBalance();
    }

    public TransactionResult receiveDeposit(Long userId, String sessionId, LocalDateTime localDateTime,
                                            BigDecimal amount, String sender, String memo) {
        var in = accountDomainService.credit(
                userId, sessionId, localDateTime, amount, AccountType.CHECKING
                , TransactionDetailType.DEPOSIT,"계좌이체입금 - " + sender, sender, memo
        );
        return TransactionResult.ok(in);
    }

    public TransactionResult receivePayroll(Long userId, String sessionId, LocalDateTime localDateTime,
                                            BigDecimal amount, String counterparty, String memo) {
        var in = accountDomainService.credit(
                userId, sessionId, localDateTime, amount, AccountType.CHECKING,
                TransactionDetailType.DEPOSIT,"급여이체" ,counterparty, memo
        );
        return TransactionResult.ok(in);
    }

    public TransactionResult applyMonthlyInterest(Long userId, String sessionId, LocalDateTime localDateTime) {
        Account savings = getAccountByType(userId, AccountType.SAVINGS);
        BigDecimal principal = savings.getBalance();
        BigDecimal monthlyRate = savings.getInterestRate().divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal interest = principal
                .multiply(monthlyRate)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN); // 퍼센트로 계산
        var in = accountDomainService.credit(userId, sessionId, localDateTime, interest, AccountType.SAVINGS,
                TransactionDetailType.INTEREST, "월 이자지급(세전)", null, null);
        return TransactionResult.ok(in);
    }


    // 입출금 -> 저축
    public TransactionResult moveToSavings(Long userId, String sessionId, LocalDateTime localDateTime, BigDecimal amount, String memo) {
        try {
            var logs = accountDomainService.transfer(userId, sessionId, localDateTime, amount, AccountType.CHECKING, AccountType.SAVINGS, "저축 이체(입출금→저축)", memo);
            return TransactionResult.ok(logs);
        } catch(CoreException e) {
            return TransactionResult.fail(e.getMessage());
        }
    }

    public Account getAccountByType(Long userId, AccountType type) {
        return accountRepository.findAccountByUser_IdAndType(userId, type)
                .orElseThrow(() -> new CoreException(String.format("userId: %d %s 계좌 없음", userId, type.getName())));
    }

}
