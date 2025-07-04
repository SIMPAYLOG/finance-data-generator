package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.request.CreateUserRequestDto;
import com.simpaylog.generatorapi.dto.request.UserGenerationConditionRequestDto;
import com.simpaylog.generatorapi.dto.response.Response;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Response<List<User>> getAllUsers() {
        List<User> users = userService.selectUserAll();
        return Response.success(HttpStatus.OK.value(), users);
    }

    @PostMapping
    public Response<Void> createUser(@RequestBody @Valid CreateUserRequestDto createUserRequestDto) {
        List<User> result = new ArrayList<>();
        for (int i = 0; i < createUserRequestDto.conditions().size(); i++) {
            UserGenerationConditionRequestDto dto = createUserRequestDto.conditions().get(i);
            result.addAll(userService.generateUser(UserGenerationConditionRequestDto.toCore(dto, i)));
        }
        userService.createUser(result);
        return Response.success(HttpStatus.OK.value());
    }
}
