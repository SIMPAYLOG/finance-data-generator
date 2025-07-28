package com.simpaylog.generatorapi.configuration.websocket;

import com.simpaylog.generatorapi.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

import static com.simpaylog.generatorapi.exception.ErrorCode.*;

@Slf4j
public class WebsocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes){
        // 쿼리 파라미터를 추출
        String fromStr = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("durationStart");
        String toStr = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("durationEnd");

        if (fromStr == null || toStr == null) {//from 또는 to를 파라미터로 받지 못했을 경우
            log.error("Handshake rejected: 'durationStart' or 'durationEnd' date parameter is missing.");
            throw new ApiException(INVALID_DATE_SETTING);
        }

        try {
            // LocalDate로 파싱 시도
            LocalDate from = LocalDate.parse(fromStr);
            LocalDate to = LocalDate.parse(toStr);

            attributes.put("durationStart", from);
            attributes.put("durationEnd", to);

            log.info("Handshake successful with dates: duration start={}, duration end={}", from, to);
            return true; // 핸드셰이크를 계속 진행합니다.

        } catch (DateTimeParseException e) {//date 형식이 잘못된 경우
            log.error("Handshake rejected: Invalid date format. duration start='{}', duration end='{}'.", fromStr, toStr, e);
            throw new ApiException(INVALID_DATE_FORMAT);
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Exception exception) {
    }
}