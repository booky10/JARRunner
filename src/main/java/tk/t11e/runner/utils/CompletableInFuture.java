package tk.t11e.runner.utils;
// Created by booky10 in JARRunner (11:54 06.09.20)

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CompletableInFuture<T> {

    private T value;
    private final List<Consumer<T>> consumers = new ArrayList<>();

    public void setValue(T value) {
        this.value = value;

        for (Consumer<T> consumer : consumers)
            consumer.accept(value);
    }

    public void getValue(Consumer<T> consumer) {
        consumers.add(consumer);
        if (value != null) consumer.accept(value);
    }

    public void clear() {
        value = null;
        consumers.clear();
    }
}