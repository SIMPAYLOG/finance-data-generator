package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.exception.ApiException;
import com.simpaylog.generatorapi.exception.ErrorCode;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {

    private final UserService userService;
    private final TransactionProgressTracker transactionProgressTracker;
    private final TransactionSimulationExecutor transactionSimulationExecutor;
    private final Set<String> activeSimulations = ConcurrentHashMap.newKeySet();

    public void startSimulation(String sessionId, LocalDate from, LocalDate to) {
        if (!activeSimulations.add(sessionId)) {
            log.warn("이미 시뮬레이션 진행 중인 세션: {}", sessionId);
            return;
        }
        try {
            List<TransactionUserDto> users = userService.findAllTransactionUserBySessionId(sessionId);
            if(users.isEmpty()) {
                throw new ApiException(ErrorCode.NO_USERS_FOUND);
            }
            int days = (int) ChronoUnit.DAYS.between(from, to) + 1;
            transactionProgressTracker.initProgress(sessionId, users.size() * days);
            transactionSimulationExecutor.simulateTransaction(users, from, to);
        } catch (CoreException e) {
            throw new ApiException(ErrorCode.SESSION_ID_NOT_FOUND, e.getMessage());
        }
    }
}
