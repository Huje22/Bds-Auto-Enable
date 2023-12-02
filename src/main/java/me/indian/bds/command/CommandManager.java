package me.indian.bds.command;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.defaults.BackupCommand;
import me.indian.bds.command.defaults.DeathsCommand;
import me.indian.bds.command.defaults.HelpCommand;
import me.indian.bds.command.defaults.LinkCommand;
import me.indian.bds.command.defaults.PlaytimeCommand;
import me.indian.bds.command.defaults.VersionCommand;
import me.indian.bds.server.ServerProcess;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProcess serverProcess;
    private final List<Command> commandList;

    public CommandManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.commandList = new ArrayList<>();
        this.registerCommand(new HelpCommand(this.commandList));
        this.registerCommand(new BackupCommand(this.bdsAutoEnable));
        this.registerCommand(new PlaytimeCommand(this.bdsAutoEnable));
        this.registerCommand(new DeathsCommand());
        this.registerCommand(new LinkCommand(this.bdsAutoEnable));
        this.registerCommand(new VersionCommand(this.bdsAutoEnable));
    }

    public <T extends Command> void registerCommand(final T command) {
        if (this.commandList.stream().anyMatch(command1 -> command1.getName().equals(command.getName()))) {
            throw new RuntimeException("Komenda o nazwie " + command.getName() + " już istnieje!");
        }

        this.commandList.add(command);
    }

    public boolean runCommands(final CommandSender sender, final String playerName, final String commandName, final String[] args, final boolean isOp) {
        for (final Command command : this.commandList) {
            if (command.getName().equals(commandName)) {
                command.setCommandSender(sender);
                command.setBdsAutoEnable(this.bdsAutoEnable);
                command.setPlayerName(playerName);

                if (!command.onExecute(sender, args, isOp) && !command.getUsage().isEmpty()) {
                    switch (sender) {
                        case CONSOLE -> this.bdsAutoEnable.getLogger().print(command.getUsage());
                        case PLAYER -> this.serverProcess.tellrawToPlayer(playerName, command.getUsage());
                    }
                }
                return true;
            }
        }

        if (sender == CommandSender.PLAYER) {
            this.serverProcess.tellrawToPlayer(playerName, "&cNie znaleziono takiego polecenia");
        }

        return false;
    }
}