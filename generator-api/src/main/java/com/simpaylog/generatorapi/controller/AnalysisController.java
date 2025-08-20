package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.analysis.*;
import com.simpaylog.generatorapi.dto.analysis.HourlyTransaction;
import com.simpaylog.generatorapi.dto.analysis.PeriodTransaction;
import com.simpaylog.generatorapi.dto.analysis.TimeHeatmapCell;
import com.simpaylog.generatorapi.dto.chart.ChartIncomeCountDto;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            @RequestParam String interval,
            @RequestParam(required = false) Integer userId
    ) throws IOException {
        return Response.success(HttpStatus.OK.value(), analysisService.searchByPeriod(sessionId, durationStart, durationEnd, interval, userId));
    }

    //전체 - 개인 월별 수입/지출 금액 비교
    @GetMapping("/search-period-amount")
    public Response<CommonChart<PeriodTransaction.PTSummary>> searchPeriodAmount(
            @RequestParam String sessionId,
            @RequestParam LocalDate durationStart,
            @RequestParam LocalDate durationEnd,
            @RequestParam String interval,
            @RequestParam(required = false) Integer userId
    ) throws IOException {
        return Response.success(HttpStatus.OK.value(), analysisService.searchPeriodAmount(sessionId, durationStart, durationEnd, interval, userId));
    }

    @GetMapping("/time-heatmap")
    public Response<CommonChart<TimeHeatmapCell.TCSummary>> searchByPeriod(
            @RequestParam String sessionId,
            @RequestParam LocalDate durationStart,
            @RequestParam LocalDate durationEnd
    ) throws IOException {
        return Response.success(HttpStatus.OK.value(), analysisService.searchTimeHeatmap(sessionId, durationStart, durationEnd));
    }

    @GetMapping("/amount-avg/by-hour")
    public Response<CommonChart<HourlyTransaction.HourlySummary>> searchTimeAmountAvgByPeriod(
            @RequestParam String sessionId,
            @RequestParam LocalDate durationStart,
            @RequestParam LocalDate durationEnd,
            @RequestParam(required = false) Integer userId
    ) throws IOException {
        return Response.success(HttpStatus.OK.value(), analysisService.searchHourAmountAvgByPeriod(sessionId, durationStart, durationEnd, userId));
    }

    @GetMapping("/amount-avg/by-transaction-type")
    public Response<CommonChart<AmountTransaction.AmountTransactionSummary>> searchTypeAmountAvgByPeriod(
            @RequestParam String sessionId,
            @RequestParam LocalDate durationStart,
            @RequestParam LocalDate durationEnd,
            @RequestParam(required = false) Integer userId
    ) throws IOException {
        return Response.success(HttpStatus.OK.value(), analysisService.searchUserTradeAmountAvgByUserId(sessionId, durationStart, durationEnd, userId));
    }

    @GetMapping("/all-category-info")
    public Response<?> searchAllCategoryInfo(
    @RequestParam String sessionId,
    @RequestParam String durationStart,
    @RequestParam String durationEnd
    ) {
        return Response.success(HttpStatus.OK.value(), analysisService.searchAllCategoryInfo(sessionId, durationStart, durationEnd));
    }

    @GetMapping("/category/by-volume-top5")
    public Response<?> searchCategoryByVomlumeTop5(
            @RequestParam String sessionId,
            @RequestParam String durationStart,
            @RequestParam String durationEnd,
            @RequestParam(required = false) Integer userId
    ) {
        return Response.success(HttpStatus.OK.value(), analysisService.searchCategoryByVomlumeTop5(sessionId, durationStart, durationEnd, userId));
    }

    @GetMapping("/category/by-userId")
    public Response<CommonChart<CategoryAmountTransaction.AmountTransactionSummary>> searchUserCategoryTradeAmount(
            @RequestParam String sessionId,
            @RequestParam LocalDate durationStart,
            @RequestParam LocalDate durationEnd,
            @RequestParam(required = false) Integer userId
    ) throws IOException {
        return Response.success(HttpStatus.OK.value(), analysisService.searchUserCategoryTradeAmount(sessionId, durationStart, durationEnd, userId));
    }

    @GetMapping("/category/by-age-group")
    public Response<?> searchCategoryByVomlumeTop5EachAgeGroup(
            @RequestParam String sessionId,
            @RequestParam String durationStart,
            @RequestParam String durationEnd
    ) {
        Map<String, List<ChartIncomeCountDto>> response = analysisService.searchCategoryByVomlumeTop5EachAgeGroup(sessionId, durationStart, durationEnd);
        return Response.success(HttpStatus.OK.value(), response);
    }

    @GetMapping("/transactions/info")
    public Response<?> searchTransactionInfo(
            @RequestParam(required = false) String durationStart,
            @RequestParam(required = false) String durationEnd,
            @RequestParam String intervalType,
            @RequestParam String sessionId
    ) {
        return Response.success(HttpStatus.OK.value(), analysisService.searchTransactionInfo(sessionId, Optional.ofNullable(durationStart), Optional.ofNullable(durationEnd), intervalType));
    }


    @GetMapping("/income-expense/by-age-group")
    public Response<?> searchIncomeExpenseForAgeGroup(
            @RequestParam String sessionId,
            @RequestParam String durationStart,
            @RequestParam String durationEnd
    ) {
        return Response.success(HttpStatus.OK.value(), analysisService.searchIncomeExpenseForAgeGroup(sessionId, durationStart, durationEnd));
    }

    @GetMapping("/income-expense/by-preference")
    public Response<?> searchIncomeExpenseForPreferece(
            @RequestParam String sessionId,
            @RequestParam String durationStart,
            @RequestParam String durationEnd
    ) {
        return Response.success(HttpStatus.OK.value(), analysisService.searchIncomeExpenseForPreferece(sessionId, durationStart, durationEnd));
    }

    @GetMapping("/income-expense")
    public Response<?> searchIncomeExpense(
            @RequestParam String sessionId,
            @RequestParam(required = false) String durationStart,
            @RequestParam(required = false) String durationEnd,
            @RequestParam(required = false) Integer userId
    ) {
        return Response.success(HttpStatus.OK.value(), analysisService.searchIncomeExpense(sessionId, durationStart, durationEnd, userId));
    }
}
