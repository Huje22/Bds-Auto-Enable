package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.event.Position;
import me.indian.bds.util.DefaultsVariables;

public class VersionCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final AppConfigManager appConfigManager;

    public VersionCommand(final BDSAutoEnable bdsAutoEnable) {
        super("version", "Pokazuje załadowaną wersje minecraft + wersje oprogramowania");
        this.bdsAutoEnable = bdsAutoEnable;
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
    }

    @Override
    public boolean onExecute(final String[] args, Position position, final boolean isOp) {
        this.sendMessage(this.getServerVersion());
        this.sendMessage("&aWersja BDS-Auto-Enable:&b " + this.bdsAutoEnable.getProjectVersion());
        return true;
    }

    private String getServerVersion() {
        final int protocol = this.bdsAutoEnable.getVersionManager().getLastKnownProtocol();
        String serverVersion = "&aWersja minecraft:&b " + this.appConfigManager.getVersionManagerConfig().getVersion() + "&r (&d" + protocol + "&r) ";

        if (DefaultsVariables.isLeviLamina()) serverVersion += "&1LeviLamia";

        return serverVersion;
    }
}
