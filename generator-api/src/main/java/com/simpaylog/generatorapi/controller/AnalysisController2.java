package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.analysis.HourlyTransaction;
import com.simpaylog.generatorapi.dto.response.CommonChart;
import com.simpaylog.generatorapi.dto.response.Response;
import com.simpaylog.generatorapi.service.AnalysisService2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/analysis2")
@RequiredArgsConstructor
public class AnalysisController2 {
    private final AnalysisService2 analysisService;

    @GetMapping("/search-time-average")
    public Response<CommonChart<HourlyTransaction.HourlySummary>> searchTimeSummaryByPeriod(
            @RequestParam String sessionId,
            @RequestParam LocalDate durationStart,
            @RequestParam LocalDate durationEnd
    ) throws IOException {
        return Response.success(HttpStatus.OK.value(), analysisService.searchTimeSummaryByPeriod(sessionId, durationStart, durationEnd));
    }

}
