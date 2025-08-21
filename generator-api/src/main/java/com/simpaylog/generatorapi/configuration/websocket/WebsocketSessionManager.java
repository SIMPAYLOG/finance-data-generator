package com.simpaylog.generatorapi.configuration.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class WebsocketSessionManager {

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public void sendProgressUpdate(String message) {
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    synchronized (session) { // 세션 단위 직렬화
                        session.sendMessage(new TextMessage(message));
                    }
                }
            } catch (IOException e) {
                log.error("Failed to send message to session {}", session.getId(), e);
                removeSession(session);
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