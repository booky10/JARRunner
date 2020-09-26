package tk.t11e.runner.utils;
// Created by booky10 in JARRunner (09:43 06.09.20)

import tk.t11e.runner.logger.RunnerLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnerManager {

    public static final File jarDirectory = new File("plugins");
    public static final HashMap<String, Thread> threads = new HashMap<>();
    private static final HashMap<String, Object> instances = new HashMap<>();
    private static final Logger logger = RunnerLogger.getLogger();

    static {
        if (!jarDirectory.exists()) jarDirectory.mkdirs();
    }

    public static void startJAR(String jar) {
        jar = jar.toLowerCase().replace(' ', '_');
        String name = jar;

        Thread thread = new Thread(() -> {
            try {
                File file = new File(jarDirectory, name + ".jar");
                if (!file.exists()) throw new FileNotFoundException("JAR does not exits!");

                URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
                URL manifestURL = classLoader.getResource("RunnerPlugin.MF");
                URL realManifestURL = classLoader.getResource("META-INF/MANIFEST.MF");

                if (manifestURL != null || realManifestURL != null) {
                    String main;
                    if (manifestURL != null) {
                        URLConnection connection = manifestURL.openConnection();
                        connection.connect();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        HashMap<String, String> manifest = new HashMap<>();
                        try {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String[] split = line.split(": ");
                                String key = split[0];
                                StringBuilder value = new StringBuilder();
                                for (int i = 1; i < split.length; i++) {
                                    value.append(split[i]);
                                    if (i != split.length - 1) value.append(": ");
                                }
                                manifest.put(key, value.toString());
                            }
                        } catch (Throwable throwable) {
                            logger.log(Level.SEVERE, "Error while reading Manifest!", throwable);
                            return;
                        }
                        String main = manifest.get("Runner-Main");
                    } else {
                        URLConnection connection = manifestURL.openConnection();
                        connection.connect();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        HashMap<String, String> manifest = new HashMap<>();
                        try {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String[] split = line.split(": ");
                                String key = split[0];
                                StringBuilder value = new StringBuilder();
                                for (int i = 1; i < split.length; i++) {
                                    value.append(split[i]);
                                    if (i != split.length - 1) value.append(": ");
                                }
                                manifest.put(key, value.toString());
                            }
                        } catch (Throwable throwable) {
                            logger.log(Level.SEVERE, "Error while reading Manifest!", throwable);
                            return;
                        }
                        String main = manifest.get("Main-Class");
                    }

                    if (main == null)
                        logger.severe("Could not find Runner-Main in manifest!");
                    else {
                        Class<?> mainClass = classLoader.loadClass(main);
                        if (mainClass != null) {
                            try {
                                if (RunnerPlugin.class.isAssignableFrom(mainClass)) {
                                    Object instance = mainClass.newInstance();

                                    try {
                                        new Thread(() -> {
                                            try {
                                                mainClass.getMethod("onLoad").invoke(instance);
                                            } catch (Throwable throwable) {
                                                throw new Error(throwable);
                                            }
                                        }, name + " Loader").start();

                                        new Thread(() -> {
                                            try {
                                                mainClass.getMethod("onEnable").invoke(instance);
                                            } catch (Throwable throwable) {
                                                throw new Error(throwable);
                                            }
                                        }, name + " Enabler").start();

                                        logger.info("Started ¸\"" + name + "\"!");
                                        instances.put(name, instance);

                                        new Thread(() -> {
                                            try {
                                                Method hackThreadStart = RunnerPlugin.class.getDeclaredMethod("startThreadHacker");
                                                hackThreadStart.setAccessible(true);
                                                hackThreadStart.invoke(instance);
                                            } catch (Throwable throwable) {
                                                throw new Error(throwable);
                                            }
                                        }, name + " Thread Hacker").start();
                                        return;
                                    } catch (Throwable throwable) {
                                        logger.log(Level.SEVERE, "Error while enabling " + name + "!", throwable);
                                        try {
                                            mainClass.getMethod("onDisable").invoke(instance);
                                        } catch (Throwable error) {
                                            logger.log(Level.SEVERE, "Error while disabling " + name + "!", throwable);
                                        }
                                    }
                                } else
                                    logger.severe("The main class does not extend the RunnerPlugin!");
                            } catch (Throwable throwable) {
                                logger.log(Level.SEVERE, "Error while trying to execute program!", throwable);
                            }
                        } else
                            logger.severe("Main class (from manifest) could not be found!");
                    }
                } else
                    logger.severe("RunnerPlugin.MF does not exits!");
            } catch (Throwable throwable) {
                logger.log(Level.SEVERE, "Error while starting!", throwable);
            }
            threads.remove(name);
        }, "Starter Thread [" + name + "] ");

        threads.put(name, thread);
        thread.start();
    }

    public static CompletableInFuture<Void> stopJAR(String jar) {
        CompletableInFuture<Void> future = new CompletableInFuture<>();

        jar = jar.toLowerCase().replace(' ', '_');
        String name = jar;

        new Thread(() -> {
            try {
                if (threads.containsKey(name))
                    if (instances.containsKey(name)) {
                        Object instance = instances.get(name);
                        Class<?> instanceClass = instance.getClass();

                        try {
                            instanceClass.getMethod("onDisable").invoke(instance);
                            logger.info("Stopped \"" + name + "\"!");
                        } catch (Throwable throwable) {
                            logger.log(Level.SEVERE, "Error while disabling!", throwable);
                        } finally {
                            threads.remove(name);
                            instances.remove(name);

                            Method hackThreadStop = RunnerPlugin.class.getDeclaredMethod("stopThreadHacker");
                            hackThreadStop.setAccessible(true);
                            hackThreadStop.invoke(instance);
                        }
                    } else
                        logger.severe("Instance not found!");
                else
                    logger.severe("Thread not found!");
            } catch (Throwable throwable) {
                logger.log(Level.SEVERE, "Error!", throwable);
            }
            future.setValue(null);
        }, "Stopper Thread [" + name + "] ").start();

        return future;
    }
}
