package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.dto.request.SimulationStartRequestDto;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {
    private final UserService userService;

    public void startSimulation(SimulationStartRequestDto request) {
        // TODO: 비동기 처리 모듈과 연결하여 이후 작업 필요
        log.info("{}명 생성 요청", request.userCount());
        List<User> result = userService.generateUser(request.userCount());
        userService.createUser(result);
    }

    public void stopSimulation() {
        userService.deleteUserAll();
    }
}
