package me.indian.bds.util;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ThreadFactory;
import org.jetbrains.annotations.NotNull;

public class ThreadUtil implements ThreadFactory {

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
        } catch (final InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException exception) {
            exception.printStackTrace();;
        }
    }

    public static int getThreadsCount() {
        int availableThreads = 2;
        try {
            availableThreads = ManagementFactory.getThreadMXBean().getThreadCount();
        } catch (final Exception ignore) {
        }
        return availableThreads;
    }

    @Override
    public Thread newThread(@NotNull final Runnable runnable) {
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
