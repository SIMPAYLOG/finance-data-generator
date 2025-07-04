package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.dto.request.SimulationStartRequestDto;
import com.simpaylog.generatorapi.dto.request.UserGenerationConditionRequestDto;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {
    private final UserService userService;

    public void startSimulation(SimulationStartRequestDto request) {
        // TODO: 비동기 처리 모듈과 연결하여 이후 작업 필요
        List<User> result = new ArrayList<>();
        for (UserGenerationConditionRequestDto dto : request.conditions()) {
            result.addAll(userService.generateUser(UserGenerationConditionRequestDto.toCore(dto, 1)));
        }
        userService.createUser(result);
    }

    public void stopSimulation() {
        userService.deleteUserAll();
    }
}
