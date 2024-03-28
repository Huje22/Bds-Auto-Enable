package me.indian.bds.command.defaults;

import java.util.Map;
import me.indian.bds.command.Command;
import me.indian.bds.extension.Extension;

public class HelpCommand extends Command {

    private final Map<Command, Extension> commandMap;

    public HelpCommand(final Map<Command, Extension> commandMap) {
        super("help", "Lista pomocnych poleceń");
        this.commandMap = commandMap;
    }


    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        this.sendMessage("&a---------------------");
        this.commandMap.forEach((command, extension) -> this.sendMessage("&a" + command.getName() + " &4-&b " + command.getDescription()));
        this.sendMessage("&a---------------------");
        return true;
    }
}