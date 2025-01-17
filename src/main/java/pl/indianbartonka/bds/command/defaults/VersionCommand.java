package pl.indianbartonka.bds.command.defaults;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.config.AppConfigManager;

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
        this.sendMessage(this.getServerVersion());
        this.sendMessage("&aWersja BDS-Auto-Enable:&b " + this.bdsAutoEnable.getProjectVersion());
        return true;
    }

    private String getServerVersion() {
        return "&aWersja minecraft:&b " + this.appConfigManager.getVersionManagerConfig().getVersion() +
                "&r (&d" + this.bdsAutoEnable.getVersionManager().getLastKnownProtocol() + "&r) ";
    }
}
