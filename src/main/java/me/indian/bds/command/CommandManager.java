package me.indian.bds.command;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.defaults.BackupCommand;
import me.indian.bds.command.defaults.DeathsCommand;
import me.indian.bds.command.defaults.HelpCommand;
import me.indian.bds.command.defaults.LinkCommand;
import me.indian.bds.command.defaults.PlaytimeCommand;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final List<Command> commandList;

    public CommandManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.commandList = new ArrayList<>();
        this.registerCommand(new HelpCommand(this.bdsAutoEnable, this.commandList));
        this.registerCommand(new BackupCommand(this.bdsAutoEnable));
        this.registerCommand(new PlaytimeCommand(this.bdsAutoEnable));
        this.registerCommand(new DeathsCommand(this.bdsAutoEnable));
        this.registerCommand(new LinkCommand(this.bdsAutoEnable));
    }


    public void registerCommand(final Command command) {
        if(this.commandList.stream().anyMatch(command1 -> command1.getName().equals(command.getName()))){
            throw new RuntimeException("Komenda o nazwie " + command.getName() +" już istnieje!");
        }

        this.commandList.add(command);
    }

    public void runCommands(final String playerName, final String commandName, final String[] args, final boolean isOp) {
        for (final Command command : this.commandList) {
            if (command.getName().equals(commandName)) {
                command.onExecute(playerName, args, isOp);
                return;
            }
        }
        this.bdsAutoEnable.getServerProcess().tellrawToPlayer(playerName, "&cNie znaleziono takiego polecenia");
    }
}