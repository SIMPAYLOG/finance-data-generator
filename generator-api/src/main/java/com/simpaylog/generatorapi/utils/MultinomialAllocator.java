package com.simpaylog.generatorapi.utils;

import java.util.Arrays;
import java.util.Random;

public class MultinomialAllocator {

    /**
     * Multinomial 방식으로 N명을 주어진 확률 분포에 따라 무작위로 할당
     *
     * @param probabilities 정규화된 확률 배열 (합이 1이어야 함)
     * @param totalCount    총 인원 수 (샘플 수)
     * @return 각 그룹에 배정된 인원 수 배열
     */
    public static int[] sampleMultinomial(double[] probabilities, int totalCount) {
        int[] counts = new int[probabilities.length];
        Random rand = new Random();

        for (int i = 0; i < totalCount; i++) {
            double r = rand.nextDouble();
            double acc = 0.0;
            for (int j = 0; j < probabilities.length; j++) {
                acc += probabilities[j];
                if (r <= acc) {
                    counts[j]++;
                    break;
                }
            }
        }
        return counts;
    }

    /**
     * 배열을 정규화하여 합이 1이 되도록 변환
     *
     * @param arr 임의의 수 배열
     * @return 정규화된 배열 (비율 형태)
     */
    public static double[] normalize(double[] arr) {
        double sum = Arrays.stream(arr).sum();
        return Arrays.stream(arr).map(v -> v / sum).toArray();
    }
}
