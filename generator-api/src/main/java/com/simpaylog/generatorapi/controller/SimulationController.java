package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.request.SimulationStartRequestDto;
import com.simpaylog.generatorapi.dto.response.Response;
import com.simpaylog.generatorapi.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/simulation")
public class SimulationController {
    private final SimulationService simulationService;

    @PostMapping("/start")
    public Response<Void> startSimulation(@RequestBody SimulationStartRequestDto simulationStartRequestDto) {
        simulationService.startSimulation(simulationStartRequestDto);
        return Response.success(HttpStatus.OK.value());
    }

    @GetMapping("/stop")
    public Response<Void> stopSimulation() {
        simulationService.stopSimulation();
        return Response.success(HttpStatus.OK.value());
    }

}
