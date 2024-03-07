package me.indian.bds.shutdown;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfig;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.ThreadUtil;

public class ShutdownHandler {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfig appConfig;
    private final ServerProcess serverProcess;

    public ShutdownHandler(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfig = bdsAutoEnable.getAppConfigManager().getAppConfig();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();

        this.shutdownHook();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(bdsAutoEnable));
    }

    private void shutdownHook() {
        final ThreadUtil shutdownThread = new ThreadUtil("Shutdown");

        Runtime.getRuntime().addShutdownHook(shutdownThread.newThread(() -> {
            try {
                this.serverProcess.instantShutdown();
            } catch (final Exception exception) {
                this.logger.critical("Wystąpił błąd podczas próby uruchomienia shutdown hooku ", exception);
            }
        }));
    }
}