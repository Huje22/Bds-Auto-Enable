package me.indian.bds;

import me.indian.bds.config.AppConfig;
import me.indian.bds.discord.embed.component.Footer;
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
        this.handleUncaughtException();
    }

    private void shutdownHook() {
        final Thread shutdown = new Thread(() -> {
            try {
                this.serverProcess.instantShutdown();
            } catch (final Exception exception) {
                this.logger.critical("Wystąpił błąd podczas próby uruchomienia shutdown hooku ", exception);
            }
        });
        shutdown.setName("Shutdown");
        Runtime.getRuntime().addShutdownHook(shutdown);
    }

    private void handleUncaughtException() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            final boolean closeOnException = this.appConfig.isCloseOnException();

            this.logger.critical("Wystąpił niezłapany wyjątek w wątku&b " + thread.getName());
            this.logger.logThrowableToFile(throwable);

            this.bdsAutoEnable.getDiscordHelper().getWebHook().sendEmbedMessage("Wystąpił niezłapany wyjątek w wątku** " + thread.getName() + "**",
                    (closeOnException ? "**Aplikacja zostaje zamknięta z tego powodu**" : ""),
                    throwable,
                    new Footer(throwable.getLocalizedMessage()));

            if (closeOnException) {
                this.logger.alert("Zamykanie aplikacji z powodu niezłapanego wyjątku");
                System.exit(0);
            }

            if (this.bdsAutoEnable.isMainThread()) System.exit(0);
        });
    }
}
