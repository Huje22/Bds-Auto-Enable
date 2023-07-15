package me.indian.bds.watchdog;

import me.indian.bds.Main;
import me.indian.bds.config.Config;
import me.indian.bds.logger.impl.Logger;
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

    public WatchDog(final Config config) {
        this.logger = Main.getLogger();
        this.service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Watchdog"));
        this.config = config;
        this.timer = new Timer();
        if (this.config.isBackup()) logger.alert("Backupy są włączone");
        this.os = config.getSystemOs();
    }


    public void backup() {
        this.service.execute(() -> {
            if (config.isWatchdog() && config.isBackup()) {
                String folder;
                if (os == SystemOs.WINDOWS) {
                    folder = "\\worlds\\";
                } else if (os == SystemOs.LINUX) {
                    folder = "/worlds/";
                } else {
                    folder = "worlds";
                }

                final String path = config.getFilePath() + folder + config.getWorldName();
                logger.alert("Ścieżka świata backupów " + path);
                final File file = new File(path);

                if (!file.exists()) {
                    logger.critical("Folder świata " + config.getWorldName() + " nie istnieje");
                    logger.alert("Ścieżka " + path);
                    Thread.currentThread().interrupt();
                    timer.cancel();
                    hourlyTask.cancel();
                }

                this.hourlyTask = new TimerTask() {
                    @Override
                    public void run() {
                        upDateDate();
                        try {
                            ZipUtil.zipFolder(path, path + " " + date + ".zip");
                            logger.info("Utworzono kopię zapasową ");

                        } catch (final Exception exception) {
                            logger.critical("Nie można utworzyć kopij zapasowej");
                            logger.critical(exception);
                        }
                    }
                };
                timer.schedule(hourlyTask, 0, this.minutesToMilliseconds(30));
            }
        });
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
