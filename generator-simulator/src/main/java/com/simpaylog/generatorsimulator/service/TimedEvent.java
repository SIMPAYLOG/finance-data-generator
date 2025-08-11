package com.simpaylog.generatorsimulator.service;

import java.time.LocalDateTime;

public record TimedEvent(
        LocalDateTime time,
        Runnable task
) implements OneTimeEvent {

    @Override
    public void run() {
        task.run();
    }
}
