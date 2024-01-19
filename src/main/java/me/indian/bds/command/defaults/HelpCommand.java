package me.indian.bds.command.defaults;

import java.util.List;
import me.indian.bds.command.Command;

public class HelpCommand extends Command {

    private final List<Command> commandList;

    public HelpCommand(final List<Command> commandsList) {
        super("help", "Lista pomocnych poleceń");
        this.commandList = commandsList;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        this.sendMessage("&a---------------------");
        for (final Command command : this.commandList) {
            this.sendMessage("&a" + command.getName() + " &4-&b " + command.getDescription());
        }
        this.sendMessage("&a---------------------");

        return true;
    }
}