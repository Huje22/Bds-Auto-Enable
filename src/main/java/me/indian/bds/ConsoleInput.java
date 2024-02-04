package me.indian.bds;

import me.indian.bds.command.CommandManager;
import me.indian.bds.command.CommandSender;
import me.indian.bds.event.server.ConsoleCommandEvent;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;

import java.util.Arrays;
import java.util.Scanner;

public class ConsoleInput {

    private final BDSAutoEnable bdsAutoEnable;
    private final String prefix;
    private final Scanner mainScanner;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private final CommandManager commandManager;

    public ConsoleInput(final Scanner mainScanner, final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.prefix = "&b[&eConsole&3Input&b] ";
        this.mainScanner = mainScanner;
        this.logger = bdsAutoEnable.getLogger();
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.commandManager = bdsAutoEnable.getCommandManager();

        this.handleCommands();
    }

    private void handleCommands() {
        new ThreadUtil("ConsoleInput").newThread(() -> {
            try {
                while (this.mainScanner.hasNext()) {
                    final String input = this.mainScanner.nextLine();
                    final String[] args = MessageUtil.stringToArgs(input);
                    final String[] newArgs = MessageUtil.removeFirstArgs(args);

                    this.logger.instantLogToFile(input);

                    final boolean done = this.commandManager.runCommands(CommandSender.CONSOLE,
                            "CONSOLE", args[0], newArgs, true);

                    if (done) continue;

                    this.bdsAutoEnable.getEventManager()
                            .callEventWithResponse(new ConsoleCommandEvent(args[0] + " " + Arrays.toString(newArgs)));
                    this.serverProcess.sendToConsole(input);
                }
                this.logger.alert("Konsola zakończyła działanie");
            } catch (final Exception exception) {
                this.logger.critical("Konsola aplikacji uległa awarii , powoduje to wyłączenie aplikacji ");

                throw exception;
            }
        }).start();
    }
}
