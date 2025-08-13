package com.simpaylog.generatorsimulator.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyUtil {

    public static BigDecimal adjust(BigDecimal amount, int unit, RoundingMode mode) {
        if (amount == null) {
            throw new IllegalArgumentException("금액(amount)은 null이 될 수 없습니다.");
        }
        if (unit <= 0) {
            throw new IllegalArgumentException("단위(unit)은 0보다 커야 합니다.");
        }
        return amount
                .divide(BigDecimal.valueOf(unit), 0, mode) // 단위로 나누어 반올림/올림/내림
                .multiply(BigDecimal.valueOf(unit));       // 다시 단위 곱
    }

    /** 10원 단위 반올림 */
    public static BigDecimal roundTo10(BigDecimal amount) {
        return adjust(amount, 10, RoundingMode.HALF_UP);
    }

    /** 100원 단위 반올림 */
    public static BigDecimal roundTo100(BigDecimal amount) {
        return adjust(amount, 100, RoundingMode.HALF_UP);
    }

    /** 10원 단위 올림 */
    public static BigDecimal ceilTo10(BigDecimal amount) {
        return adjust(amount, 10, RoundingMode.CEILING);
    }

    /** 10원 단위 내림 */
    public static BigDecimal floorTo10(BigDecimal amount) {
        return adjust(amount, 10, RoundingMode.FLOOR);
    }

    /** 100원 단위 올림 */
    public static BigDecimal ceilTo100(BigDecimal amount) {
        return adjust(amount, 100, RoundingMode.CEILING);
    }

    /** 100원 단위 내림 */
    public static BigDecimal floorTo100(BigDecimal amount) {
        return adjust(amount, 100, RoundingMode.FLOOR);
    }

}
