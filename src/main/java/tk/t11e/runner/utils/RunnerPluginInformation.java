package tk.t11e.runner.utils;
// Created by booky10 in JARRunner (20:12 07.09.20)

import java.io.File;

public class RunnerPluginInformation {

    private final String name;
    private final Thread thread;
    private final RunnerPlugin instance;
    private final File file;

    public RunnerPluginInformation(String name, Thread thread, RunnerPlugin instance, File file) {
        this.name = name;
        this.thread = thread;
        this.instance = instance;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public Thread getThread() {
        return thread;
    }

    public RunnerPlugin getInstance() {
        return instance;
    }

    public File getFile() {
        return file;
    }
}