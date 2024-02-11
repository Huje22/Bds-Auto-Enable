package me.indian.bds.command.defaults;

import java.util.Set;
import me.indian.bds.command.Command;

public class HelpCommand extends Command {

    private final Set<Command> commandSet;

    public HelpCommand(final  Set<Command> commandSet) {
        super("help", "Lista pomocnych poleceń");
        this.commandSet = commandSet;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        this.sendMessage("&a---------------------");
        this.commandSet.forEach(command -> this.sendMessage("&a" + command.getName() + " &4-&b " + command.getDescription()));
        this.sendMessage("&a---------------------");
        return true;
    }
}