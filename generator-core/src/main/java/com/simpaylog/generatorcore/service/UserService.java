package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.cache.OccupationLocalCache;
import com.simpaylog.generatorcore.dto.UserGenerationCondition;
import com.simpaylog.generatorcore.dto.analyze.OccupationCodeStat;
import com.simpaylog.generatorcore.dto.analyze.OccupationNameStat;
import com.simpaylog.generatorcore.dto.response.*;
import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.AccountType;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.UserBehaviorProfileRepository;
import com.simpaylog.generatorcore.repository.UserRepository;
import com.simpaylog.generatorcore.repository.redis.RedisPaydayRepository;
import com.simpaylog.generatorcore.repository.redis.RedisSessionRepository;
import com.simpaylog.generatorcore.session.SimulationSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final OccupationLocalCache occupationLocalCache;
    private final UserRepository userRepository;
    private final UserBehaviorProfileRepository userBehaviorProfileRepository;
    private final UserGenerator userGenerator;
    private final RedisPaydayRepository redisPaydayRepository;
    private final RedisSessionRepository redisSessionRepository;

    @Transactional
    public String createUser(List<UserGenerationCondition> userGenerationConditions) {
        String sessionId = UUID.randomUUID().toString();
        List<User> userList = new ArrayList<>();
        for (UserGenerationCondition condition : userGenerationConditions) {
            List<User> generated = generateUser(condition);
            for (User user : generated) {
                user.setSessionId(sessionId);
            }
            userList.addAll(generated);
        }
        redisSessionRepository.save(new SimulationSession(sessionId, LocalDateTime.now()));
        userRepository.saveAll(userList);
        return sessionId;
    }

    @Transactional
    public void deleteUserAll() {
        userRepository.deleteAllUsers();
    }

    @Transactional
    public void deleteUsersBySessionId(String sessionId) {
        getSimulationSessionOrException(sessionId);
        redisSessionRepository.delete(sessionId);
        userRepository.deleteUsersBySessionId(sessionId);
    }

    private List<User> generateUser(UserGenerationCondition userGenerationCondition) {
        return userGenerator.generateUserPool(userGenerationCondition);
    }

    public List<TransactionUserDto> findAllTransactionUserBySessionId(String sessionId) {
        getSimulationSessionOrException(sessionId);
        return userRepository.findAllTransactionUserDtosBySessionId(sessionId);
    }

    public void initPaydayCache(String sessionId, LocalDate from, LocalDate to) {
        List<TransactionUserDto> users = findAllTransactionUserBySessionId(sessionId);
        redisPaydayRepository.init(sessionId, users, from, to);
        for (TransactionUserDto user : users) {
            for (LocalDate cur = LocalDate.of(from.getYear(), from.getMonth(), 1); !cur.isAfter(to); cur = cur.plusMonths(1)) {
                redisPaydayRepository.register(sessionId, user.userId(), YearMonth.from(cur), user.wageType().getStrategy().getPayOutDates(cur));
            }
        }
    }

    // TODO: 거래 로그 생성
    @Transactional
    public boolean withdraw(String sessionId, Long userId, BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new CoreException("금액이 잘못되었습니다.");
        }
        User user = getUserOrException(sessionId, userId);
        Account checking = getAccountByType(user, AccountType.CHECKING);
        Account savings = getAccountByType(user, AccountType.SAVINGS);
        if (checking.getBalance().compareTo(amount) >= 0) {
            checking.setBalance(checking.getBalance().subtract(amount));
            return true;
        }
        // 음수 허용
        if (checking.getBalance().subtract(amount).compareTo(checking.getOverdraftLimit().negate()) >= 0) {
            checking.setBalance(checking.getBalance().subtract(amount));
            return true;
        }
        // 예금 인출 시도
        BigDecimal additionalWithdraw = amount.subtract(checking.getBalance().add(checking.getOverdraftLimit())); // 추가로 필요한 출금액
        if (savings != null && savings.getBalance().compareTo(additionalWithdraw) >= 0) {
            savings.setBalance(savings.getBalance().subtract(additionalWithdraw));
            deposit(sessionId, userId, additionalWithdraw); // 입출금 통장으로 송금
            checking.setBalance(checking.getOverdraftLimit().negate()); // 지출 발생
            return true;
        }
        // 실패
        log.warn("userId={} 잔액 부족: {}원", userId, amount);
        return false;
    }

    public BigDecimal getBalance(String sessionId, Long userId) {
        User user = getUserOrException(sessionId, userId);
        Account checking = getAccountByType(user, AccountType.CHECKING);
        return checking.getBalance();
    }

    public void deposit(String sessionId, Long userId, BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new CoreException("금액이 잘못되었습니다.");
        }
        User user = getUserOrException(sessionId, userId);
        Account checking = getAccountByType(user, AccountType.CHECKING);
        checking.setBalance(checking.getBalance().add(amount));
    }

    public void transferToSavings(String sessionId, Long userId, BigDecimal salary, BigDecimal savingRate) {
        User user = getUserOrException(sessionId, userId);
        Account checking = getAccountByType(user, AccountType.CHECKING);
        Account saving = getAccountByType(user, AccountType.SAVINGS);

        BigDecimal savingAmount = salary.multiply(savingRate).setScale(0, RoundingMode.DOWN); // 1. 이체 금액 계산

        if (checking.getBalance().compareTo(savingAmount) < 0) {
            log.warn("잔액이 부족하여 이체할 수 없습니다.");
            return;
        }
        checking.setBalance(checking.getBalance().subtract(savingAmount)); // 2. 입출금 통장에서 출금
        saving.setBalance(saving.getBalance().add(savingAmount)); // 3. 예금 통장에 입금
    }

    public void applyMonthlyInterest(String sessionId, Long userId) {
        User user = getUserOrException(sessionId, userId);
        Account account = getAccountByType(user, AccountType.SAVINGS);
        BigDecimal principal = account.getBalance();

        BigDecimal monthlyRate = account.getInterestRate()
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal interest = principal
                .multiply(monthlyRate)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN); // 퍼센트로 계산

        account.setBalance(principal.add(interest));
    }

    private Account getAccountByType(User user, AccountType type) {
        return user.getAccounts().stream()
                .filter(a -> a.getType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(type + " 계좌 없음"));
    }

    private User getUserOrException(String sessionId, Long userId) {
        return userRepository.findUserBySessionIdAndId(sessionId, userId)
                .orElseThrow(() -> new CoreException("NOT FOUND USER: " + userId));
    }

    public UserAnalyzeResultResponse analyzeUsers(String sessionId) {
        getSimulationSessionOrException(sessionId);
        return new UserAnalyzeResultResponse(
                userRepository.countUsersBySessionId(sessionId),
                userRepository.analyzeAgeGroup(sessionId),
                occupationCodeToName(userRepository.analyzeOccupation(sessionId)),
                userRepository.analyzeGender(sessionId)
        );
    }

    private List<OccupationNameStat> occupationCodeToName(List<OccupationCodeStat> occupationCodeStats) {
        return occupationCodeStats.stream()
                .map(stat -> new OccupationNameStat(
                        occupationLocalCache.get(stat.occupationCategory()).occupationCategory().substring(2),
                        stat.count()
                ))
                .collect(Collectors.toList());
    }

    public Page<UserInfoResponse> findUsersByPage(Pageable pageable, String sessionId) {
        getSimulationSessionOrException(sessionId);
        return userRepository.findAllBySessionIdOrderByName(pageable, sessionId)
                .map(UserInfoResponse::userToUserInfoResponse);
    }

    public AgeGroupResponse getAgeGroup() {
        return new AgeGroupResponse(Stream.concat(occupationLocalCache.get(1).ageGroupInfo().stream()
                                .map(ageInfo -> new AgeGroupDetailResponse(
                                        String.valueOf(ageInfo.range()[0]),
                                        String.format("%s (%d-%d세)",
                                                ageInfo.label(),
                                                ageInfo.range()[0],
                                                ageInfo.range()[1])
                                )),
                        Stream.of(new AgeGroupDetailResponse("MIX", "혼합")))
                .collect(Collectors.toList()));
    }

    public OccupationListResponse getOccupationCategory() {
        return new OccupationListResponse(Stream.concat(
                        occupationLocalCache.getCache().values().stream()
                                .map(occupation -> new OccupationCategoryResponse(
                                        String.valueOf(occupation.code()),
                                        occupation.occupationCategory().substring(2)
                                )),
                        Stream.of(new OccupationCategoryResponse("MIX", "혼합")))
                .collect(Collectors.toList())
        );
    }

    public PreferenceListResponse getPreferenceList() {
        List<PreferenceResponse> preferences = Stream.concat(
                Arrays.stream(PreferenceType.values())
                        .map(type -> new PreferenceResponse(
                                String.valueOf(type.getKey()),
                                type.getName()
                        )),
                Stream.of(new PreferenceResponse("MIX", "혼합"))
        ).collect(Collectors.toList());

        return new PreferenceListResponse(preferences);
    }

    public SimulationSession getSimulationSessionOrException(String sessionId) {
        return redisSessionRepository.find(sessionId).orElseThrow(() -> new CoreException(String.format("해당 sessionId를 찾을 수 없습니다. sessionId: %s", sessionId)));
    }
}
