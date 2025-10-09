package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.dto.TransactionLog;
import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.enums.AccountType;
import com.simpaylog.generatorcore.enums.TransactionDetailType;
import com.simpaylog.generatorcore.enums.TransactionType;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountDomainService {

    private final AccountRepository accountRepository;

    public TransactionLog credit(Long userId, String sessionId, LocalDateTime localDateTime, BigDecimal amount, AccountType accountType, TransactionDetailType detailType, String description, String counterparty, String memo) {
        validateAmount(amount);
        Account target = getAccountByType(userId, accountType);

        var balanceBefore = target.getBalance();
        var transactionLog = TransactionLog.of(userId, sessionId, localDateTime, TransactionType.DEPOSIT, detailType, description, counterparty, memo, amount, balanceBefore);
        target.setBalance(transactionLog.balanceAfter());
        return transactionLog;
    }

    public TransactionLog debit(Long userId, String sessionId, LocalDateTime localDateTime, BigDecimal amount, AccountType accountType, TransactionDetailType detailType, String description, String counterparty, String memo) {
        validateAmount(amount);
        Account target = getAccountByType(userId, accountType);
        validateSufficientBalance(target, amount);

        var balanceBefore = target.getBalance();
        var transactionLog = TransactionLog.of(userId, sessionId, localDateTime, TransactionType.WITHDRAW, detailType, description, counterparty, memo, amount, balanceBefore);
        target.setBalance(transactionLog.balanceAfter());
        return transactionLog;
    }

    public List<TransactionLog> transfer(Long userId, String sessionId, LocalDateTime localDateTime, BigDecimal amount, AccountType from, AccountType to, String description, String memo) {
        validateAmount(amount);
        if (from == to) throw new CoreException("동일한 계좌입니다.");

        Account source = getAccountByType(userId, from);
        validateSufficientBalance(source, amount);

        Account dest = getAccountByType(userId, to);

        // 1) 출금(내부이체)
        var out = TransactionLog.of(
                userId, sessionId, localDateTime,
                TransactionType.WITHDRAW,
                TransactionDetailType.INTERNAL_TRANSFER_OUT,
                description + " 출금",
                dest.getUser().getName(),
                memo,
                amount,
                source.getBalance()
        );
        source.setBalance(out.balanceAfter());

        // 2) 입금(내부이체)
        var in = TransactionLog.of(
                userId, sessionId, localDateTime.plusMinutes((long) (Math.random() * 3)),
                TransactionType.DEPOSIT,
                TransactionDetailType.INTERNAL_TRANSFER_IN,
                description + " 입금",
                source.getUser().getName(),
                memo,
                amount,
                dest.getBalance()
        );
        dest.setBalance(in.balanceAfter());

        return List.of(out, in);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new CoreException("금액이 잘못되었습니다.");
        }
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new CoreException("잔액이 부족합니다.");
        }
    }

    private Account getAccountByType(Long userId, AccountType type) {
        return accountRepository.findAccountByUser_IdAndType(userId, type)
                .orElseThrow(() -> new CoreException(String.format("userId: %d %s 계좌 없음", userId, type.getName())));
    }
}
