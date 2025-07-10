package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.cache.OccupationLocalCache;
import com.simpaylog.generatorcore.cache.PreferenceLocalCache;
import com.simpaylog.generatorcore.dto.analyze.OccupationCodeStat;
import com.simpaylog.generatorcore.dto.analyze.OccupationNameStat;
import com.simpaylog.generatorcore.dto.response.UserAnalyzeResultResponse;
import com.simpaylog.generatorcore.dto.*;
import com.simpaylog.generatorcore.dto.response.UserInfoResponse;
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
        for(UserGenerationCondition condition : userGenerationConditions) {
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
        List<UserInfoResponse> users = perferenceIdtoType(userRepository.findAllSimpleInfo());

        UserAnalyzeResultResponse result = new UserAnalyzeResultResponse(
                userRepository.count(),
                userRepository.analyzeAgeGroup(),
                occupationCodeToName(userRepository.analyzeOccupation()),
                userRepository.analyzeGender()
        );

        return result;
    }

    List<UserInfoResponse> perferenceIdtoType(List<UserInfoDto> users) {
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

    List<OccupationNameStat> occupationCodeToName(List<OccupationCodeStat> occupationCodeStats) {
        return occupationCodeStats.stream()
                .map(stat -> new OccupationNameStat(
                        occupationLocalCache.get(stat.occupationCategory()).occupationCategory(),
                        stat.count()
                ))
                .collect(Collectors.toList());
    }

    public Page<UserInfoResponse> findUsersByPage(Pageable pageable) {
        return userRepository.findAllByOrderByNameAsc(pageable).map(user -> UserInfoResponse.userToUserInfoResponse(
                user,
                preferenceLocalCache.get(user.getUserBehaviorProfile().getPreferenceId()).name()
        ));
    }
}
