package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.TestConfig;
import com.simpaylog.generatorapi.dto.request.SimulationStartRequestDto;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Import(SimulationService.class)
class SimulationServiceTest extends TestConfig {
    private final UserService userService = mock(UserService.class);
    private final SimulationService sut = new SimulationService(userService);

    @Test
    void 시뮬레이션_시작했을_때_정상동작() {
        // Given
        int totalUserCnt = 5;
        SimulationStartRequestDto requestDto = new SimulationStartRequestDto(totalUserCnt, LocalDate.now(), LocalDate.now().plusDays(7));
        List<User> mockUsers = new ArrayList<>();
        for (int i = 0; i < totalUserCnt; i++) {
            mockUsers.add(mock(User.class));
        }
        when(userService.generateUser(totalUserCnt)).thenReturn(mockUsers);
        // When
        sut.startSimulation(requestDto);
        // Then
        ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
        verify(userService).createUser(captor.capture());
        verify(userService).generateUser(totalUserCnt);
        List<User> actualCreatedUsers = captor.getValue();
        assertEquals(totalUserCnt, actualCreatedUsers.size());
    }

    @Test
    void 시뮬레이션_중지했을때_정상동작() {
        // when
        sut.stopSimulation();
        // then
        verify(userService).deleteUserAll();
    }
}