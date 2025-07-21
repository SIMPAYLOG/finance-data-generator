package com.simpaylog.generatorcore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationService {
    private final WebSocketProgressService webSocketProgressService;

    @Async
    public void startSimulation(WebSocketSession session, String period) {
        log.info("Starting dummy data generation for session: {}", session.getId());

        try {
            for(int i = 1; i <= Integer.parseInt(extractNumbers(period)); i++) {
                webSocketProgressService.sendProgressUpdate(i+"개월 데이터");
                Thread.sleep(3000);
            }
            webSocketProgressService.sendProgressUpdate("시뮬레이션이 완료되었습니다!");
        } catch (Exception e) {
            log.error("Error while sending dummy data", e);
            Thread.currentThread().interrupt();
        }finally {
            try {
                if (session.isOpen()) { // 세션이 아직 열려있는지 확인
                    log.info("Closing WebSocket session {} after simulation completion", session.getId());
                    session.close(CloseStatus.NORMAL); // 정상 종료 상태로 연결 닫기
                }
            } catch (Exception closeException) {
                log.error("Error closing WebSocket session {}: {}", session.getId(), closeException.getMessage());
            }
        }
    }

    public static String extractNumbers(String text) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
