package me.indian.bds.util;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ThreadFactory;

public class ThreadUtil extends Thread implements ThreadFactory {

    private final String threadName;
    private final Runnable runnable;
    private int threadCount;

    public ThreadUtil(final String threadName) {
        this.threadName = threadName + "-%b";
        this.runnable = null;
        this.threadCount = 0;
    }

    public ThreadUtil(final String threadName, final Runnable runnable) {
        this.threadName = threadName + "-%b";
        this.runnable = runnable;
        this.threadCount = 0;
    }

    public static void sleep(final int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getThreadsCount() {
        int availableThreads = 2;
        try {
            availableThreads = ManagementFactory.getThreadMXBean().getThreadCount();
        } catch (Exception ignore) {
        }
        return availableThreads;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(generateThreadName());
        return thread;
    }

    public Thread newThread() {
        Thread thread = new Thread(runnable);
        thread.setName(generateThreadName());
        return thread;
    }

    private String generateThreadName() {
        this.threadCount++;
        return this.threadName.replace("%b", String.valueOf(threadCount));
    }
}
