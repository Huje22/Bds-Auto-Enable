package pl.indianbartonka.bds.shutdown;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.logger.Logger;

public class ShutdownHandler {

    private static boolean SHUTDOWN_HOOK_CALLED = false;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ServerProcess serverProcess;

    public ShutdownHandler(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();

        this.shutdownHook();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(bdsAutoEnable));
    }

    public static boolean isShutdownHookCalled() {
        return SHUTDOWN_HOOK_CALLED;
    }

    private void shutdownHook() {
        final ThreadUtil shutdownThread = new ThreadUtil("Shutdown");

        Runtime.getRuntime().addShutdownHook(shutdownThread.newThread(() -> {
            SHUTDOWN_HOOK_CALLED = true;
            try {
                this.serverProcess.instantShutdown();
                this.bdsAutoEnable.getMcLog().sendCurrentLog();
            } catch (final Exception exception) {
                this.logger.critical("Wystąpił błąd podczas próby uruchomienia shutdown hooku ", exception);
            }
        }));
    }
}