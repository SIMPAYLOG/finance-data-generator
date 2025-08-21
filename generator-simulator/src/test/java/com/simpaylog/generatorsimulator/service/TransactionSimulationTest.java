package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorsimulator.TestConfig;
import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorsimulator.dto.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
 * [트랜잭션 생성 시뮬레이션 테스트]
 * <p>
 * 이 테스트는 실제 사용자 시나리오에 따라 특정 기간 동안의 시간대별 트랜잭션 생성을 시뮬레이션한다.
 * - 각 시간에 대해 성향(preference)에 따라 카테고리를 선택하고
 * - 해당 카테고리에 맞는 소비 내역을 생성하여 총 소비 금액과 분포를 출력함
 * <p>
 * 이 테스트는 TransactionService의 전체 흐름을 검증하지 않음.
 * → 대신 TransactionGenerator 및 TradeGenerator의 동작을 통합적으로 시뮬레이션하여
 * 소비 패턴이 성향에 맞게 분포되는지 확인하는 데 목적을 둠.
 * <p>
 * 통합 테스트 성격이 있지만, 외부 시스템(Kafka, DB)과의 연동은 포함하지 않음.
 */

@Tag("simulation")
@DisplayName("사용자 성향 기반 트랜잭션 생성 시뮬레이션 테스트")
@Import({TransactionGenerator.class, TradeGenerator.class})
class TransactionSimulationTest extends TestConfig {

    @Autowired
    private TransactionGenerator transactionGenerator;
    @Autowired
    private TradeGenerator tradeGenerator;

    private final String[] DAYS = {" ", "월", "화", "수", "목", "금", "토", "일"};
    private static Map<CategoryType, Integer> categoryCostMap = new HashMap<>();

    @BeforeEach
    void setup() {
        for (CategoryType ct : CategoryType.values()) categoryCostMap.put(ct, 0);
    }


    /**
     * [사용자 성향에 따른 트랜잭션 생성 시뮬레이션]
     * <p>
     * 각 성향(PreferenceType)에 대해 7월 한 달간의 트랜잭션을 시뮬레이션함
     * 시간별로 가능한 소비 카테고리를 선택하고, Trade를 생성하여 로그를 출력
     * 결과적으로 카테고리별 소비 금액을 집계
     * <p>
     * 이 테스트는 다음을 검증하는 데 목적이 있음
     * - pickOneCategory()가 성향과 시간대에 맞게 작동하는지
     * - generateTrade()가 실제 금액을 현실적으로 생성하는지
     * - 하루에 같은 카테고리 중복 사용이 방지되는지
     */
    @MethodSource("preferenceTypes")
    @ParameterizedTest(name = "[{index}] 성향: {0}")
    void 기간과_분위_성향이_주어지고_카테고리별_사용내역을_출력한다(PreferenceType preferenceType) {
        // Given
        LocalDateTime from = LocalDateTime.of(2025, 7, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 7, 31, 23, 59);
        int decile = 1;

        BigDecimal totalSpent = simulateTransactions(from, to, decile, preferenceType);

        System.out.printf("%n총 사용 금액: %d%n", totalSpent.intValue());
        for (Map.Entry<CategoryType, Integer> entry : categoryCostMap.entrySet()) {
            System.out.printf("[%s]: %d원 사용%n", entry.getKey().getLabel(), entry.getValue());
        }

    }

    static Stream<Arguments> preferenceTypes() {
        return Arrays.stream(PreferenceType.values()).map(Arguments::of);
    }

    private BigDecimal simulateTransactions(LocalDateTime from, LocalDateTime to, int decile, PreferenceType preferenceType) {
        BigDecimal money = BigDecimal.ZERO;
        Map<CategoryType, LocalDateTime> repeated = new HashMap<>();
        LocalDateTime current = null;
        LocalDate prevDate = from.toLocalDate();

        long hours = ChronoUnit.HOURS.between(from, to);
        for (int hour = 0; hour <= hours; hour++) {
            int[] minutes = getRandomMinutes();
            for (int mIdx = 0; mIdx < minutes.length; mIdx++) {
                current = from.plusHours(hour).plusMinutes(minutes[mIdx]);

                LocalDate currentDate = current.toLocalDate();
                if (!currentDate.equals(prevDate)) { // 날짜가 변경되었는지 체크
                    System.out.println();
                    repeated.clear();
                    prevDate = currentDate;
                }

                CategoryType picked = transactionGenerator.pickOneCategory(current, preferenceType, repeated).orElse(null);
                if (picked == null) continue;

                repeated.put(picked, current);
                Trade result = tradeGenerator.generateTrade(decile, picked);
                money = money.add(result.cost());
                categoryCostMap.put(picked, categoryCostMap.get(picked) + result.cost().intValue());
                printTransaction(current, picked, result);
            }
        }
        printCategorySummary(categoryCostMap);
        return money;
    }

    private int[] getRandomMinutes() {
        int cnt = ThreadLocalRandom.current().nextInt(4);
        int[] minutes = new int[cnt];
        for (int i = 0; i < cnt; i++) minutes[i] = ThreadLocalRandom.current().nextInt(60);
        Arrays.sort(minutes);
        return minutes;
    }

    private void printTransaction(LocalDateTime time, CategoryType category, Trade trade) {
        System.out.printf("[%s %s] %-20s\t %s %d원 지출%n", time, DAYS[time.getDayOfWeek().getValue()], category.getLabel(), trade.tradeName(), trade.cost().intValue());
    }

    private void printCategorySummary(Map<CategoryType, Integer> map) {
        System.out.println();
        for (Map.Entry<CategoryType, Integer> entry : map.entrySet()) {
            System.out.printf("[%s]: %d원 사용%n", entry.getKey().getLabel(), entry.getValue());
        }
    }

}