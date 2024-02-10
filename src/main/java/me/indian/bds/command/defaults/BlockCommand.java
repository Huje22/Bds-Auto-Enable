package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.watchdog.module.PackModule;

public class BlockCommand extends Command {

    private final PackModule packModule;

    public BlockCommand(final BDSAutoEnable bdsAutoEnable) {
        super("block", "Top 10 graczy z największym wykopaniem i postawieniem bloków");
        this.packModule = bdsAutoEnable.getWatchDog().getPackModule();
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.packModule.isLoaded()) {
            this.sendMessage("&cPaczka &b" + this.packModule.getPackName() + "&c nie jest załadowana!");
            return true;
        }

        this.sendMessage("&a---------------------");
        StatusUtil.getTopBlock(false, 10).forEach(this::sendMessage);
        this.sendMessage("&a---------------------");
        return true;
    }
}