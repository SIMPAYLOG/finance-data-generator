package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.enums.AccountType;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    // TODO: 거래 로그 생성
    @Transactional
    public boolean withdraw(Long userId, BigDecimal amount, LocalDateTime dateTime) {
        if (amount == null || amount.signum() < 0) {
            throw new CoreException("금액이 잘못되었습니다.");
        }
        Account checking = getAccountByType(userId, AccountType.CHECKING);
        Account savings = getAccountByType(userId, AccountType.SAVINGS);
        if (checking.getBalance().compareTo(amount) >= 0) {
            checking.setBalance(checking.getBalance().subtract(amount));
//            log.info("userId={} [출금][{}][{}] 출금 금액: {}, 현재 잔액: {}", userId, checking.getType().getName(), dateTime, amount, checking.getBalance());
            return true;
        }
        // 음수 허용
        if (checking.getBalance().subtract(amount).compareTo(checking.getOverdraftLimit().negate()) >= 0) {
            checking.setBalance(checking.getBalance().subtract(amount));
//            log.info("userId={} [마이너스 출금][{}][{}] 출금 금액: {}, 현재 잔액: {}", userId, checking.getType().getName(), dateTime, amount, checking.getBalance());
            return true;
        }
        // 예금 인출 시도
        BigDecimal additionalWithdraw = amount.subtract(checking.getBalance().add(checking.getOverdraftLimit())); // 추가로 필요한 출금액
        if (savings != null && savings.getBalance().compareTo(additionalWithdraw) >= 0) {
            savings.setBalance(savings.getBalance().subtract(additionalWithdraw));
//            log.info("userId={} [송금][{}][{}] 출금 금액: {}, 현재 잔액: {}", userId, savings.getType().getName(), dateTime, amount, savings.getBalance());
            deposit(userId, additionalWithdraw, dateTime); // 입출금 통장으로 송금
            checking.setBalance(checking.getOverdraftLimit().negate()); // 지출 발생
//            log.info("userId={} [마이너스 출금][{}][{}] 출금 금액: {}, 현재 잔액: {}", userId, checking.getType().getName(), dateTime, amount, checking.getBalance());
            return true;
        }
        // 실패
        log.warn("userId={} 잔액 부족: {}원", userId, amount);
        return false;
    }

    public void deposit(Long userId, BigDecimal amount, LocalDateTime dateTime) {
        if (amount == null || amount.signum() < 0) {
            throw new CoreException("금액이 잘못되었습니다.");
        }
        Account checking = getAccountByType(userId, AccountType.CHECKING);
        checking.setBalance(checking.getBalance().add(amount));
//        log.info("userId={} [입금][{}][{}] 입금 금액: {}, 현재 잔액: {}", userId, checking.getType().getName(), dateTime, amount, checking.getBalance());
    }

    public void transferToSavings(Long userId, BigDecimal salary, BigDecimal savingRate, LocalDateTime dateTime) {
        Account checking = getAccountByType(userId, AccountType.CHECKING);
        Account saving = getAccountByType(userId, AccountType.SAVINGS);

        BigDecimal savingAmount = salary.multiply(savingRate).setScale(0, RoundingMode.DOWN); // 1. 이체 금액 계산

        if (checking.getBalance().compareTo(savingAmount) < 0) {
            log.warn("잔액이 부족하여 이체할 수 없습니다. 저축할 금액: {}, 현재 잔액: {}", savingAmount, checking.getBalance());
            return;
        }
        checking.setBalance(checking.getBalance().subtract(savingAmount)); // 2. 입출금 통장에서 출금
//        log.info("userId={} [예금 통장으로 이체][{}][{}] 출금 금액: {}, 현재 잔액: {}", userId, checking.getType().getName(), dateTime, savingAmount, checking.getBalance());
        saving.setBalance(saving.getBalance().add(savingAmount)); // 3. 예금 통장에 입금
//        log.info("userId={} [저축][{}][{}] 입금 금액: {}, 현재 잔액: {}", userId, saving.getType().getName(), dateTime, savingAmount, saving.getBalance());
    }

    public void applyMonthlyInterest(Long userId, LocalDateTime dateTime) {
        Account account = getAccountByType(userId, AccountType.SAVINGS);
        BigDecimal principal = account.getBalance();

        BigDecimal monthlyRate = account.getInterestRate()
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal interest = principal
                .multiply(monthlyRate)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN); // 퍼센트로 계산

        account.setBalance(principal.add(interest));
//        log.info("userId={} [입금-이자발생][{}][{}] 출금 금액: {}, 현재 잔액: {}", userId, account.getType().getName(), dateTime, interest, account.getBalance());
    }

    public Account getAccountByType(Long userId, AccountType type) {
        return accountRepository.findAccountByUser_IdAndType(userId, type)
                .orElseThrow(() -> new CoreException(String.format("userId: %d %s 계좌 없음", userId, type.getName())));
    }

}
