package me.indian.bds.command.defaults;

import me.indian.bds.command.Command;
import me.indian.bds.command.CommandSender;

import java.util.List;

public class HelpCommand extends Command {

    private final List<Command> commandList;

    public HelpCommand(final List<Command> commandsList) {
        super("help", "Lista pomocnych poleceń");
        this.commandList = commandsList;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        this.sendMessage("&a---------------------");
        if (this.commandSender == CommandSender.PLAYER) this.sendMessage("&a!tps&4-&b ilość tików na sekundę servera");
        for (final Command command : this.commandList) {
            this.sendMessage("&a" + command.getName() + "&4-&b " + command.getDescription());
        }
        this.sendMessage("&a---------------------");

        return true;
    }
}