package me.indian.bds;

import me.indian.bds.config.AppConfig;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;

public class ShutdownHandler {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfig appConfig;
    private final ServerProcess serverProcess;

    public ShutdownHandler(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfig = this.bdsAutoEnable.getAppConfigManager().getAppConfig();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();


        this.shutdownHook();
    }

    private void shutdownHook() {
        final Thread shutdown = new Thread(() -> {
            try {
                this.bdsAutoEnable.getExtensionLoader().disableExtensions();
                this.serverProcess.instantShutdown();
            } catch (final Exception exception) {
                this.logger.critical("Wystąpił błąd podczas próby uruchomienia shutdown hooku ", exception);
            }
        });
        shutdown.setName("Shutdown");
        Runtime.getRuntime().addShutdownHook(shutdown);
    }
}
