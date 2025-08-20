package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.cache.OccupationLocalCache;
import com.simpaylog.generatorcore.dto.FixedObligation;
import com.simpaylog.generatorcore.dto.UserGenerationCondition;
import com.simpaylog.generatorcore.dto.UserProfile;
import com.simpaylog.generatorcore.dto.analyze.OccupationNameStat;
import com.simpaylog.generatorcore.dto.response.*;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.UserRepository;
import com.simpaylog.generatorcore.repository.redis.FixedObligationRepository;
import com.simpaylog.generatorcore.repository.redis.RedisPaydayRepository;
import com.simpaylog.generatorcore.repository.redis.RedisSessionRepository;
import com.simpaylog.generatorcore.session.SimulationSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserGenerator userGenerator;
    private final FixedObligationPresetProvider fixedObligationPresetProvider;
    private final RedisPaydayRepository redisPaydayRepository;
    private final RedisSessionRepository redisSessionRepository;
    private final FixedObligationRepository fixedObligationRepository;

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

    public void initFixedObligation(String sessionId, LocalDate from, LocalDate to) {
        List<TransactionUserDto> users = findAllTransactionUserBySessionId(sessionId);
        for (TransactionUserDto user : users) {
            List<FixedObligation> fixedObligations = fixedObligationPresetProvider.generate(new UserProfile(user.userId(), user.decile(), user.preferenceType(), user.age()), from);
            fixedObligationRepository.saveAll(sessionId, user.userId(), fixedObligations);
        }
    }

    //생성된 유저의 성별 비율, 직업군 비율, 연령대 비율을 반환해주는 메소드
    public UserAnalyzeResultResponse analyzeUsers(String sessionId) {
        getSimulationSessionOrException(sessionId);
        return new UserAnalyzeResultResponse(
                userRepository.countUsersBySessionId(sessionId),
                userRepository.analyzeAgeGroup(sessionId),
                userRepository.analyzeOccupation(sessionId).stream()
                        .map(stat -> new OccupationNameStat(
                                occupationLocalCache.get(stat.occupationCategory()).occupationCategory().substring(2),
                                stat.count()
                        ))
                        .collect(Collectors.toList()),
                userRepository.analyzeGender(sessionId)
        );
    }

    public Page<UserInfoResponse> findUsersByPage(Pageable pageable, String sessionId) {
        getSimulationSessionOrException(sessionId);
        return userRepository.findAllBySessionIdOrderByName(pageable, sessionId)
                .map(UserInfoResponse::userToUserInfoResponse);
    }

    // 클라이언트가 조건 생성시에 나이대로 선택할 수 있는 리스트를 생성해주는 메소드
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

    // 클라이언트가 조건 생성시에 직업군으로 선택할 수 있는 리스트를 생성해주는 메소드
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

    // 클라이언트가 조건 생성시에 소비성향으로 선택할 수 있는 리스트를 생성해주는 메소드
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

    //원하는 나이대의 아이디 값을 불러오는 메소드 (ageGroup이 10일 때, 10대의 userId 리스트를 반환)
    public List<Long> getIdsByAgeGroup(int ageGroup, String sessionId) {
        List<Long> userIds = userRepository.findUserIdsByAgeGroup(ageGroup, sessionId);

        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userIds;
    }
}
