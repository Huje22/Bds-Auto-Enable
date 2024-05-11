package me.indian.bds.command.defaults;

import java.util.List;
import me.indian.bds.command.Command;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ServerUtil;

public class AlertCommand extends Command {

    public AlertCommand() {
        super("alert", "Ważne informacje na czat");
        this.addOption("<message>", "Wiadomość");
        this.addAlliases(List.of("b"));
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
         if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            this.deniedSound();
            return true;
        }

        if (args.length > 0) {
            for (final String newArg : MessageUtil.buildMessageFromArgs(args).split("-")) {
                ServerUtil.tellrawToAll(newArg);
            }

            return true;
        }
        return false;
    }
}
