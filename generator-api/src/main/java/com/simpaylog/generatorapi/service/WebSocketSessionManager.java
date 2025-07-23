package com.simpaylog.generatorapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class WebSocketSessionManager {

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public void sendProgressUpdate(String message) {
        log.info("Sending progress update: {}", message);
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                log.error("Failed to send message to session {}", session.getId(), e);
            }
        });
    }

    public void closeAllSessions() {
        log.info("Closing all WebSocket sessions.");
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.close(CloseStatus.NORMAL);
                }
            } catch (IOException e) {
                log.error("Error closing session {}", session.getId(), e);
            }
        });
        sessions.clear(); // 세션 목록 비우기
    }
}