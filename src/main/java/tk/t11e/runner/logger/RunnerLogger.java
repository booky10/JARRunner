package tk.t11e.runner.logger;
// Created by booky10 in JARRunner (09:28 06.09.20)

import tk.t11e.runner.Boot;

import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class RunnerLogger {

    public static final HashMap<String, Logger> loggers = new HashMap<>();

    public static Logger getLogger() {
        try {
            throw new Throwable();
        } catch (Throwable throwable) {
            String name = throwable.getStackTrace()[1].getClassName();

            if (!loggers.containsKey(name)) {
                Logger logger = Logger.getLogger(name);

                if (Boot.logger != null) logger.setParent(Boot.logger);
                logger.setUseParentHandlers(false);

                ConsoleHandler handler = new ConsoleHandler();
                handler.setFormatter(new RunnerLogFormatter());
                logger.addHandler(handler);

                loggers.put(name, logger);
            }
            return loggers.getOrDefault(name, Logger.getAnonymousLogger());
        }
    }
}