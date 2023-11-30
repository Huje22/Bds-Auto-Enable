package me.indian.bds;

import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.ThreadUtil;

import java.util.Scanner;

public class ConsoleInput {

    private final Scanner scanner;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private final DiscordIntegration discord;

    public ConsoleInput(final Scanner scanner, final BDSAutoEnable bdsAutoEnable) {
        this.scanner = scanner;
        this.logger = bdsAutoEnable.getLogger();
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.discord = bdsAutoEnable.getDiscord();

        this.handleCommands();
    }

    private void handleCommands() {

        //Ulepszyc to , dodac coś jak CommandManager ale dla konsoli

        new ThreadUtil("ConsoleInput").newThread(() -> {
            try {
                while (this.scanner.hasNext()) {
                    final String input = this.scanner.nextLine();
                    this.serverProcess.sendToConsole(input);
                    this.logger.instantLogToFile(input);
                    this.discord.writeConsole(input);
                }
                System.out.println("Koniec");
            } catch (final Exception exception) {
                this.logger.critical("Wypisywanie konsoli uległo awarii , powoduje to wyłączenie aplikacji ", exception);
                this.discord.sendEmbedMessage("ServerProcess",
                        "Wypisywanie konsoli uległo awarii , powoduje to wyłączenie aplikacji",
                        exception,
                        exception.getLocalizedMessage());
                this.discord.sendMessage("<owner>");
                System.exit(0);
            }
        }).start();
    }
}