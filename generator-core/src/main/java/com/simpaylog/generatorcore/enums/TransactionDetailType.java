package com.simpaylog.generatorcore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionDetailType {
    // 입금 계열
    DEPOSIT("입금", "일반 입금", ChannelType.TRANSFER),
    REFUND("환불", "결제 환불", ChannelType.CARD),
    INTEREST("이자", "이자 지급", ChannelType.SYSTEM),
    CASH_DEPOSIT("현금입금", "ATM 현금 입금", ChannelType.ATM),

    // 출금 계열
    WITHDRAWAL("출금", "일반 출금", ChannelType.TRANSFER),
    AUTO_PAYMENT("자동이체", "자동이체 출금", ChannelType.AUTO),
    CARD_PAYMENT("카드결제", "카드 결제", ChannelType.CARD),
    FEE("수수료", "수수료 출금", ChannelType.SYSTEM),
    LOAN_REPAYMENT("대출상환", "대출 상환 출금", ChannelType.SYSTEM),
    CASH_WITHDRAWAL("현금출금", "ATM 현금 출금", ChannelType.ATM),

    // 내부 이체 계열
    INTERNAL_TRANSFER_OUT("이체출금", "내부계좌 이체 출금", ChannelType.SYSTEM),
    INTERNAL_TRANSFER_IN("이체입금", "내부계좌 이체 입금", ChannelType.SYSTEM);


    // ---- 필드 ----
    private final String label;          // 한글 표시명
    private final String description;    // 설명
    private final ChannelType channel;   // 거래 발생 채널

}
