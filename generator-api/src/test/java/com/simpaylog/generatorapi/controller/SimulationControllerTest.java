package com.simpaylog.generatorapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.request.SimulationStartRequestDto;
import com.simpaylog.generatorapi.dto.request.UserGenerationConditionRequestDto;
import com.simpaylog.generatorapi.service.SimulationService;
import com.simpaylog.generatorcore.repository.UserRepository;
import com.simpaylog.generatorcore.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(SimulationController.class)
class SimulationControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    SimulationService simulationService;
    @MockitoBean
    UserService userService;
    @MockitoBean
    UserRepository userRepository;

    @Test
    void 잘못된_값을_받았을_때_에러반환() throws Exception {
        // Given
        UserGenerationConditionRequestDto mockCondition = new UserGenerationConditionRequestDto(-1, "TEST-preferenceId", "TEST-ageGroup", "TEST-gender", "TEST-occupationCode");
        SimulationStartRequestDto mockDto = new SimulationStartRequestDto(List.of(mockCondition), LocalDate.now(), LocalDate.now().plusDays(5));
        // When & Then
        mockMvc.perform(post("/api/simulation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void 정상적인_값을_받았을_때() throws Exception {
        // Given
        UserGenerationConditionRequestDto mockCondition = new UserGenerationConditionRequestDto(100, "1", "10", "MALE", "MIX");
        SimulationStartRequestDto mockDto = new SimulationStartRequestDto(List.of(mockCondition), LocalDate.now(), LocalDate.now().plusDays(5));
        // When & Then
        mockMvc.perform(post("/api/simulation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockDto))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
}