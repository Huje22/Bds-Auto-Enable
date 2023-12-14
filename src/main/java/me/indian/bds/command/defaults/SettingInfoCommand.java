package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;

public class SettingInfoCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public SettingInfoCommand(final BDSAutoEnable bdsAutoEnable) {
        super("setting", "info o aktualnych ustawieniach servera");
        this.bdsAutoEnable = bdsAutoEnable;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        //TODO: Zaimplenetuj wypisywanie najważniejszych informacji z server.properties
        return true;
    }
}
