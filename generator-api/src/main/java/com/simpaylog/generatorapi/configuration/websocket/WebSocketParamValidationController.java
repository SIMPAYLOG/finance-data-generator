package com.simpaylog.generatorapi.configuration.websocket;

import com.simpaylog.generatorapi.dto.response.Response;
import com.simpaylog.generatorapi.exception.ApiException;
import com.simpaylog.generatorapi.exception.ErrorCode;
import com.simpaylog.generatorcore.repository.redis.RedisSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/validate")
public class WebSocketParamValidationController {
    private final RedisSessionRepository redisSessionRepository;

    @GetMapping("/websocket-params")
    public Response<Void> validateWebSocketParams(
            @RequestParam String sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate durationStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate durationEnd
    ) {
        // 세션 ID 확인
        if (sessionId == null || sessionId.isBlank() || !redisSessionRepository.hasKey(sessionId)) {
            throw new ApiException(ErrorCode.SESSION_ID_NOT_FOUND);
        }
        // 날짜 순서 확인
        if (durationStart.isAfter(durationEnd)) {
            throw new ApiException(ErrorCode.INVALID_DATE_SETTING);
        }
        return Response.success(HttpStatus.OK.value());
    }
}
