package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.analysis.HourlyTransaction;
import com.simpaylog.generatorapi.dto.analysis.PeriodTransaction;
import com.simpaylog.generatorapi.dto.analysis.TimeHeatmapCell;
import com.simpaylog.generatorapi.dto.response.CommonChart;
import com.simpaylog.generatorapi.dto.response.Response;
import com.simpaylog.generatorapi.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/time-heatmap")
    public Response<CommonChart<TimeHeatmapCell.TCSummary>> searchByPeriod(
            @RequestParam String sessionId,
            @RequestParam LocalDate durationStart,
            @RequestParam LocalDate durationEnd
    ) throws IOException {
        return Response.success(HttpStatus.OK.value(), analysisService.searchTimeHeatmap(sessionId, durationStart, durationEnd));
    }

    @GetMapping("/hour-amount-average")
    public Response<CommonChart<HourlyTransaction.HourlySummary>> searchTimeAmountAvgByPeriod(
            @RequestParam String sessionId,
            @RequestParam LocalDate durationStart,
            @RequestParam LocalDate durationEnd
    ) throws IOException {
        return Response.success(HttpStatus.OK.value(), analysisService.searchTimeAmountAvgByPeriod(sessionId, durationStart, durationEnd));
    }

}
