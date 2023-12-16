package me.indian.bds;

import java.util.Scanner;
import me.indian.bds.command.CommandManager;
import me.indian.bds.command.CommandSender;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.component.Footer;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;

public class ConsoleInput {

    private final String prefix;
    private final Scanner scanner;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private final DiscordIntegration discord;
    private final CommandManager commandManager;

    public ConsoleInput(final Scanner scanner, final BDSAutoEnable bdsAutoEnable) {
        this.prefix = "&b[&eConsole&3Input&b] ";
        this.scanner = scanner;
        this.logger = bdsAutoEnable.getLogger();
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.discord = bdsAutoEnable.getDiscord();
        this.commandManager = bdsAutoEnable.getCommandManager();

        this.handleCommands();
    }

    private void handleCommands() {
        new ThreadUtil("ConsoleInput").newThread(() -> {
            try {
                while (this.scanner.hasNext()) {
                    final String input = this.scanner.nextLine();
                    final String[] args = MessageUtil.stringToArgs(input);
                    final String[] newArgs = MessageUtil.removeArgs(args, 1);

                    this.logger.instantLogToFile(input);
                    this.discord.writeConsole(input);

                    final boolean done = this.commandManager.runCommands(CommandSender.CONSOLE,
                            "CONSOLE", args[0], newArgs, true);

                    if (done) continue;

                    this.someChangesForCommands(args[0]);
                    this.serverProcess.sendToConsole(input);
                }
            } catch (final Exception exception) {
                this.logger.critical("Wypisywanie konsoli uległo awarii , powoduje to wyłączenie aplikacji ", exception);
                this.discord.sendEmbedMessage("ServerProcess",
                        "Wypisywanie konsoli uległo awarii , powoduje to wyłączenie aplikacji",
                        exception,
                        new Footer(exception.getLocalizedMessage()));
                this.discord.sendMessage("<owner>");
                System.exit(0);
            }
        }).start();
    }

    private void someChangesForCommands(final String command) {
        switch (command.toLowerCase()) {
            case "stop" -> {
                if(!this.serverProcess.isEnabled()) return;
                this.serverProcess.tellrawToAllAndLogger(this.prefix, "&4Zamykanie servera...", LogState.ALERT);
                this.serverProcess.kickAllPlayers(this.prefix + "&cKtoś wykonał&a stop &c w konsoli servera , co skutkuje  restartem");
                if (!Thread.currentThread().isInterrupted()) ThreadUtil.sleep(2);
            }
        }
    }
}