package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.TestConfig;
import com.simpaylog.generatorapi.configuration.OccupationLocalCache;
import com.simpaylog.generatorapi.dto.OccupationInfos;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.enums.Gender;
import com.simpaylog.generatorcore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.*;

@Import({OccupationLocalCache.class, SimulationService.class})
class SimulationServiceTest extends TestConfig {
    private final UserService userService = mock(UserService.class);
    private final OccupationLocalCache occupationLocalCache = mock(OccupationLocalCache.class);
    private final SimulationService sut = new SimulationService(userService, occupationLocalCache);

    @BeforeEach
    void setup() {
        // 직업 비율이 하나만 있다고 가정
        when(occupationLocalCache.getRatios()).thenReturn(new double[]{1.0});
        // 직업 정보 Mock
        OccupationInfos.AgeGroupInfo ageGroupInfo = mock(OccupationInfos.AgeGroupInfo.class);
        when(ageGroupInfo.ratio()).thenReturn(1.0);
        when(ageGroupInfo.dominantDeciles()).thenReturn(new int[]{1, 1});
        OccupationInfos.Occupation occupation = mock(OccupationInfos.Occupation.class);
        when(occupation.ageGroupInfo()).thenReturn(Collections.singletonList(ageGroupInfo));
        when(occupation.decileDistribution()).thenReturn(new double[]{1.0});
        when(occupation.averageMonthlyWage()).thenReturn(100000);
        when(occupation.occupationCategory()).thenReturn("테스트직업");
        when(occupationLocalCache.get(anyInt())).thenReturn(occupation);

        // userService.createUser가 호출될 때마다 dummy User 반환
        when(userService.createUser(anyInt(), any(Gender.class), any(BigDecimal.class), anyInt(), anyInt()))
                .thenReturn(mock(User.class));
    }

    @Test
    void 사용자풀_사이즈를_입력받았을_때_생성된_리스트의_크기와_동일하다() {
        // Given
        int totalUserCnt = 500;
        // When
        sut.startSimulation(totalUserCnt);
        // Then
        verify(userService, times(totalUserCnt))
                .createUser(anyInt(), any(Gender.class), any(BigDecimal.class), anyInt(), anyInt());
        // deleteUserAll도 호출되었는지 검증
        verify(userService, times(1)).deleteUserAll();
    }
}