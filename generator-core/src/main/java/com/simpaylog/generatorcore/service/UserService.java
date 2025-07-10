package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.cache.OccupationLocalCache;
import com.simpaylog.generatorcore.cache.PreferenceLocalCache;
import com.simpaylog.generatorcore.dto.response.UserAnalyzeResultResponse;
import com.simpaylog.generatorcore.dto.*;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.repository.UserBehaviorProfileRepository;
import com.simpaylog.generatorcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void createUser(List<User> userList) {
        userRepository.saveAll(userList);
    }

    @Transactional
    public void deleteUserAll() {
        userBehaviorProfileRepository.deleteAllProfiles();
        userRepository.deleteAllUsers();
    }

    @Transactional(readOnly = true)
    public List<User> selectUserAll() {
        return userRepository.findAll();
    }

    public List<User> generateUser(UserGenerationCondition userGenerationCondition) {
        return userGenerator.generateUserPool(userGenerationCondition);
    }

    public UserAnalyzeResultResponse analyzeUsers() {
        List<UserSimpleTypeInfo> users = perferenceIdtoType(userRepository.findAllSimpleInfo());

        UserAnalyzeResultResponse result = new UserAnalyzeResultResponse(
                userRepository.count(),
                userRepository.analyzeAgeGroup(),
                occupationCodeToName(userRepository.analyzeOccupation()),
                userRepository.analyzeGender()
        );

        return result;
    }

    List<UserSimpleTypeInfo> perferenceIdtoType(List<UserSimpleIdInfo> users) {
        return users.stream()
                .map(user -> new UserSimpleTypeInfo(
                        user.name(),
                        user.gender(),
                        user.age(),
                        preferenceLocalCache.get(user.preferenceId()).name(),
                        user.occupationName()
                ))
                .collect(Collectors.toList());
    }

    List<OccupationNameStats> occupationCodeToName(List<OccupationCodeStats> occupationCodeStats) {
        return occupationCodeStats.stream()
                .map(stat -> new OccupationNameStats(
                        occupationLocalCache.get(stat.occupationCategory()).occupationCategory(),
                        stat.count()
                ))
                .collect(Collectors.toList());
    }

    public Page<UserSimpleTypeInfo> findUsersByPage(Pageable pageable) {
        Page<User> userPage = userRepository.findAllByOrderByNameAsc(pageable);

        return userPage.map(user -> new UserSimpleTypeInfo(
                user.getName(),
                user.getGender(),
                user.getAge(),
                preferenceLocalCache.get(user.getUserBehaviorProfile().getPreferenceId()).name(),
                user.getOccupationName()
        ));
    }
}
