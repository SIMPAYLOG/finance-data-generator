package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.UserBehaviorProfile;
import com.simpaylog.generatorcore.enums.Gender;
import com.simpaylog.generatorcore.repository.UserBehaviorProfileRepository;
import com.simpaylog.generatorcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserBehaviorProfileRepository userBehaviorProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public User createUser(int age, Gender gender, BigDecimal incomeValue, int jobNumber, int occupationCode) {
        // TODO: 월급일 조절 필요
        // 유저:  나이대, 성별, 순자산, 부채, 작업번호, 직업번호
        // 유저 프로필: 소득금액
        UserBehaviorProfile profile = UserBehaviorProfile.of(incomeValue, 25);
        User user = User.of(profile, age, gender, BigDecimal.ZERO, BigDecimal.ZERO, jobNumber, occupationCode);
        userBehaviorProfileRepository.save(profile);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUserAll() {
        userRepository.deleteAll();
    }
}
