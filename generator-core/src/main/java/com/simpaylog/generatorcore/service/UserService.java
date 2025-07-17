package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.UserBehaviorProfileRepository;
import com.simpaylog.generatorcore.repository.UserRepository;
import com.simpaylog.generatorcore.service.dto.UserGenerationCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
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

    public List<User> generateUser(UserGenerationCondition userGenerationCondition) {
        return userGenerator.generateUserPool(userGenerationCondition);
    }

    // TODO 유저 및 유저 프로필 반환 메서드 필요

    @Transactional
    public void updateUserBalance(Long userId, BigDecimal balance) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CoreException(String.format("NOT FOUND USER: %d", userId)));
        user.setBalance(balance);
    }

}
