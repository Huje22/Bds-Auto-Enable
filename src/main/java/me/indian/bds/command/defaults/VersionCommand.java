package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.config.AppConfigManager;

public class VersionCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final AppConfigManager appConfigManager;

    public VersionCommand(final BDSAutoEnable bdsAutoEnable) {
        super("version", "Pokazuje załadowaną wersje minecraft + wersje oprogramowania");
        this.bdsAutoEnable = bdsAutoEnable;
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        final int protocol = this.bdsAutoEnable.getVersionManager().getLastKnownProtocol();
        this.sendMessage("&aWersja minecraft:&b " + this.appConfigManager.getVersionManagerConfig().getVersion() + "&r (&a"+protocol+"&r)");
        this.sendMessage("&aWersja BDS-Auto-Enable:&b " + this.bdsAutoEnable.getProjectVersion());
        return true;
    }
}
