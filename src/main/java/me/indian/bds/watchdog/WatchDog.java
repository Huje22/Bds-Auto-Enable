package me.indian.bds.watchdog;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.basic.Defaults;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MinecraftColor;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.util.ZipUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatchDog {

    private static boolean backuping = false;
    private final Logger logger;
    private final BDSAutoEnable bdsAutoEnable;
    private final ExecutorService service;
    private final Timer timer;
    private String worldPath;
    private File backupFile;
    private File worldFile;
    private String worldName;
    private String date;
    private TimerTask hourlyTask;
    private final Config config;
    private final String prefix;


    public WatchDog(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Watchdog"));
        this.timer = new Timer();
        this.prefix = "&b[&3WatchDog&b]";
        if (this.config.isBackup()) {
            logger.alert("Backupy są włączone");
            this.backupFile = new File("BDS-Auto-Enable/backup");
            this.worldName = Defaults.getWorldName();
            this.worldPath = Defaults.getWorldsPath() + this.worldName;
            this.worldFile = new File(this.worldPath);
            if (!this.backupFile.exists()) {
                this.logger.alert("Nie znaleziono foldera dla backupów");
                this.logger.info("Tworzenie folderu dla backupów");
                if (this.backupFile.mkdir()) {
                    this.logger.info("Utworzono folder dla backupów");
                } else {
                    this.logger.error("Nie można utworzyć folderu dla backupów");
                }
            }

            if (!this.worldFile.exists()) {
                this.logger.critical("Folder świata \"" + this.worldName + "\" nie istnieje");
                this.logger.alert("Ścieżka " + this.worldPath);
                Thread.currentThread().interrupt();
                this.timer.cancel();
//            hourlyTask.cancel();
            }
        }
    }

    public void backup() {
        this.service.execute(() -> {
            if (this.config.isWatchdog() && this.config.isBackup()) {
                this.logger.debug("Ścieżka świata backupów " + Defaults.getWorldsPath() + this.worldName);
                this.hourlyTask = new TimerTask() {
                    @Override
                    public void run() {
                        forceBackup();
                    }
                };
                this.timer.schedule(this.hourlyTask, 0, this.minutesToMilliseconds(60));
            }
        });
    }

    public void forceBackup() {
        if (!this.config.isBackup()) return;
        if (!this.worldFile.exists()) return;
        if (backuping) {
            this.logger.error("Nie można zrobić kopi podczas robienia już jednej");
            return;
        }
        this.service.execute(() -> {
            this.upDateDate();
            try {
                backuping = true;
                this.bdsAutoEnable.sendCommandToConsole(MinecraftColor.colorize("tellraw @a {\"rawtext\":[{\"text\":\"" + this.prefix + " &6Tworzenie kopij zapasowej\"}]}"));
                ZipUtil.zipFolder(this.worldPath, this.backupFile.getAbsolutePath() + File.separator + this.worldName + this.date + ".zip");
                this.logger.info("Utworzono kopię zapasową");
                this.bdsAutoEnable.sendCommandToConsole(MinecraftColor.colorize("tellraw @a {\"rawtext\":[{\"text\":\"" + this.prefix + " &aUtworzono kopię zapasową\"}]}"));
                backuping = false;
            } catch (final Exception exception) {
                backuping = false;
                this.bdsAutoEnable.sendCommandToConsole(MinecraftColor.colorize("tellraw @a {\"rawtext\":[{\"text\":\"" + this.prefix + " &4Nie można utworzyć kopii zapasowej\"}]}"));
                this.logger.critical("Nie można utworzyć kopii zapasowej");
                this.logger.critical(exception);
                exception.printStackTrace();
            }
        });
    }

    private void upDateDate() {
        final LocalDateTime now = LocalDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.date = (now.format(formatter) + " ").replace(":", "-");
    }

    public int minutesToMilliseconds(int minutes) {
        return minutes * 60000;
    }
}
