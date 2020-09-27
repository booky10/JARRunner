package tk.t11e.runner.utils;
// Created by booky10 in JARRunner (10:54 06.09.20)

import tk.t11e.runner.logger.RunnerLogger;

import java.util.logging.Logger;

public abstract class RunnerPlugin {

    private final String name;

    protected RunnerPlugin(String name) {
        this.name = name;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onLoad() {
    }

    private final Thread threadHacker = new Thread(() -> {
        while (true) ThreadUtils.sleep(2147483647);
    }, "Thread Hacker [" + getClass().getSimpleName() + "]");

    final void startThreadHacker() {
        try {
            threadHacker.start();
        } catch (Throwable ignored) {
        }
    }

    final void stopThreadHacker() {
        try {
            if (threadHacker.isAlive())
                threadHacker.stop();
        } catch (Throwable ignored) {
        }
    }

    protected final Logger getLogger() {
        return RunnerLogger.getLogger();
    }

    protected final String getName() {
        return name;
    }

    public final void disable() {
        RunnerManager.stopJAR(name);
    }
}