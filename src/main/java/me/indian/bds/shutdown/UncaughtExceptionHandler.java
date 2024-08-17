package me.indian.bds.shutdown;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfig;
import me.indian.bds.event.server.ServerUncaughtExceptionEvent;
import me.indian.util.logger.Logger;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfig appConfig;

    public UncaughtExceptionHandler(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = bdsAutoEnable.getLogger();
        this.appConfig = bdsAutoEnable.getAppConfigManager().getAppConfig();
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable throwable) {
        if (throwable instanceof Error) {
            this.logger.critical("Wystąpił niezłapany błąd w wątku&b " + thread.getName(), throwable);
        } else {
            this.logger.critical("Wystąpił niezłapany wyjątek w wątku&b " + thread.getName(), throwable);
        }
        this.bdsAutoEnable.getEventManager().callEvent(new ServerUncaughtExceptionEvent(thread, throwable));

        if (this.appConfig.isCloseOnException() || this.bdsAutoEnable.isMainThread(thread)) {
            System.exit(20);
        }
    }
}