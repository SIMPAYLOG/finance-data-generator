package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorsimulator.TestConfig;
import com.simpaylog.generatorcore.cache.TradeInfoLocalCache;
import com.simpaylog.generatorcore.cache.dto.TradeInfo;
import com.simpaylog.generatorcore.dto.CategoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TradeGenerator.class)
public class TradeGeneratorUnitTest extends TestConfig {
    @Autowired
    TradeInfoLocalCache tradeInfoLocalCache;

    Random random = new Random();

    @ParameterizedTest
    @MethodSource("provideDecileAndCategoryComb") //소득분위 및 카테고리 조합을 만드는 함수
    @DisplayName("소득분위와 카테고리를 입력받아, 카테고리 거래 분위 선택 가중치를 캐시에서 가져온다")
    void 소득분위와_카테고리를_입력받아_가중치를_캐시에서_가져온다(int decile, CategoryType categoryType) {
        //Given(파라미터)
        //When
        List<Double> weights = tradeInfoLocalCache.getWeights(decile, categoryType);
        //Then
        // 1. 가져온 weights 리스트가 null이 아니어야함
        assertThat(weights)
                .as("Decile %d, Category '%s'에 대한 weights는 null이 아니어야 합니다.", decile, categoryType)
                .isNotNull();

        // 2. 가져온 weights 리스트가 비어있지 않아야함 (유효한 가중치가 존재해야 함)
        assertThat(weights)
                .as("Decile %d, Category '%s'에 대한 weights는 비어있지 않아야 합니다.", decile, categoryType)
                .isNotEmpty();

        // 3. weights 리스트의 크기가 10 이어야함(10분위 각각을 선택할 확률 데이터임)
        assertThat(weights.size())
                .as("Decile %d, Category '%s'에 대한 weights 리스트의 크기는 10이어야 합니다.", decile, categoryType)
                .isEqualTo(10);

        // 4. 모든 가중치 값이 0.0보다 크거나 같아야함.
        assertThat(weights)
                .as("Decile %d, Category '%s'의 모든 가중치는 0.0 이상이어야 합니다.", decile, categoryType)
                .allMatch(weight -> weight >= 0.0 && weight <= 1.0);
    }

    //소득분위 및 카테고리 조합을 만드는 함수
    private static Stream<Arguments> provideDecileAndCategoryComb() {
        return IntStream.rangeClosed(1, 10) // 1~10 분위
                .boxed()
                .flatMap(decile -> Arrays.stream(CategoryType.values())
                        .map(category -> Arguments.of(decile, category))
                );
    }

    @ParameterizedTest
    @MethodSource("provideWeights") //소득분위 및 카테고리 조합을 만드는 함수
    @DisplayName("소득분위와 카테고리를 입력받아, 카테고리 거래 분위 선택 가중치를 캐시에서 가져온다")
    void 가중치를_기반으로_1에서_10분위_중_하나의_분위를_확률적으로_뽑는다(List<Double> weights) {
        //Given(매개변수 로 주어진 weigths)
        //When
        //아래 로직대로라면, 가중치 합이 정확이 1이나 100 처럼 100%를 표시하는 수가 되지 않아도 됨
        //(예시) weights = [1, 3, 7]이 주어지는 경우, 11을 100%라고 가정한 확률에서 뽑기 시작하는 로직임
        int newDecile = 0;
        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight <= 0) {
            // 모든 가중치가 0이거나 음수인 경우, 기본값으로 1분위 반환
            newDecile = 1;
        }

        double randomValue = random.nextDouble() * totalWeight; // 0.0 (inclusive) ~ totalWeight (exclusive)
        double cumulativeWeight = 0.0;

        for (int decile = 1; decile <= weights.size(); decile++) {
            cumulativeWeight += weights.get(decile - 1); //index 값이므로 decile - 1
            if (randomValue < cumulativeWeight) {
                newDecile = decile;
            }
        }
        // 부동 소수점 오차 등으로 인해 루프가 끝까지 도달하지 못할 경우, 마지막 decile 반환
        if (newDecile == 0) newDecile = 1;

        //Then
        // 1. newDecile이 1~10 사이 값이 나옴
        assertThat(newDecile).isBetween(1, 10);
    }

    //가중치 리스트를 만드는 함수
    private static Stream<Arguments> provideWeights() {
        return Stream.of(
                // 1. 균등 가중치 (모든 분위가 동일한 확률)
                Arguments.of(Arrays.asList(0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1)),
                // 2. 낮은 분위에 가중치 집중 (합이 1)
                Arguments.of(Arrays.asList(0.4, 0.3, 0.1, 0.05, 0.05, 0.02, 0.02, 0.02, 0.02, 0.02)),
                // 3. 높은 분위에 가중치 집중 (합이 1)
                Arguments.of(Arrays.asList(0.02, 0.02, 0.02, 0.02, 0.02, 0.1, 0.1, 0.2, 0.2, 0.38)),
                // 4. 특정 분위에만 가중치 (예: 5분위만 1.0)
                Arguments.of(Arrays.asList(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0)),
                // 5. 모든 가중치가 0인 경우 (로직에 따라 1분위 반환 예상)
                Arguments.of(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)),
                // 6. 가중치 합이 1이 아닌 경우 (내부적으로 정규화되므로 문제 없음)
                Arguments.of(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)),
                // 7. 가중치 리스트 크기가 10보다 작은 경우 (로직이 마지막 decile을 반환하는지 확인)
                Arguments.of(Arrays.asList(0.5, 0.5))
        );
    }

    @ParameterizedTest
    @MethodSource("provideDecileAndCategoryComb") //소득분위 및 카테고리 조합을 만드는 함수
    @DisplayName("새로 뽑은 소득분위와 카테고리 이름을 사용하여 거래 목록들을 가져온다")
    void 소득분위와_카테고리_이름을_사용하여_거래_목록을_가져온다(int decile, CategoryType categoryType) {
        //When
        List<TradeInfo.TradeItemDetail> trades = tradeInfoLocalCache.getTradeList(decile, categoryType);
        //Then
        // 1. trades가 null이 아니어야 한다.
        assertThat(trades).isNotNull();
        // 2. trades가 빈 배열이 아니어야 한다.
        assertThat(trades).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10, 50})
    @DisplayName("거래 목록에서 임의의 거래 하나를 정한다")
    void 거래_목록에서_임의의_거래_하나를_선택한다(int cnt) {
        //Given
        List<TradeInfo.TradeItemDetail> trades = new ArrayList<>();
        for (int i = 1; i <= cnt; i++) {
            trades.add(new TradeInfo.TradeItemDetail("거래" + cnt, 10 + cnt, 100 + cnt));
        }
        //When
        int randomIdx = random.nextInt(trades.size()); // 0 <= idx < size
        TradeInfo.TradeItemDetail trade = trades.get(randomIdx);
        //Then
        // 1. 임의의 거래가 null이 아님
        assertThat(trade).isNotNull();
    }

}
