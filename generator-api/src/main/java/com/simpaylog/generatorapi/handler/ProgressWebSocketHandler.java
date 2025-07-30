package com.simpaylog.generatorapi.handler;

import com.simpaylog.generatorapi.service.SimulationService;
import com.simpaylog.generatorapi.configuration.websocket.WebsocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProgressWebSocketHandler extends TextWebSocketHandler {

    private final WebsocketSessionManager webSocketSessionManager;
    private final SimulationService simulationService;

    // 클라이언트가 연결되었을 때 실행
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket session established: {}", session.getId());
        webSocketSessionManager.addSession(session);
        simulationService.startSimulation(
                session.getAttributes().get("sessionId").toString(),
                (LocalDate) session.getAttributes().get("durationStart"),
                (LocalDate) session.getAttributes().get("durationEnd")
        );
    }

    // 클라이언트 연결이 끊겼을 때 실행
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket session closed: {} with status: {}", session.getId(), status);
        webSocketSessionManager.removeSession(session);
    }
}