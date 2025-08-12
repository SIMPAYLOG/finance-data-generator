package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorsimulator.TestConfig;
import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorsimulator.dto.Trade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TradeGenerator.class)
class TradeGeneratorTest extends TestConfig {
    @Autowired
    private TradeGenerator tradeGenerator;


    //소득분위 및 카테고리 조합을 만드는 함수
    private static Stream<Arguments> provideDecileAndCategoryComb() {
        return IntStream.rangeClosed(1, 10) // 1~10 분위
                .boxed()
                .flatMap(decile -> Arrays.stream(CategoryType.values())
                        .map(category -> Arguments.of(decile, category))
                );
    }

    @ParameterizedTest
    @MethodSource("provideDecileAndCategoryComb")
    void 분위와_카테고리가_주어지면_랜덤_거래를_생성한다(int decile, CategoryType categoryType) {
        //When
        Trade trade = tradeGenerator.generateTrade(decile, categoryType);
        //Then
        // 1. Trade 객체가 null이 아닌지 확인합니다.
        assertThat(trade).isNotNull();

        // 2. tradeName이 비어있지 않은지 확인합니다.
        assertThat(trade.tradeName()).isNotBlank();

        // 3. cost가 양수이고, 100원 단위로 떨어지는지 확인한다.
        assertThat(trade.cost()).isPositive();
        assertThat(trade.cost().intValue() % 100).isEqualTo(0); // 비용이 100의 배수인지 확인
        System.out.println(trade.tradeName() + " | " + trade.cost());
    }

}