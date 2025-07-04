package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.TestConfig;
import com.simpaylog.generatorapi.dto.request.SimulationStartRequestDto;
import com.simpaylog.generatorapi.dto.request.UserGenerationConditionRequestDto;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.service.UserService;
import org.junit.jupiter.api.Test;
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
        int[] userCnt = {5, 10, 15, 20};
        SimulationStartRequestDto request = new SimulationStartRequestDto(List.of(
                new UserGenerationConditionRequestDto(userCnt[0], "MIX", "MIX", "MIX", "MIX"),
                new UserGenerationConditionRequestDto(userCnt[1], "MIX", "MIX", "MIX", "MIX"),
                new UserGenerationConditionRequestDto(userCnt[2], "MIX", "MIX", "MIX", "MIX"),
                new UserGenerationConditionRequestDto(userCnt[3], "MIX", "MIX", "MIX", "MIX")
        ), LocalDate.now(), LocalDate.now().plusDays(7));
        List<User> mockUsers1 = makeUsers(userCnt[0]);
        List<User> mockUsers2 = makeUsers(userCnt[1]);
        List<User> mockUsers3 = makeUsers(userCnt[2]);
        List<User> mockUsers4 = makeUsers(userCnt[3]);

        when(userService.generateUser(any()))
                .thenReturn(mockUsers1)
                .thenReturn(mockUsers2)
                .thenReturn(mockUsers3)
                .thenReturn(mockUsers4);
        // When
        sut.startSimulation(request);
        // Then
        verify(userService, times(4)).generateUser(any());
        verify(userService).createUser(argThat(users -> users.size() == 50));
    }

    @Test
    void 시뮬레이션_중지했을때_정상동작() {
        // when
        sut.stopSimulation();
        // then
        verify(userService).deleteUserAll();
    }

    private List<User> makeUsers(int size) {
        List<User> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(mock(User.class));
        }
        return result;
    }

}