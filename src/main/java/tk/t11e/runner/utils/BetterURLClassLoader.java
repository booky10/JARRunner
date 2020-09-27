package tk.t11e.runner.utils;
// Created by booky10 in JARRunner (19:16 07.09.20)

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Stack;

public class BetterURLClassLoader extends URLClassLoader {

    private static final Field ucpField, closedField, urlsField, pathField, lookupCacheURLsField;
    private static final Method disableAllLookupCachesMethod;
    private static final Class<?> ucpClass;

    static {
        try {
            ucpField = URLClassLoader.class.getDeclaredField("ucp");
            ucpClass = Class.forName("sun.misc.URLClassPath");
            closedField = ucpClass.getDeclaredField("closed");
            urlsField = ucpClass.getDeclaredField("urls");
            pathField = ucpClass.getDeclaredField("urls");
            lookupCacheURLsField = ucpClass.getDeclaredField("lookupCacheURLs");
            disableAllLookupCachesMethod = ucpClass.getDeclaredMethod("disableAllLookupCaches");

            ucpField.setAccessible(true);
            closedField.setAccessible(true);
            urlsField.setAccessible(true);
            pathField.setAccessible(true);
            lookupCacheURLsField.setAccessible(true);
            disableAllLookupCachesMethod.setAccessible(true);
        } catch (Throwable throwable) {
            throw new Error(throwable);
        }
    }

    public BetterURLClassLoader(ClassLoader parent, URL... urls) {
        super(urls, parent);
    }

    public BetterURLClassLoader(URL... urls) {
        super(urls);
    }

    public BetterURLClassLoader(ClassLoader parent, URLStreamHandlerFactory factory, URL... urls) {
        super(urls, parent, factory);
    }

    public void addURLs(URL... urls) {
        for (URL url : urls) addURL(url);
    }

    public void removeURLs(URL... urls) {
        if (urls == null || urls.length == 0) return;
        try {
            Object ucp = ucpField.get(this);
            Boolean isClosed = (Boolean) closedField.get(ucp);
            Stack<URL> urlStack = (Stack<URL>) urlsField.get(ucp);
            ArrayList<URL> pathList = (ArrayList<URL>) pathField.get(ucp);
            URL[] lookupCacheURLs = (URL[]) lookupCacheURLsField.get(ucp);

            if (!isClosed) for (URL url : urls)
                if (url != null && pathList.contains(url)) {
                    urlStack.remove(url);
                    pathList.remove(url);

                    if (lookupCacheURLs != null) disableAllLookupCachesMethod.invoke(ucp);
                }

            urlsField.set(ucp, urlStack);
            pathField.set(ucp, pathList);
            ucpField.set(this, ucp);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new Error(exception);
        }
    }
}