package com.simpaylog.generatorsimulator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TradeGenerator tradeGenerator;
    private final TransactionGenerator transactionGenerator;

    /*
    1. 정해진 기간 for문
    2. 유저정보(분위, 성향)
     */

}
