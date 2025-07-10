package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.request.CreateUserRequestDto;
import com.simpaylog.generatorapi.dto.response.Response;
import com.simpaylog.generatorcore.dto.UserGenerationCondition;
import com.simpaylog.generatorcore.dto.response.UserAnalyzeResultResponse;
import com.simpaylog.generatorcore.dto.response.UserInfoResponse;
import com.simpaylog.generatorcore.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


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

    @PostMapping
    public Response<Void> createUser(@RequestBody @Valid CreateUserRequestDto createUserRequestDto) {
        List<UserGenerationCondition> list = new ArrayList<>();
        for(int i = 0; i < createUserRequestDto.conditions().size(); i++){
            list.add(toUserGenerationCondition(createUserRequestDto.conditions().get(i), i));
        }
        userService.createUser(list);
        return Response.success(HttpStatus.OK.value());
    }

    @GetMapping("/analyze")
    public Response<UserAnalyzeResultResponse> analyzeUsers() {
        return Response.success(HttpStatus.OK.value(), userService.analyzeUsers());
    }

    @GetMapping("/list")
    public Response<Page<UserInfoResponse>> getUsers(
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        return Response.success(HttpStatus.OK.value(), userService.findUsersByPage(pageable));
    }
}
