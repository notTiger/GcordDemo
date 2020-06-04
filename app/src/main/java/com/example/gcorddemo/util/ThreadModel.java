package com.example.gcorddemo.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public enum ThreadModel {
    instance;
    public void execute(Runnable task) {
        ExecutorHolder.cacheExecutor.execute(task);
    }

    private static class ExecutorHolder {
        private final static Executor cacheExecutor;

        static {
            cacheExecutor = Executors.newCachedThreadPool();
        }
    }
}
