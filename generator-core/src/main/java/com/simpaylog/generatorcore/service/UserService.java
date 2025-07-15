package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.cache.OccupationLocalCache;
import com.simpaylog.generatorcore.cache.PreferenceLocalCache;
import com.simpaylog.generatorcore.dto.analyze.OccupationCodeStat;
import com.simpaylog.generatorcore.dto.analyze.OccupationNameStat;
import com.simpaylog.generatorcore.dto.response.*;
import com.simpaylog.generatorcore.dto.*;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.repository.UserBehaviorProfileRepository;
import com.simpaylog.generatorcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final OccupationLocalCache occupationLocalCache;
    private final PreferenceLocalCache preferenceLocalCache;
    private final UserRepository userRepository;
    private final UserBehaviorProfileRepository userBehaviorProfileRepository;
    private final UserGenerator userGenerator;

    @Transactional
    public void createUser(List<UserGenerationCondition> userGenerationConditions) {
        List<User> userList = new ArrayList<>();
        for (UserGenerationCondition condition : userGenerationConditions) {
            userList.addAll(generateUser(condition));
        }
        userRepository.saveAll(userList);
    }

    @Transactional
    public void deleteUserAll() {
        userBehaviorProfileRepository.deleteAllProfiles();
        userRepository.deleteAllUsers();
    }

    private List<User> generateUser(UserGenerationCondition userGenerationCondition) {
        return userGenerator.generateUserPool(userGenerationCondition);
    }

    public UserAnalyzeResultResponse analyzeUsers() {
        List<UserInfoResponse> users = perferenceIdToType(userRepository.findAllSimpleInfo());

        UserAnalyzeResultResponse result = new UserAnalyzeResultResponse(
                userRepository.count(),
                userRepository.analyzeAgeGroup(),
                occupationCodeToName(userRepository.analyzeOccupation()),
                userRepository.analyzeGender()
        );

        return result;
    }

    /**
     * 소비 성향 ID에 해당하는 이름을 반환받아 새로운 객체 생성하고 반환합니다.
     *
     * @param users : DB에 저장된 user 정보입니다.
     * @return preferenceLocalCache에서 ID에 해당하는 소비성향을 찾아 새 객체를 만들어 반환합니다.
     */
    List<UserInfoResponse> perferenceIdToType(List<UserInfoDto> users) {
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
                        occupationLocalCache.get(stat.occupationCategory()).occupationCategory(),
                        stat.count()
                ))
                .collect(Collectors.toList());
    }

    public Page<UserInfoResponse> findUsersByPage(Pageable pageable) {
        return userRepository.findAllByOrderByNameAsc(pageable)
                .map(user -> UserInfoResponse.userToUserInfoResponse(
                        user,
                        preferenceLocalCache.get(user.getUserBehaviorProfile().getPreferenceId()).name()
                ));
    }

    public AgeGroupResponse getAgeGroup() {
        return new AgeGroupResponse(occupationLocalCache.get(1).ageGroupInfo().stream()
                .map(ageInfo -> {
                    return new AgeGroupDetailResponse(
                            String.valueOf(ageInfo.range()[0]),
                            String.format("%s (%d-%d세)",
                                    ageInfo.label(),
                                    ageInfo.range()[0],
                                    ageInfo.range()[1])
                    );
                })
                .collect(Collectors.toList()));
    }

    public OccupationListResponse getOccupationCategory() {
        return new OccupationListResponse(occupationLocalCache.getCache().values().stream()
                .map(occupation -> new OccupationCategoryResponse(
                        String.valueOf(occupation.code()),
                        occupation.occupationCategory().substring(2)
                ))
                .collect(Collectors.toList())
        );
    }

    public PreferenceListResponse getPreferenceList() {
        return new PreferenceListResponse(preferenceLocalCache.getCache().values().stream()
                .map(preferenceInfo -> new PreferenceResponse(
                        String.valueOf(preferenceInfo.id()),
                        preferenceInfo.name()
                ))
                .collect(Collectors.toList())
        );
    }
}
