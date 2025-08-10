package com.simpaylog.generatorsimulator.service;

import java.time.LocalDateTime;

@FunctionalInterface
public interface OneTimeEvent {
    void run();

    default LocalDateTime time() {
        throw new UnsupportedOperationException("시간 정의 필요");
    }
}
