package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.chart.AgeGroupIncomeExpenseAverageDto;
import com.simpaylog.generatorapi.dto.chart.ChartData;
import com.simpaylog.generatorapi.dto.response.ChartResponse;
import com.simpaylog.generatorapi.dto.response.Response;
import com.simpaylog.generatorapi.exception.ErrorCode;
import com.simpaylog.generatorapi.service.TransactionAnalyzeService;
import com.simpaylog.generatorcore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
@Slf4j
public class ChartController {

    private final TransactionAnalyzeService transactionAnalyzeService;
    private final UserService userService;

    @GetMapping("/category-counts")
    public Response<?> getCategoryCounts(@RequestParam String sessionId) {
        try {
            ChartResponse response = transactionAnalyzeService.getCategoryCounts(sessionId);
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
            ChartResponse response = transactionAnalyzeService.getTopVomlumeCategoryCounts(sessionId, durationStart, durationEnd);
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
            ChartResponse response = transactionAnalyzeService.getTransactionSummary(durationStart, durationEnd, intervalType, sessionId);
            return Response.success(HttpStatus.OK.value(), response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument: {}", e.getMessage());
            return Response.error(ErrorCode.INVALID_REQUEST); // 예시 에러코드
        } catch (IOException e) {
            log.error("Elasticsearch connection error: {}", e.getMessage());
            return Response.error(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
    }

    @GetMapping("/category/by-age-group")
    public Response<?> getIdsByAge(
            @RequestParam String sessionId
    ) throws IOException {
        try {
            Map<String, List<ChartData>> response = transactionAnalyzeService.getCategorySummaryByAllAgeGroups(sessionId);
            return Response.success(HttpStatus.OK.value(), response);
        } catch (IOException e) {
            log.error("Elasticsearch error: {}", e.getMessage());
            return Response.error(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
    }

    @GetMapping("/income-expense/by-age-group2")
    public Response<?> getFinancialSummary(
            @RequestParam String sessionId
    ) {
        try {
            Map<String, AgeGroupIncomeExpenseAverageDto> response = transactionAnalyzeService.getFinancialsByAgeGroup(sessionId);
            return Response.success(HttpStatus.OK.value(), response);
        } catch (IOException e) {
            return Response.error(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
    }

    @GetMapping("/income-expense/by-age-group")
    public Response<?> getSummaryByAgeGroup(
            @RequestParam String sessionId,
            @RequestParam String durationStart,
            @RequestParam String durationEnd) {
        try {
            Map<Integer, List<Long>> userIdsByAgeGroup = userService.groupUserIdsByAgeForSession(sessionId);
            Map<String, AgeGroupIncomeExpenseAverageDto> response =transactionAnalyzeService.getFinancialsForGroup(sessionId, userIdsByAgeGroup, durationStart, durationEnd);
            return Response.success(HttpStatus.OK.value(), response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}