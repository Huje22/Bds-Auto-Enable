package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.command.CommandSender;
import me.indian.bds.config.AppConfigManager;

public class VersionCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final AppConfigManager appConfigManager;

    public VersionCommand(final BDSAutoEnable bdsAutoEnable) {
        super("version", "Pokazuje załadowaną versie minecraft + versie oprogramowania");
        this.bdsAutoEnable = bdsAutoEnable;
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
    }

    @Override
    public boolean onExecute( final String[] args, final boolean isOp) {
        this.sendMessage("&aWersja minecraft:&b " + this.appConfigManager.getVersionManagerConfig().getVersion());
        this.sendMessage("&aWersja BDS-Auto-Enable:&b " + this.bdsAutoEnable.getProjectVersion());
        return false;
    }
}