package me.indian.bds.util;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for thread management and creation.
 * Docs writed by Chat-GTP
 */
public final class ThreadUtil implements ThreadFactory {

    private final String threadName;
    private final Runnable runnable;
    private final boolean daemon;
    private final AtomicInteger threadCount = new AtomicInteger(0);

    /**
     * Constructs a new ThreadUtil with the specified thread name prefix.
     *
     * @param threadName the prefix for thread names created by this factory
     */
    public ThreadUtil(final String threadName) {
        this(threadName, null, false);
    }

    /**
     * Constructs a new ThreadUtil with the specified thread name prefix and daemon flag.
     *
     * @param threadName the prefix for thread names created by this factory
     * @param daemon     if true, created threads will be daemon threads
     */
    public ThreadUtil(final String threadName, final boolean daemon) {
        this(threadName, null, daemon);
    }

    /**
     * Constructs a new ThreadUtil with the specified thread name prefix and runnable.
     *
     * @param threadName the prefix for thread names created by this factory
     * @param runnable   the runnable to be executed by created threads
     */
    public ThreadUtil(final String threadName, final Runnable runnable) {
        this(threadName, runnable, false);
    }

    /**
     * Constructs a new ThreadUtil with the specified thread name prefix, runnable, and daemon flag.
     *
     * @param threadName the prefix for thread names created by this factory
     * @param runnable   the runnable to be executed by created threads
     * @param daemon     if true, created threads will be daemon threads
     */
    public ThreadUtil(final String threadName, final Runnable runnable, final boolean daemon) {
        this.threadName = threadName + "-%d";
        this.runnable = runnable;
        this.daemon = daemon;
    }

    /**
     * Causes the current thread to sleep for the specified number of seconds.
     *
     * @param seconds the number of seconds to sleep
     */
    public static void sleep(final int seconds) {
        try {
            Thread.sleep(1000L * seconds);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            exception.printStackTrace();
        }
    }

    /**
     * Causes the current thread to sleep for the specified number of milliseconds.
     *
     * @param millis the number of milliseconds to sleep
     */
    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            exception.printStackTrace();
        }
    }

    /**
     * Returns the number of available logical processors.
     *
     * @return the number of available logical processors
     */
    public static int getLogicalThreads() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Returns the current number of active threads in the JVM.
     *
     * @return the current number of active threads
     */
    public static int getThreadsCount() {
        return ManagementFactory.getThreadMXBean().getThreadCount();
    }

    /**
     * Returns the peak number of threads since the JVM started or peak reset.
     *
     * @return the peak number of threads
     */
    public static int getPeakThreadsCount() {
        return ManagementFactory.getThreadMXBean().getPeakThreadCount();
    }

    /**
     * Checks if the current thread is important based on its name.
     *
     * @return true if the current thread is important, false otherwise
     */
    public static boolean isImportantThread() {
        final String threadName = Thread.currentThread().getName();
        return threadName.contains("Console") || threadName.contains("Server process");
    }

    @Override
    public Thread newThread(@NotNull final Runnable runnable) {
        final Thread thread = new Thread(runnable);
        thread.setName(this.generateThreadName());
        thread.setDaemon(this.daemon);
        return thread;
    }

    /**
     * Creates a new thread with the specified runnable.
     *
     * @return the new thread
     */
    public Thread newThread() {
        final Thread thread = new Thread(this.runnable);
        thread.setName(this.generateThreadName());
        thread.setDaemon(this.daemon);
        return thread;
    }

    /**
     * Returns the thread name prefix used by this factory.
     *
     * @return the thread name prefix
     */
    public String getThreadName() {
        return this.threadName;
    }

    /**
     * Returns if the created threads are daemon threads.
     *
     * @return true if created threads are daemon threads, false otherwise
     */
    public boolean isDaemon() {
        return this.daemon;
    }

    private String generateThreadName() {
        return this.threadName.replace("%d", String.valueOf(this.threadCount.incrementAndGet()));
    }
}