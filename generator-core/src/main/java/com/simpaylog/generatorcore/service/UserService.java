package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.cache.OccupationLocalCache;
import com.simpaylog.generatorcore.cache.PreferenceLocalCache;
import com.simpaylog.generatorcore.dto.UserGenerationCondition;
import com.simpaylog.generatorcore.dto.UserInfoDto;
import com.simpaylog.generatorcore.dto.analyze.OccupationCodeStat;
import com.simpaylog.generatorcore.dto.analyze.OccupationNameStat;
import com.simpaylog.generatorcore.dto.response.*;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.UserBehaviorProfileRepository;
import com.simpaylog.generatorcore.repository.UserRepository;
import com.simpaylog.generatorcore.repository.redis.RedisPaydayRepository;
import com.simpaylog.generatorcore.repository.redis.RedisSessionRepository;
import com.simpaylog.generatorcore.session.SimulationSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserService {
    private final OccupationLocalCache occupationLocalCache;
    private final PreferenceLocalCache preferenceLocalCache;
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
        userBehaviorProfileRepository.deleteAllProfiles();
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

    // TODO 유저 및 유저 프로필 반환 메서드 필요

    @Transactional
    public void updateUserBalance(String sessionId, Long userId, BigDecimal balance) {
        User user = userRepository.findUserBySessionIdAndId(sessionId, userId).orElseThrow(() -> new CoreException(String.format("NOT FOUND USER: %d", userId)));
        user.setBalance(balance);
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

    /**
     * 소비 성향 ID에 해당하는 이름을 반환받아 새로운 객체 생성하고 반환합니다.
     *
     * @param users : DB에 저장된 user 정보입니다.
     * @return preferenceLocalCache에서 ID에 해당하는 소비성향을 찾아 새 객체를 만들어 반환합니다.
     */
    List<UserInfoResponse> preferenceIdToType(List<UserInfoDto> users) {
        return users.stream()
                .map(user -> new UserInfoResponse(
                        user.name(),
                        user.gender(),
                        user.age(),
                        preferenceLocalCache.get(user.preferenceId()).name(),
                        user.occupationName()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 직업 ID에 해당하는 이름을 반환받아 새로운 객체 생성하고 반환합니다.
     *
     * @param occupationCodeStats : DB에 저장된 occupation 정보입니다.
     * @return occupationLocalCache에서 ID에 해당하는 직업명을 찾아 새 객체를 만들어 반환합니다.
     */
    List<OccupationNameStat> occupationCodeToName(List<OccupationCodeStat> occupationCodeStats) {
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
                .map(user -> UserInfoResponse.userToUserInfoResponse(
                        user,
                        preferenceLocalCache.get(user.getUserBehaviorProfile().getPreferenceType()).name()
                ));
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
        return new PreferenceListResponse(Stream.concat(
                        preferenceLocalCache.getCache().values().stream()
                                .map(preferenceInfo -> new PreferenceResponse(
                                        String.valueOf(preferenceInfo.id()),
                                        preferenceInfo.name()
                                )),
                        Stream.of(new PreferenceResponse("MIX", "혼합")))
                .collect(Collectors.toList())
        );
    }

    public SimulationSession getSimulationSessionOrException(String sessionId) {
        return redisSessionRepository.find(sessionId).orElseThrow(() -> new CoreException(String.format("해당 sessionId를 찾을 수 없습니다. sessionId: %s", sessionId)));
    }
}
