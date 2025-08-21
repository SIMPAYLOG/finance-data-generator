package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.request.SimulationStartRequestDto;
import com.simpaylog.generatorapi.dto.response.Response;
import com.simpaylog.generatorcore.dto.UserGenerationCondition;
import com.simpaylog.generatorcore.dto.response.*;
import com.simpaylog.generatorcore.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.simpaylog.generatorapi.dto.request.UserGenerationConditionRequestDto.toUserGenerationCondition;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @DeleteMapping
    public Response<Void> dropAllUsers() {
        userService.deleteUserAll();
        return Response.success(HttpStatus.OK.value());
    }

    @DeleteMapping("/by-session")
    public Response<Void> dropUsersBySessionId(@RequestParam String sessionId) {
        userService.deleteUsersBySessionId(sessionId);
        return Response.success(HttpStatus.OK.value());
    }

    @PostMapping
    public Response<String> createUser(@RequestBody @Valid SimulationStartRequestDto req) {
        List<UserGenerationCondition> list = new ArrayList<>();
        for (int i = 0; i < req.conditions().size(); i++) {
            list.add(toUserGenerationCondition(req.conditions().get(i), i));
        }
        String sessionId = userService.createUser(list);
        userService.initPaydayCache(sessionId, req.durationStart(), req.durationEnd());
        userService.initFixedObligation(sessionId, req.durationStart(), req.durationEnd());
        return Response.success(HttpStatus.OK.value(), sessionId);
    }

    @GetMapping("/analyze")
    public Response<UserAnalyzeResultResponse> analyzeUsers(@RequestParam String sessionId) {
        return Response.success(HttpStatus.OK.value(), userService.analyzeUsers(sessionId));
    }

    @GetMapping("/list")
    public Response<Page<UserInfoResponse>> getUsers(
            @PageableDefault(size = 10, sort = "name") Pageable pageable,
            @RequestParam String sessionId
    ) {
        return Response.success(HttpStatus.OK.value(), userService.findUsersByPage(pageable, sessionId));
    }

    @GetMapping("/age-group")
    public Response<AgeGroupResponse> getAgeGroup() {
        return Response.success(HttpStatus.OK.value(), userService.getAgeGroup());
    }

    @GetMapping("occupation-category")
    public Response<OccupationListResponse> getOccupationCategory() {
        return Response.success(HttpStatus.OK.value(), userService.getOccupationCategory());
    }

    @GetMapping("preference-list")
    public Response<PreferenceListResponse> getPreference() {
        return Response.success(HttpStatus.OK.value(), userService.getPreferenceList());
    }

    @GetMapping("count")
    public Response<UserCntResponse> getUserCnt(@RequestParam String sessionId) {
        return Response.success(HttpStatus.OK.value(), userService.getUserCnt(sessionId));
    }
}
