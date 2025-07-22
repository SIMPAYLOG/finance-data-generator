package com.simpaylog.generatorapi.handler;

import com.simpaylog.generatorapi.service.SimulationService;
import com.simpaylog.generatorapi.service.WebSocketProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgressWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketProgressService progressService;
    private final SimulationService simulationService;

    // 클라이언트가 연결되었을 때 실행
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket session established: {}", session.getId());
        progressService.addSession(session);

        simulationService.startSimulation((LocalDate)session.getAttributes().get("from"), (LocalDate)session.getAttributes().get("to"));
    }

    // 클라이언트 연결이 끊겼을 때 실행
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket session closed: {} with status: {}", session.getId(), status);
        progressService.removeSession(session);
    }
}