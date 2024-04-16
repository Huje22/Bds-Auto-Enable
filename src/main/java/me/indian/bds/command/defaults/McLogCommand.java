package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.McLog;
import me.indian.bds.command.Command;

public class McLogCommand extends Command {

    private final McLog mcLog;

    public McLogCommand(final BDSAutoEnable bdsAutoEnable) {
        super("log", "Wysyła aktualny plik logów do mclo.gs");
        this.mcLog = bdsAutoEnable.getMcLog();
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (this.player != null) {
            this.sendMessage("&cTa komenda dostępna jest tylko w&3 konsoli");
            return true;
        }

        this.mcLog.sendCurrentLog();
        return false;
    }
}
