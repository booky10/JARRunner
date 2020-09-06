package tk.t11e.runner.utils;
// Created by booky10 in JARRunner (10:54 06.09.20)

import tk.t11e.runner.logger.RunnerLogger;

import java.util.logging.Logger;

public abstract class RunnerPlugin {

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onLoad() {
    }

    private final Thread threadHacker = new Thread(() -> {
        while (true)
            try {
                Thread.sleep(2147483647L);
            } catch (InterruptedException ignored) {
            }
    }, "Thread Hacker [" + getClass().getSimpleName() + "]");

    private void startThreadHacker() {
        try {
            threadHacker.start();
        } catch (Throwable ignored) {
        }
    }

    private void stopThreadHacker() {
        try {
            if (threadHacker.isAlive())
                threadHacker.stop();
        } catch (Throwable ignored) {
        }
    }

    protected final Logger getLogger() {
        return RunnerLogger.getLogger();
    }
}