package me.indian.bds.command.defaults;

import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;

public class AlertCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProcess serverProcess;

    public AlertCommand(final BDSAutoEnable bdsAutoEnable) {
        super("alert", "Ważne informacje na czat");
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProcess = bdsAutoEnable.getServerProcess();

        this.addOption("<message>", "Wiadomość");
        this.addAlliases(List.of("b"));

    }


    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            return true;
        }

        if (args.length > 0) {
            final String[] newArgs = MessageUtil.buildMessageFromArgs(args).split("-");

            this.serverProcess.tellrawToAll("&a---------&cAlert&a------------");
            for (int i = 0; i < newArgs.length; i++) {
                this.serverProcess.tellrawToAll(newArgs[i]);
            }
            this.serverProcess.tellrawToAll("&a----------------------------");

            return true;
        }

        return false;
    }
}
