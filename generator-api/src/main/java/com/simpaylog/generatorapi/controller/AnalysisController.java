package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.analysis.PeriodTransaction;
import com.simpaylog.generatorapi.dto.response.CommonChart;
import com.simpaylog.generatorapi.dto.response.Response;
import com.simpaylog.generatorapi.service.AnalysisService;
import com.simpaylog.generatorapi.service.TransactionExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;

    @GetMapping("/search-by-period")
    public Response<CommonChart<PeriodTransaction.PTSummary>> searchByPeriod(
            @RequestParam String sessionId,
            @RequestParam LocalDate durationStart,
            @RequestParam LocalDate durationEnd,
            @RequestParam String interval
    ) throws IOException {
        return Response.success(HttpStatus.OK.value(), analysisService.searchByPeriod(sessionId, durationStart, durationEnd, interval));
    }

}
