package tk.t11e.runner;
// Created by booky10 in JARRunner (09:16 06.09.20)

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import tk.t11e.runner.logger.RunnerLogger;
import tk.t11e.runner.utils.RunnerManager;
import tk.t11e.runner.utils.ThreadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Boot {

    public static Thread inputThread;
    public static final Logger logger = RunnerLogger.getLogger();
    private static Boolean running = true;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        new Thread(() -> {
            new File("config").mkdirs();
            new File("data").mkdirs();
            new File("plugins").mkdirs();

            OptionParser parser = new OptionParser();
            parser.allowsUnrecognizedOptions();

            parser.accepts("help");
            OptionSpec<String> autoStart = parser.accepts("start").withOptionalArg().defaultsTo("none");

            OptionSpec<String> optionNonOptions = parser.nonOptions();
            OptionSet options = parser.parse(args);
            List<String> list = new ArrayList<>(options.valuesOf(optionNonOptions));
            if (!list.isEmpty()) System.out.println("Completely ignored arguments: " + list);

            RunnerLogger.getLogger();

            if (options.has("help")) {
                logger.info("+------------------------------+");
                logger.info("| Current commands:            |");
                logger.info("|   - end (Stops the Program)  |");
                logger.info("|   - stop (Stop a JAR)        |");
                logger.info("|   - restart (Restart a JAR)  |");
                logger.info("|   - run (Start a JAR)        |");
                logger.info("|   - list (List running JARs) |");
                logger.info("+------------------------------+");
                logger.info("");
            } else if (!options.valueOf(autoStart).equals("none")) {
                logger.info("Starting Auto Started JAR...");
                RunnerManager.startJAR(options.valueOf(autoStart));
            }

            Thread shutdownThread = new Thread(() -> {
                List<String> runningThreads = new ArrayList<>(RunnerManager.threads.keySet());
                running = false;

                if (runningThreads.size() > 0)
                    new Thread(() -> {
                        System.out.println("Waiting for programs to finish...");

                        for (String name : new ArrayList<>(RunnerManager.threads.keySet()))
                            RunnerManager.stopJAR(name).getValue(voided -> runningThreads.remove(name));
                    }, "End Thread").start();

                while (runningThreads.size() != 0) ThreadUtils.sleep(100);
                System.out.println("Shutting down...");
            }, "Shutdown Thread");
            Runtime.getRuntime().addShutdownHook(shutdownThread);

            inputThread = new Thread(() -> {
                logger.info("Ready for Input!");
                while (running) {
                    try {
                        StringBuilder command = new StringBuilder();
                        char character;
                        while ((character = (char) System.in.read()) != '\n')
                            command.append(character);
                        String[] commandArgs = command.toString().toLowerCase().split(" ");

                        switch (commandArgs[0]) {
                            case "end":
                            case "quit":
                                System.exit(0);
                                break;
                            case "restart":
                                if (commandArgs.length >= 2)
                                    if (RunnerManager.threads.containsKey(commandArgs[1]))
                                        RunnerManager.stopJAR(commandArgs[1]).getValue(voided -> {
                                            ThreadUtils.sleep(150);
                                            RunnerManager.startJAR(commandArgs[1]);
                                        });
                                    else
                                        logger.warning("That doesn't run right now!");
                                else
                                    logger.warning("Please specify the name!");
                                break;
                            case "stop":
                                if (commandArgs.length >= 2)
                                    if (RunnerManager.threads.containsKey(commandArgs[1]))
                                        RunnerManager.stopJAR(commandArgs[1]);
                                    else
                                        logger.warning("That doesn't run right now!");
                                else
                                    logger.warning("Please specify the name!");
                                break;
                            case "run":
                            case "start":
                                if (commandArgs.length >= 2) {
                                    String name = commandArgs[1];
                                    Thread thread = RunnerManager.threads.get(name);

                                    if (thread == null) {
                                        try {
                                            RunnerManager.startJAR(name);
                                        } catch (Throwable throwable) {
                                            RunnerManager.threads.remove(name);
                                            logger.log(Level.SEVERE, "Error while starting!", throwable);
                                        }
                                    } else
                                        logger.warning("It already exits!");
                                } else
                                    logger.warning("Please specify the name!");
                                break;
                            case "list":
                                if (RunnerManager.threads.size() == 0)
                                    logger.warning("There are no current Threads!");
                                else {
                                    logger.info("Current Threads:");
                                    logger.info(String.join(", ", RunnerManager.threads.keySet()));
                                }
                                break;
                            case "help":
                                logger.info("+------------------------------+");
                                logger.info("| Current commands:            |");
                                logger.info("|   - end (Stops the Program)  |");
                                logger.info("|   - stop (Stop a JAR)        |");
                                logger.info("|   - restart (Restart a JAR)  |");
                                logger.info("|   - run (Start a JAR)        |");
                                logger.info("|   - list (List running JARs) |");
                                logger.info("+------------------------------+");
                                break;
                            case "booky10isgood":
                                shutdownThread.start();
                                break;
                            default:
                                logger.warning("Please use \"help\" to get help!");
                                break;
                        }
                    } catch (Throwable throwable) {
                        throw new Error(throwable);
                    }
                }
            }, "Input Thread");
            inputThread.start();

            ThreadUtils.sleep(15);
            logger.info("Finished Initialising (" + (System.currentTimeMillis() - start - 15) + "ms)");
        }, "Startup Thread").start();
    }
}