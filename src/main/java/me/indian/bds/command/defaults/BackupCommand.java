package me.indian.bds.command.defaults;

import java.nio.file.Files;
import java.nio.file.Path;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.BackupModule;

public class BackupCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public BackupCommand(final BDSAutoEnable bdsAutoEnable) {
        super("backup", "Info o backup");
        this.bdsAutoEnable = bdsAutoEnable;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        final BackupModule backupModule = this.bdsAutoEnable.getWatchDog().getBackupModule();
        if (backupModule == null) {
            this.sendMessage("&cNie udało się uzyskać&b Modułu Backupów");
            return true;
        }

        if (!backupModule.isEnabled()) {
            this.sendMessage("&aBackupy są wyłączone");
            return true;
        }

        if (args.length == 0) {
            if (backupModule.getBackups().size() == 0) {
                this.sendMessage("&aBrak backupów");
                this.sendMessage("&aNastępny backup za:&b " + DateUtil.formatTimeDynamic(backupModule.calculateMillisUntilNextBackup()));
                return true;
            }

            this.sendMessage("&aNastępny backup za:&b " + DateUtil.formatTimeDynamic(backupModule.calculateMillisUntilNextBackup()));
            for (final Path path : backupModule.getBackups()) {
                if (!Files.exists(path)) continue;
                this.sendMessage("&a" + path.getFileName() + " Rozmiar: " + backupModule.getBackupSize(path.toFile(), false));
            }
            return true;
        }

        if (args[0].equals("do")) {
            if (!isOp) {
                this.sendMessage("&cPotrzebujesz wyższych uprawnień");
                return true;
            }
            backupModule.backup();
            ThreadUtil.sleep((int) MathUtil.getCorrectNumber(5, (backupModule.getLastBackupTime() / 5), 60) + 1);
            this.sendMessage("&aStatus backupa:&r " + backupModule.getStatus());
        }

        return true;
    }
}