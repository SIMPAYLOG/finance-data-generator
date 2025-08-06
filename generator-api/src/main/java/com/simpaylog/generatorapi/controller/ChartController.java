package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.response.ChartResponse;
import com.simpaylog.generatorapi.dto.response.Response;
import com.simpaylog.generatorapi.exception.ErrorCode;
import com.simpaylog.generatorapi.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
@Slf4j
public class ChartController {

    private final TransactionLogService transactionLogService;

    @GetMapping("/category-counts")
    public Response<?> getCategoryCounts(@RequestParam String sessionId) {
        try {
            ChartResponse response = transactionLogService.getCategoryCounts(sessionId);
            return Response.success(HttpStatus.OK.value(), response);
        } catch (IOException e) {
            log.error(e.getMessage());
            return Response.error(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
    }

    @GetMapping("/top-volume-category-counts")
    public Response<?> getTopVolumeCategoryCounts(
            @RequestParam String sessionId,
            @RequestParam String durationStart,
            @RequestParam String durationEnd) {
        try {
            ChartResponse response = transactionLogService.getTopVomlumeCategoryCounts(sessionId, durationStart, durationEnd);
            return Response.success(HttpStatus.OK.value(), response);
        } catch (IOException e) {
            log.error(e.getMessage());
            return Response.error(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
    }

    @GetMapping("/transactions/summary")
    public Response<?> getTransactionSummary(
            @RequestParam String durationStart,
            @RequestParam String durationEnd,
            @RequestParam String intervalType,
            @RequestParam String sessionId) {
        try {
            ChartResponse response = transactionLogService.getTransactionSummary(durationStart, durationEnd, intervalType, sessionId);
            return Response.success(HttpStatus.OK.value(), response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument: {}", e.getMessage());
            return Response.error(ErrorCode.INVALID_REQUEST); // 예시 에러코드
        } catch (IOException e) {
            log.error("Elasticsearch connection error: {}", e.getMessage());
            return Response.error(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
    }
}