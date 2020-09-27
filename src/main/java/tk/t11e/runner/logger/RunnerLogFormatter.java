package tk.t11e.runner.logger;
// Created by booky10 in JARRunner (09:34 06.09.20)

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class RunnerLogFormatter extends Formatter {

    private static final String FORMAT = "[%d] [%t/%level]: %msg%n";
    private static final SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        String formatted = FORMAT;

        formatted = formatted.replace("%t", Thread.currentThread().getName());
        formatted = formatted.replace("%level", record.getLevel().getName());
        formatted = formatted.replace("%msg", record.getMessage());
        formatted = formatted.replace("%n", "\n");
        formatted = formatted.replace("%d", date.format(new Date(record.getMillis())));

        Throwable throwable = record.getThrown();
        if (throwable != null) throwable.printStackTrace();

        String color;
        int levelInt = record.getLevel().intValue();

        if (levelInt >= Level.SEVERE.intValue())
            color = "91";
        else if (levelInt >= Level.WARNING.intValue())
            color = "93";
        else if (levelInt < Level.INFO.intValue())
            color = "92";
        else
            color = "37";

        return "\u001B[" + color + "m" + formatted;
    }
}