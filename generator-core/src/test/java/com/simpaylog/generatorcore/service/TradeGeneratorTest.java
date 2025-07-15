package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.TestConfig;
import com.simpaylog.generatorcore.service.dto.Trade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TradeGeneratorTest extends TestConfig{
    @Autowired
    private TradeGenerator tradeGenerator;

    private static final List<String> ALL_CATEGORY_NAMES = Arrays.asList(
            "groceriesNonAlcoholicBeverages",
            "alcoholicBeveragesTobacco",
            "clothingFootwear",
            "householdGoodsServices",
            "housingUtilitiesFuel",
            "health",
            "transportation",
            "communication",
            "recreationCulture",
            "education",
            "foodAccommodation",
            "otherGoodsServices"
    );

    //소득분위 및 카테고리 조합을 만드는 함수
    private static Stream<Arguments> provideDecileAndCategoryComb(){
        return IntStream.rangeClosed(1, 10)
                .boxed()
                .flatMap(decile -> ALL_CATEGORY_NAMES.stream()
                        .map(categoryName -> Arguments.of(decile, categoryName)));
    }

    @ParameterizedTest
    @MethodSource("provideDecileAndCategoryComb")
    void 분위와_카테고리가_주어지면_랜덤_거래를_생성한다(int decile, String categoryName){
        //When
        Trade trade = tradeGenerator.generateTrade(decile, categoryName);
        //Then
        // 1. Trade 객체가 null이 아닌지 확인합니다.
        assertThat(trade).isNotNull();

        // 2. tradeName이 비어있지 않은지 확인합니다.
        assertThat(trade.tradeName()).isNotBlank();

        // 3. cost가 양수이고, 100원 단위로 떨어지는지 확인한다.
        assertThat(trade.cost()).isPositive();
        assertThat(trade.cost() % 100).isEqualTo(0); // 비용이 100의 배수인지 확인
    }

}