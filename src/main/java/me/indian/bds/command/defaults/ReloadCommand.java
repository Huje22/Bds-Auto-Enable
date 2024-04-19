package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.logger.Logger;

public class ReloadCommand extends Command {

    private final Logger logger;
    private final AppConfigManager appConfigManager;

    public ReloadCommand(final BDSAutoEnable bdsAutoEnable) {
        super("reload", "Przeładowuje pliki konfiguracyjne");
        this.logger = bdsAutoEnable.getLogger();
        this.appConfigManager = bdsAutoEnable.getAppConfigManager();
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
         if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            this.deniedSound();
            return true;
        }

        try {
            this.appConfigManager.load();
            this.sendMessage("&aPrzeładowano pliki konfiguracyjne! ");
        } catch (final Exception exception) {
            this.sendMessage("&cNie udało się przeładować plików konfiguracyjnych zajrzyj do&e konsoli&c po więcej informacji");
            this.logger.logThrowable(exception);
        }
        return true;
    }
}
