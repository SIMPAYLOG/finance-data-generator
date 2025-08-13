package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorsimulator.cache.TradeInfoLocalCache;
import com.simpaylog.generatorsimulator.cache.dto.TradeInfo;
import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorsimulator.dto.Trade;
import com.simpaylog.generatorsimulator.exception.SimulatorException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@AllArgsConstructor
public class TradeGenerator {

    private final TradeInfoLocalCache tradeInfoLocalCache;

    /**
     * 입력된 분위와 카테고리를 기반으로 새로운 분위, 임의의 거래 및 해당 거래의 비용을 생성합니다.
     *
     * @param decile       사용자의 현재 분위 (1~10)
     * @param categoryType 사용자가 선택한 카테고리 이름 (예: "groceriesNonAlcoholicBeverages")
     * @return 생성된 거래 이름과 비용을 포함하는 TradeResult 객체
     * @throws IllegalArgumentException 유효하지 않은 입력 또는 데이터 부족 시 발생
     */
    public Trade generateTrade(int decile, CategoryType categoryType) {
        // 1. 입력 유효성 검사 (분위는 1~10, 카테고리 이름은 비어있지 않아야 함)
        if (decile < 1 || decile > 10) {
            throw new SimulatorException(String.format("소득 분위는 1~10분위 사이어야 합니다. 입력된 분위값: %d", decile));
        }

        // 2. 입력받은 분위와 카테고리에 해당하는 weights를 참조
        List<Double> weights = tradeInfoLocalCache.getWeights(decile, categoryType);
        if (weights == null || weights.isEmpty()) {
            log.warn("decile: {} | category: {} 에 weights가 존재하지 않습니다.", decile, categoryType);
            throw new SimulatorException("Weights data not available for the given decile and category.");
        }

        // 3. 1~10에 대한 발생 확률을 가진 weights를 기반으로, 1~10분위 중 하나를 뽑음 (newDecile)
        int newDecile = selectNewDecile(weights);

        // 4. 새롭게 뽑은 newDecile, 카테고리 정보를 기반으로 trades 가져옴
        List<TradeInfo.TradeItemDetail> trades = tradeInfoLocalCache.getTradeList(newDecile, categoryType);
        if (trades == null || trades.isEmpty()) {
            log.warn("new decile: {} | category: {} 에 대한 trades가 존재하지 않습니다.", newDecile, categoryType.getKey());
            throw new SimulatorException("Trade list not available or empty for the selected new decile and category.");
        }

        // 5. trades에서 임의의 거래 하나 선택
        TradeInfo.TradeItemDetail selectedTrade = selectRandomTrade(trades);
        // 6. 해당 거래의 min, max값 사이의 값 하나를 cost로 정하기
        BigDecimal cost = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(selectedTrade.min(), selectedTrade.max() + 1));
        // 7. 임의의 거래 이름 및 cost 반환
        return new Trade(selectedTrade.name(), cost);
    }

    public Map<CategoryType, Integer> estimateCounts(int decile, Map<CategoryType, BigDecimal> budget) {
        Map<CategoryType, Integer> out = new EnumMap<>(CategoryType.class);
        for (Map.Entry<CategoryType, BigDecimal> e : budget.entrySet()) {
            CategoryType category = e.getKey();
            BigDecimal averageAmount = meanAmount(decile, category); // 해당 카테고리의 평균 결제 금액

            int N = (averageAmount.signum() > 0) ? e.getValue().divide(averageAmount, 0, RoundingMode.DOWN).intValue() : 1;
            int clamp = switch (category) {
                case HOUSING_UTILITIES_FUEL, EDUCATION, COMMUNICATION -> Math.max(1, Math.min(N, 3));
                case GROCERIES_NON_ALCOHOLIC_BEVERAGES -> Math.max(10, Math.min(N, 80));
                case FOOD_ACCOMMODATION -> Math.max(4, Math.min(N, 40));
                default -> Math.max(2, Math.min(N, 30));
            };
            out.put(category, clamp);
        }
        return out;
    }

    // 카테고리별 평균 결제 금액
    private BigDecimal meanAmount(int decile, CategoryType category) {
        List<TradeInfo.TradeItemDetail> trades = tradeInfoLocalCache.getTradeList(decile, category);
        if (trades.isEmpty()) return BigDecimal.ZERO;

        double s = 0.0;
        for (TradeInfo.TradeItemDetail trade : trades) {
            double avg = (trade.min() + trade.max()) / 2.0;
            s += avg;
        }
        return BigDecimal.valueOf(s / trades.size());
    }

    /**
     * 주어진 가중치 리스트를 기반으로 새로운 분위(decile)를 확률적으로 선택합니다.
     * 가중치 리스트의 인덱스는 decile - 1에 해당합니다.
     *
     * @param weights 각 decile에 대한 확률을 나타내는 리스트 (총합이 1이 아니어도 내부적으로 정규화)
     * @return 선택된 새로운 분위 (1~10)
     */
    private int selectNewDecile(List<Double> weights) {
        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight <= 0) {
            // 모든 가중치가 0이거나 음수인 경우, 기본값으로 1분위 반환
            log.warn("가중치 총 합이 0입니다. default 값인 1분위를 반환합니다.");
            return 1;
        }

        double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight; // 0.0 (inclusive) ~ totalWeight (exclusive)
        double cumulativeWeight = 0.0;

        for (int decile = 1; decile <= weights.size(); decile++) {
            cumulativeWeight += weights.get(decile - 1); //idx값이므로 decile - 1
            if (randomValue < cumulativeWeight) {
                return decile;
            }
        }
        // 부동 소수점 오차 등으로 인해 루프가 끝까지 도달하지 못할 경우, 마지막 decile 반환
        return 1;
    }

    /**
     * 주어진 거래 목록에서 임의의 거래 하나를 선택합니다.
     *
     * @param trades 선택할 거래 목록
     * @return 임의로 선택된 TradeItemDetail 객체
     * @throws IllegalArgumentException 거래 목록이 비어있을 경우 발생
     */
    private TradeInfo.TradeItemDetail selectRandomTrade(List<TradeInfo.TradeItemDetail> trades) {
        if (trades.isEmpty()) {
            throw new SimulatorException("해당 카테고리의 상품이 비어있습니다.");
        }
        int randomIdx = ThreadLocalRandom.current().nextInt(trades.size()); // 0 <= idx < size
        return trades.get(randomIdx);
    }

}