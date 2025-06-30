package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.repository.UserBehaviorProfileRepository;
import com.simpaylog.generatorcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<User> generateUser(int totalUserCnt) {
        return userGenerator.generateUserPool(totalUserCnt);
    }
}
