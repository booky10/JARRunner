package tk.t11e.runner.utils;
// Created by booky10 in JARRunner (09:43 06.09.20)

import com.sun.xml.internal.ws.util.StringUtils;
import tk.t11e.runner.Boot;
import tk.t11e.runner.logger.RunnerLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnerManager {

    public static final File jarDirectory = new File("plugins");
    public static final HashMap<String, Thread> threads = new HashMap<>();
    private static final HashMap<String, RunnerPlugin> instances = new HashMap<>();
    private static final Logger logger = RunnerLogger.getLogger();
    private static BetterURLClassLoader loader;
    private static final Boolean threadLock = false;

    static {
        if (!jarDirectory.exists()) jarDirectory.mkdirs();
    }

    public static void startJAR(String jar) {
        jar = jar.toLowerCase().replace(' ', '_');

        for (String name : jar.split(",")) {
            Thread thread = new Thread(() -> {
                synchronized (threadLock) {
                    try {
                        File file = new File(jarDirectory, name + ".jar");
                        if (!file.exists()) {
                            File[] files = jarDirectory.listFiles();
                            if (files != null) for (File jarFile : files)
                                if (jarFile.getName().equalsIgnoreCase(name + ".jar")) {
                                    file = jarFile;
                                    break;
                                }
                            if (!file.exists()) throw new FileNotFoundException("JAR does not exits!");
                        }

                        if (loader == null)
                            loader = new BetterURLClassLoader(Boot.class.getClassLoader(), file.toURI().toURL());
                        else
                            loader.addURLs(file.toURI().toURL());

                        URLClassLoader simpleLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
                        URL manifestURL = simpleLoader.getResource("RunnerPlugin.MF");

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
                            if (main == null)
                                logger.severe("Could not find Runner-Main in manifest!");
                            else {
                                Class<?> mainClass = loader.loadClass(main);
                                if (mainClass != null) {
                                    try {
                                        if (RunnerPlugin.class.isAssignableFrom(mainClass)) {
                                            Constructor<? extends RunnerPlugin> constructor = (Constructor<? extends RunnerPlugin>) mainClass.getDeclaredConstructor(String.class);
                                            constructor.setAccessible(true);
                                            RunnerPlugin instance = constructor.newInstance(name);

                                            try {
                                                new Thread(() -> {
                                                    try {
                                                        instance.onLoad();

                                                        new Thread(() -> {
                                                            try {
                                                                instance.onEnable();
                                                            } catch (Throwable throwable) {
                                                                throw new Error(throwable);
                                                            }
                                                        }, "Enabler [" + StringUtils.capitalize(name) + "]").start();
                                                    } catch (Throwable throwable) {
                                                        ThreadUtils.sleep(500);
                                                        stopJAR(name);
                                                        throw new Error(throwable);
                                                    }
                                                }, "Loader [" + StringUtils.capitalize(name) + "]").start();

                                                logger.info("Started \"" + name + "\"!");
                                                instances.put(name, instance);

                                                new Thread(() -> {
                                                    try {
                                                        instance.startThreadHacker();
                                                    } catch (Throwable throwable) {
                                                        throw new Error(throwable);
                                                    }
                                                }, "Thread Hacker [" + StringUtils.capitalize(name) + "]").start();
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
                }
            }, "Starter Thread [" + StringUtils.capitalize(name) + "]");

            threads.put(name, thread);
            thread.start();
        }
    }

    public static CompletableInFuture<Boolean> stopJAR(String jar) {
        CompletableInFuture<Boolean> future = new CompletableInFuture<>();

        jar = jar.toLowerCase().replace(' ', '_');
        String name = jar;

        new Thread(() -> {
            try {
                if (threads.containsKey(name))
                    if (instances.containsKey(name)) {
                        RunnerPlugin instance = instances.get(name);
                        threads.remove(name);
                        instances.remove(name);
                        loader.removeURLs();

                        try {
                            instance.disable();
                            logger.info("Stopped \"" + name + "\"!");
                            future.setValue(true);
                        } catch (Throwable throwable) {
                            future.setValue(false);
                            logger.log(Level.SEVERE, "Error while disabling!", throwable);
                        } finally {
                            instance.stopThreadHacker();
                        }
                    } else {
                        future.setValue(false);
                        logger.severe("Instance not found!");
                    }
                else {
                    future.setValue(false);
                    logger.severe("Thread not found!");
                }
            } catch (Throwable throwable) {
                future.setValue(false);
                logger.log(Level.SEVERE, "Error!", throwable);
            }
        }, "Stopper Thread [" + StringUtils.capitalize(name) + "]").start();

        return future;
    }
}