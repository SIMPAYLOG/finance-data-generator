package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.exception.ApiException;
import com.simpaylog.generatorapi.exception.ErrorCode;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final UserService userService;
    private final TransactionProgressTracker transactionProgressTracker;
    private final TransactionSimulationExecutor transactionSimulationExecutor;

    public void startSimulation(String sessionId, LocalDate from, LocalDate to) {
        try {
            List<TransactionUserDto> users = userService.findAllTransactionUserBySessionId(sessionId);
            if(users.isEmpty()) {
                throw new ApiException(ErrorCode.NO_USERS_FOUND);
            }
            int days = (int) ChronoUnit.DAYS.between(from, to);
            transactionProgressTracker.initProgress(sessionId, users.size() * days);
            transactionSimulationExecutor.simulateTransaction(users, from, to);
        } catch (CoreException e) {
            throw new ApiException(ErrorCode.SESSION_ID_NOT_FOUND, e.getMessage());
        }
    }
}
