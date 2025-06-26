package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.dto.request.SimulationStartRequestDto;
import com.simpaylog.generatorapi.dto.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimulationService {
    public ResponseEntity<CommonResponse<Void>> startSimution(SimulationStartRequestDto simulationStartRequestDto) {
        return ResponseEntity.ok(CommonResponse.success(HttpStatus.CREATED.value()));
    }
}
