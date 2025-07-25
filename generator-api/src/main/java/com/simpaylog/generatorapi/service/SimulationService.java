package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
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

    public void startSimulation(LocalDate from, LocalDate to) {
        List<TransactionUserDto> users = userService.findAllTransactionUser();
        int days = (int) ChronoUnit.DAYS.between(from, to);
        transactionProgressTracker.initProgress("", users.size() * days);
        transactionSimulationExecutor.simulateTransaction(users, from, to);
    }
}
