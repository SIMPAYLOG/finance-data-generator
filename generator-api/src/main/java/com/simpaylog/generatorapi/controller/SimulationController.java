package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.request.SimulationStartRequestDto;
import com.simpaylog.generatorapi.dto.response.CommonResponse;
import com.simpaylog.generatorapi.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/simulation")
public class SimulationController {
    private final SimulationService simulationService;

    @PostMapping("/start")
    public ResponseEntity<CommonResponse<Void>> startSimulation(@RequestBody SimulationStartRequestDto simulationStartRequestDto) {
        return simulationService.startSimution(simulationStartRequestDto);
    }
}
