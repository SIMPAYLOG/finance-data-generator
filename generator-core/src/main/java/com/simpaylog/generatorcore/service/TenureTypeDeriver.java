package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.dto.FixedObligation;

import java.util.concurrent.ThreadLocalRandom;

public class TenureTypeDeriver {

    public static FixedObligation.TenureType derive(int decile, int ageGroup) {
        double[] p = baseDist(decile);

        // 연령대 가중치
        double ownerBias = switch (ageGroup) {
            case 10, 20 -> -0.10; // 임차쪽 10%쯤 우세
            case 30     ->  0.00; // 중립
            case 40     -> +0.10; // 소유쪽 10%쯤 우세
            default     -> +0.20; // 50+ 소유쪽 20%쯤 우세
        };
        tiltOwner(p, ownerBias);

        double r = ThreadLocalRandom.current().nextDouble(); // [0,1)
        double c1 = p[0];            // 월세 누계
        double c2 = c1 + p[1];       // 전세 누계
        double c3 = c2 + p[2];       // 자가(대출) 누계

        if (r < c1) return FixedObligation.TenureType.RENTER_MONTHLY;
        if (r < c2) return FixedObligation.TenureType.RENTER_JEONSE;
        if (r < c3) return FixedObligation.TenureType.OWNER_MORTGAGE;
        return FixedObligation.TenureType.OWNER_FULL;
    }

    // 월세/전세/자가(대출)/자가(완납)
    private static double[] baseDist(int decile) {
        if (decile <= 3) return new double[]{0.70, 0.20, 0.09, 0.01};
        if (decile <= 6) return new double[]{0.20, 0.35, 0.35, 0.10};
        if (decile <= 8) return new double[]{0.10, 0.15, 0.50, 0.25};
        return new double[]{0.05, 0.05, 0.45, 0.45};
    }

    // 연령대에 따른 임차 VS 소유 나누기
    private static void tiltOwner(double[] p, double ownerBias) {
        double wOwner = 1.0 + ownerBias; // 소유 수치
        double wTenant = 1.0 - ownerBias; // 임차 수치

        // 임차: p[0]=월세, p[1]=전세 / 소유: p[2]=자가(대출), p[3]=자가(완납)
        p[0] *= wTenant;
        p[1] *= wTenant;
        p[2] *= wOwner;
        p[3] *= wOwner;

        // normalize
        double s = p[0] + p[1] + p[2] + p[3];
        p[0] /= s; p[1] /= s; p[2] /= s; p[3] /= s;
    }
}
