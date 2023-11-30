package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.watchdog.module.BackupModule;

import java.nio.file.Path;

public class BackupCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProcess serverProcess;

    public BackupCommand(final BDSAutoEnable bdsAutoEnable) {
        super("backup", "info o backup");
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
    }

    @Override
    public boolean onExecute(final String player, final String[] args, final boolean isOp) {
        final BackupModule backupModule = this.bdsAutoEnable.getWatchDog().getBackupModule();
        if (backupModule == null) {
            this.serverProcess.tellrawToPlayer(player, "&cNie udało się uzyskać&b Modułu Backupów");
            return true;
        }

        if (!backupModule.isEnabled()) {
            this.serverProcess.tellrawToPlayer(player, "&aBackupy są wyłączone");
            return true;
        }

        if (args.length == 0) {
            if (backupModule.getBackups().size() == 0) {
                this.serverProcess.tellrawToPlayer(player, "&aBrak backupów");
                return true;
            }

            for (final Path path : backupModule.getBackups()) {
                this.serverProcess.tellrawToPlayer(player, "&a" + path.getFileName() + " Rozmiar: ` " + backupModule.getBackupSize(path.toFile(), false) + "`");
            }
        } else if (args[0].equals("do")) {
            this.serverProcess.tellrawToPlayer(player, "&aCoś kiedyś tu będzies");
        }

        return true;
    }
}