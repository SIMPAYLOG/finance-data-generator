package com.simpaylog.generatorapi.configuration.websocket;

import com.simpaylog.generatorapi.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import static com.simpaylog.generatorapi.exception.ErrorCode.INVALID_DATE_FORMAT;

@Slf4j
@RequiredArgsConstructor
public class WebsocketHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
        try {
            // 쿼리 파라미터를 추출
            String sessionId = params.getFirst("sessionId");
            LocalDate from = LocalDate.parse(Objects.requireNonNull(params.getFirst("durationStart")));
            LocalDate to = LocalDate.parse(Objects.requireNonNull(params.getFirst("durationEnd")));

            attributes.put("sessionId", sessionId);
            attributes.put("durationStart", from);
            attributes.put("durationEnd", to);

            log.info("Handshake successful with dates: duration start={}, duration end={}", from, to);
            return true; // 핸드셰이크를 계속 진행합니다.

        } catch (Exception e) {//date 형식이 잘못된 경우
            log.warn("Unexpected handshake request received with invalid parameters. URI: {}", request.getURI(), e);
            throw new ApiException(INVALID_DATE_FORMAT);
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}