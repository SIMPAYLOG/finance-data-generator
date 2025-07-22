package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorcore.repository.UserRepository;
import com.simpaylog.generatorcore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationService {
    private final UserService userService;

    private final TransactionSimulationExecutor transactionSimulationExecutor;

    @Async
    public void startSimulation(LocalDate from, LocalDate to) {
        List<TransactionUserDto> users = userService.findAllTransactionUser();
        transactionSimulationExecutor.simulateTransaction(users, from, to);
    }
}
