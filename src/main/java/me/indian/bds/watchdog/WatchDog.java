package me.indian.bds.watchdog;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.basic.Defaults;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
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

    private final Logger logger;
    private final ExecutorService service;
    private final Config config;
    private final Timer timer;
    private String worldPath;
    private File backupFile;
    private File worldFile;
    private String worldName;
    private String date;
    private TimerTask hourlyTask;


    public WatchDog(final Config config) {
        this.logger = BDSAutoEnable.getLogger();
        this.service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Watchdog"));
        this.config = config;
        this.timer = new Timer();
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
                this.timer.schedule(this.hourlyTask, 0, this.minutesToMilliseconds(30));
            }
        });
    }

    public void forceBackup() {
        if (!this.config.isBackup()) return;
        if (!this.worldFile.exists()) return;
        this.upDateDate();
        try {
            ZipUtil.zipFolder(this.worldPath, this.backupFile.getAbsolutePath() + File.separator + this.worldName + this.date + ".zip");
            this.logger.info("Utworzono kopię zapasową");
        } catch (final Exception exception) {
            this.logger.critical("Nie można utworzyć kopii zapasowej");
            this.logger.critical(exception);
            exception.printStackTrace();
        }
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
