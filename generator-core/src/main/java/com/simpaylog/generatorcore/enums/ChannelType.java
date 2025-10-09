package com.simpaylog.generatorcore.enums;

public enum ChannelType {
    CARD,       // 오프라인/온라인 카드결제 통합
    TRANSFER,   // 계좌이체, 송금
    AUTO,       // 자동이체, 구독 결제
    ATM,        // 현금 입출금
    MOBILE,     // 간편결제(삼성페이, 네이버페이 등)
    SYSTEM      // 내부처리, 이자, 수수료, 정정 등
}