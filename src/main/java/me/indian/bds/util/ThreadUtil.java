package me.indian.bds.util;

import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ThreadFactory;

public final class ThreadUtil implements ThreadFactory {

    private final String threadName;
    private final Runnable runnable;
    private final boolean deamon;
    private int threadCount;

    public ThreadUtil(final String threadName) {
       this(threadName , null, false);
    }

    public ThreadUtil(final String threadName,final Runnable runnable) {
       this(threadName , runnable, false);
    }

    public ThreadUtil(final String threadName, final Runnable runnable, final boolean deamon) {
        this.threadName = threadName + "-%b";
        this.runnable = runnable;
        this.threadCount = 0;
        this.deamon = deamon;
    }

    public static void sleep(final int seconds) {
        try {
            Thread.sleep(1000L * seconds);
        } catch (final InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException exception) {
            exception.printStackTrace();
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

    public static boolean isImportantThread() {
        final String threadName = Thread.currentThread().getName();
        return threadName.contains("Console") || threadName.contains("Server process");
    }

    @Override
    public Thread newThread(@NotNull final Runnable runnable) {
        final Thread thread = new Thread(runnable);
        thread.setName(this.generateThreadName());
        thread.setDaemon(deamon);
        return thread;
    }

    public Thread newThread() {
        final Thread thread = new Thread(this.runnable);
        thread.setName(this.generateThreadName());
        thread.setDaemon(deamon);
        return thread;
    }

    private String generateThreadName() {
        this.threadCount++;
        return this.threadName.replace("%b", String.valueOf(this.threadCount));
    }
}
