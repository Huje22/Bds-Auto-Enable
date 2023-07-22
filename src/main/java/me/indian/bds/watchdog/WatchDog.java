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
    private final String worldPath;
    private final File backupFile;
    private final File worldFile;
    private final String worldName;
    private String date;
    private TimerTask hourlyTask;


    public WatchDog(final Config config) {
        this.logger = BDSAutoEnable.getLogger();
        this.service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Watchdog"));
        this.config = config;
        this.timer = new Timer();
        if (this.config.isBackup()) logger.alert("Backupy są włączone");
        this.backupFile = new File("BDS-Auto-Enable/backup");
        this.worldName = Defaults.getWorldName();
        this.worldPath = Defaults.getWorldsPath() + this.worldName;
        this.worldFile = new File(worldPath);
        if (!backupFile.exists()) {
            logger.alert("Nie znaleziono foldera dla backupów");
            logger.info("Tworzenie folderu dla backupów");
            if (backupFile.mkdir()) {
                logger.info("Utworzono folder dla backupów");
            } else {
                logger.error("Nie można utworzyć folderu dla backupów");
            }
        }

        if (!worldFile.exists()) {
            logger.critical("Folder świata \"" + this.worldName + "\" nie istnieje");
            logger.alert("Ścieżka " + this.worldPath);
            Thread.currentThread().interrupt();
            timer.cancel();
//            hourlyTask.cancel();
        }
    }

    public void backup() {
        this.service.execute(() -> {
            if (config.isWatchdog() && config.isBackup()) {
                logger.debug("Ścieżka świata backupów " + Defaults.getWorldsPath() + this.worldName);
                this.hourlyTask = new TimerTask() {
                    @Override
                    public void run() {
                        forceBackup();
                    }
                };
                timer.schedule(hourlyTask, 0, this.minutesToMilliseconds(30));
            }
        });
    }

    public void forceBackup() {
        if (!worldFile.exists()) return;
        upDateDate();
        try {
            ZipUtil.zipFolder(this.worldPath, this.backupFile.getAbsolutePath() + File.separator + this.worldName + this.date + ".zip");
            logger.info("Utworzono kopię zapasową");
        } catch (final Exception exception) {
            logger.critical("Nie można utworzyć kopii zapasowej");
            logger.critical(exception);
            exception.printStackTrace();
        }
    }

    private void upDateDate() {
        final LocalDateTime now = LocalDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        date = (now.format(formatter) + " ").replace(":", "-");
    }

    public int minutesToMilliseconds(int minutes) {
        int milliseconds = minutes * 60000;
        return milliseconds;
    }
}
