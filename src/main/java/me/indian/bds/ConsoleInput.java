package me.indian.bds;

import java.util.Scanner;
import me.indian.bds.command.CommandManager;
import me.indian.bds.command.CommandSender;
import me.indian.bds.discord.DiscordHelper;
import me.indian.bds.discord.embed.component.Footer;
import me.indian.bds.discord.jda.DiscordJDA;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;

public class ConsoleInput {

    private final String prefix;
    private final Scanner mainScanner;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private final DiscordHelper discordHelper;
    private final DiscordJDA discordJDA;
    private final CommandManager commandManager;

    public ConsoleInput(final Scanner mainScanner, final BDSAutoEnable bdsAutoEnable) {
        this.prefix = "&b[&eConsole&3Input&b] ";
        this.mainScanner = mainScanner;
        this.logger = bdsAutoEnable.getLogger();
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.discordHelper = bdsAutoEnable.getDiscordHelper();
        this.discordJDA = this.discordHelper.getDiscordJDA();
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
                    this.discordJDA.writeConsole(input);

                    final boolean done = this.commandManager.runCommands(CommandSender.CONSOLE,
                            "CONSOLE", args[0], newArgs, true);

                    if (done) continue;

                    this.serverProcess.sendToConsole(input);
                }
                this.logger.alert("Konsola zakończyła działanie");
            } catch (final Exception exception) {
                this.logger.critical("Konsola aplikacji uległa awarii , powoduje to wyłączenie aplikacji ");
                this.discordHelper.getWebHook().sendEmbedMessage("ServerProcess",
                        "Wypisywanie konsoli uległo awarii , powoduje to wyłączenie aplikacji",
                        exception,
                        new Footer(exception.getLocalizedMessage()));
                this.discordHelper.getWebHook().sendMessage("<owner>");

                throw exception;
            }
        }).start();
    }
}
