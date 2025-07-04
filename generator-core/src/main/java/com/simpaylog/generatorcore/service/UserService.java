package com.simpaylog.generatorcore.service;

//import com.simpaylog.generatorapi.dto.request.UserGenerationConditionRequestDto;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.repository.UserBehaviorProfileRepository;
import com.simpaylog.generatorcore.repository.UserRepository;
import com.simpaylog.generatorcore.dto.UserGenerationCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Transactional(readOnly = true)
    public List<User> selectUserAll(){
        return userRepository.findAll();
    }

    public List<User> generateUser(UserGenerationCondition userGenerationCondition) {
        return userGenerator.generateUserPool(userGenerationCondition);
    }
}
