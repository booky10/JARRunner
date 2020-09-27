package tk.t11e.runner.utils;
// Created by booky10 in JARRunner (19:02 07.09.20)

public class ThreadUtils {

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            throw new Error(exception);
        }
    }
}