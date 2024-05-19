package me.indian.bds;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.indian.bds.command.CommandManager;
import me.indian.bds.event.server.ServerAlertEvent;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;

public class ConsoleInput {

    private final BDSAutoEnable bdsAutoEnable;
    private final String prefix;
    private final Scanner mainScanner;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private final CommandManager commandManager;
    private final ExecutorService service;

    public ConsoleInput(final Scanner mainScanner, final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.prefix = "&b[&eConsole&3Input&b] ";
        this.mainScanner = mainScanner;
        this.logger = bdsAutoEnable.getLogger();
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.commandManager = bdsAutoEnable.getCommandManager();
        this.service = Executors.newFixedThreadPool(4, new ThreadUtil("ConsoleInput"));

        this.service.execute(this::handleCommands);
    }

    private void handleCommands() {
            try {
                this.logger.info("&aUruchomiono konsole");
                while (this.mainScanner.hasNext()) {
                    final String input = this.mainScanner.nextLine();
                    final String[] args = MessageUtil.stringToArgs(input);
                    final String[] newArgs = MessageUtil.removeFirstArgs(args);

                    this.service.execute(() -> {
                        this.logger.instantLogToFile(input);

                        final boolean done = this.commandManager.runCommands(null, args[0], newArgs, null, true);

                        if (done) return;

                        this.serverProcess.sendToConsole(input);
                    });
                }
                this.logger.alert("Konsola zakończyła działanie");
            } catch (final Exception exception) {
                this.logger.critical("Konsola aplikacji uległa awarii , powoduje to wyłączenie aplikacji ");
                this.bdsAutoEnable.getEventManager()
                        .callEvent(new ServerAlertEvent("Konsola aplikacji uległa awarii , powoduje to wyłączenie aplikacji ", exception, LogState.CRITICAL));

                try {
                    this.serverProcess.setCanRun(false);
                    this.serverProcess.disableServer();
                    this.serverProcess.waitFor();
                } catch (final InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                System.exit(1);
                throw exception;
            }
    }
}