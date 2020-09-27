package tk.t11e.runner.utils;
// Created by booky10 in JARRunner (19:16 07.09.20)

import sun.misc.URLClassPath;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Stack;

public class BetterURLClassLoader extends URLClassLoader {

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
            Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
            ucpField.setAccessible(true);
            URLClassPath ucp = (URLClassPath) ucpField.get(this);

            Field closedField = URLClassPath.class.getDeclaredField("closed");
            closedField.setAccessible(true);
            Boolean isClosed = (Boolean) closedField.get(ucp);

            Field urlsField = URLClassPath.class.getDeclaredField("urls");
            urlsField.setAccessible(true);
            Stack<URL> urlStack = (Stack<URL>) urlsField.get(ucp);

            Field pathField = URLClassPath.class.getDeclaredField("urls");
            pathField.setAccessible(true);
            ArrayList<URL> pathList = (ArrayList<URL>) pathField.get(ucp);

            Field lookupCacheURLsField = URLClassPath.class.getDeclaredField("lookupCacheURLs");
            lookupCacheURLsField.setAccessible(true);
            URL[] lookupCacheURLs = (URL[]) lookupCacheURLsField.get(ucp);

            Method disableAllLookupCachesMethod = URLClassPath.class.getDeclaredMethod("disableAllLookupCaches");
            disableAllLookupCachesMethod.setAccessible(true);

            if (!isClosed)
                for (URL url : urls)
                    if (url != null && pathList.contains(url)) {
                        urlStack.remove(url);
                        pathList.remove(url);

                        if (lookupCacheURLs != null)
                            disableAllLookupCachesMethod.invoke(ucp);
                    }

            urlsField.set(ucp, urlStack);
            pathField.set(ucp, pathList);
            ucpField.set(this, ucp);
        } catch (NoSuchFieldException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException exception) {
            throw new Error(exception);
        }
    }
}