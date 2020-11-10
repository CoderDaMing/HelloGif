package com.ming.hellogif.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 此类比{@link java.util.Timer}更可取
 */
public class GifTimer {
    private final ScheduledExecutorService executorService;

    public GifTimer() {
        executorService = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(getClass().getSimpleName()));
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        return executorService.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    public void cancel() {
        try {
            executorService.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}