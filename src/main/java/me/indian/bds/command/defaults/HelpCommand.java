package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.server.ServerProcess;

import java.util.List;

public class HelpCommand extends Command {

    private final ServerProcess serverProcess;
    private final List<Command> commandList;

    public HelpCommand(final BDSAutoEnable bdsAutoEnable, final List<Command> commandsList) {
        super("help", "Lista pomocnych poleceń");
        this.commandList = commandsList;
        this.serverProcess = bdsAutoEnable.getServerProcess();
    }

    @Override
    public boolean onExecute(final String player, final String[] args, final boolean isOp) {
        this.serverProcess.tellrawToPlayer(player, "&a---------------------");
        this.serverProcess.tellrawToPlayer(player, "&a!tps&4-&b ilość tików na sekundę servera");
        for (final Command command : this.commandList) {
            this.serverProcess.tellrawToPlayer(player, "&a" + command.getName() + "&4-&b " + command.getDescription());
        }
        this.serverProcess.tellrawToPlayer(player, "&a---------------------");
        return false;
    }
}
