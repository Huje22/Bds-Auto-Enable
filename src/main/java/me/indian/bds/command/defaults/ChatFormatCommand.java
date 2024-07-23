package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.server.ServerProcess;

public class ChatFormatCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProcess serverProcess;

    public ChatFormatCommand(final BDSAutoEnable bdsAutoEnable) {
        super("format", "Czy aplikacja ma handlować wiadomości graczy");
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProcess = this.bdsAutoEnable.getServerProcess();

        this.addOption("<true/false>");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            this.deniedSound();
            return true;
        }
        if (args.length == 0) return false;

        if (args[0].equals("true")) {
            this.bdsAutoEnable.getWatchDog().getPackModule().setAppHandledMessages(true);
            this.sendMessage("&aTeraz wiadomości są handlowane po stronie aplikacji");
        } else if (args[0].equals("false")) {
            this.sendMessage("&aTeraz wiadomości są handlowane po stronie servera (vanillowo)");
            this.bdsAutoEnable.getWatchDog().getPackModule().setAppHandledMessages(false);
        } else {
            return false;
        }

        this.serverProcess.sendToConsole("reload");
        return true;
    }
}