package pl.indianbartonka.bds.command.defaults;

import java.util.List;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.util.ServerUtil;
import pl.indianbartonka.util.MessageUtil;

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
