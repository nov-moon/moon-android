package com.meili.moon.sdk.app.util;

public class ProgressCount {
    private int count = 0;

    public synchronized int increment() {
        return count = count + 1;
    }

    public synchronized int decrement() {
        return count = count - 1;
    }

    public synchronized int cleanCount() {
        count = 0;
        return count;
    }

    public synchronized int getCount() {
        return count;
    }
}
