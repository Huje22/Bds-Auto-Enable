package me.indian.bds.watchdog;

import me.indian.bds.Main;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.SystemOs;
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
    private final SystemOs os;
    private String date;
    private TimerTask hourlyTask;
    private String worldPath;
    private String backupPath;
    private File backupFile;

    public WatchDog(final Config config) {
        this.logger = Main.getLogger();
        this.service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Watchdog"));
        this.config = config;
        this.timer = new Timer();
        if (this.config.isBackup()) logger.alert("Backupy są włączone");
        this.os = this.config.getSystemOs();
    }

    public void backup() {
        this.service.execute(() -> {
            if (config.isWatchdog() && config.isBackup()) {
                String worldFolder;
                if (os == SystemOs.WINDOWS) {
                    worldFolder = "\\worlds\\";
                } else if (os == SystemOs.LINUX) {
                    worldFolder = "/worlds/";
                } else {
                    worldFolder = "worlds";
                }

                this.worldPath = config.getFilePath() + worldFolder + config.getWorldName();
                final File worldFile = new File(worldPath);
                this.backupFile = new File("BDS-Auto-Enable/backup");

                logger.alert("Ścieżka świata backupów " + backupFile.getAbsolutePath());

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
                    logger.critical("Folder świata " + config.getWorldName() + " nie istnieje");
                    logger.alert("Ścieżka " + worldPath);
                    Thread.currentThread().interrupt();
                    timer.cancel();
                    hourlyTask.cancel();
                }

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
        upDateDate();
        try {
            ZipUtil.zipFolder(worldPath, backupFile.getAbsolutePath() + File.separator + config.getWorldName() + date + ".zip");
            logger.info("Utworzono kopię zapasową");
        } catch (final Exception exception) {
            logger.critical("Nie można utworzyć kopii zapasowej");
            logger.critical(exception);
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
