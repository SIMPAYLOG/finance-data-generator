package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    @GetMapping("/api")
    public void test(
            @RequestParam int userCnt
    ) {
        simulationService.startSimulation(userCnt);
    }

}
