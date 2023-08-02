package me.indian.bds.watchdog;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.ServerProcess;
import me.indian.bds.basic.Defaults;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MinecraftUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.ZipUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatchDog {

    private static boolean backuping = false;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ExecutorService service;
    private final Timer timer;
    private final Config config;
    private final String prefix;
    private final ServerProcess serverProcess;
    private String worldPath;
    private File backupFolder;
    private File worldFile;
    private String worldName;
    private String date;
    private TimerTask hourlyTask;


    public WatchDog(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Watchdog"));
        this.timer = new Timer();
        this.prefix = "&b[&3WatchDog&b]";
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        if (this.config.isBackup()) {
            logger.alert("Backupy są włączone");
            this.backupFolder = new File("BDS-Auto-Enable/backup");
            this.worldName = this.bdsAutoEnable.getServerProperties().getWorldName();
            this.worldPath = Defaults.getWorldsPath() + this.worldName;
            this.worldFile = new File(this.worldPath);
            if (!this.backupFolder.exists()) {
                this.logger.alert("Nie znaleziono foldera dla backupów");
                this.logger.info("Tworzenie folderu dla backupów");
                if (this.backupFolder.mkdir()) {
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
            if (this.config.isBackup()) {
                this.logger.debug("Ścieżka świata backupów " + Defaults.getWorldsPath() + this.worldName);
                this.hourlyTask = new TimerTask() {
                    @Override
                    public void run() {
                        forceBackup();
                    }
                };
                this.timer.schedule(this.hourlyTask, 0, MathUtil.minutesToMilliseconds(60));
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
            final File backup = new File(this.backupFolder.getAbsolutePath() + File.separator + this.worldName + " " + this.date + ".zip");
            try {
                backuping = true;
                this.saveWorld();
                this.serverProcess.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.prefix + " &6Tworzenie kopij zapasowej"));
                ZipUtil.zipFolder(this.worldPath, backup.getPath());
                this.logger.info("Utworzono kopię zapasową");
                this.serverProcess.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.prefix + " &aUtworzono kopię zapasową"));
                this.saveResume();
                backuping = false;
            } catch (final Exception exception) {
                backuping = false;
                this.bdsAutoEnable.getServerProcess().sendToConsole(MinecraftUtil.tellrawToAllMessage(this.prefix + " &4Nie można utworzyć kopii zapasowej"));
                this.logger.critical("Nie można utworzyć kopii zapasowej");
                this.logger.critical(exception);
                exception.printStackTrace();
                if (backup.delete()) {
                    this.serverProcess.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.prefix + " &aUsunięto błędny backup"));
                } else {
                    this.serverProcess.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.prefix + " &4Nie można usunać błędnego backupa"));
                }
                this.saveResume();
            }
        });
    }


    public void saveWorld() {
        this.serverProcess.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.prefix + " &aZapisywanie servera...."));
        this.serverProcess.sendToConsole("save hold");
        ThreadUtil.sleep(10);
    }

    public void saveResume() {
        this.serverProcess.sendToConsole("save resume");
    }


    private void upDateDate() {
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.date = (now.format(formatter) + " ").replace(":", "-");
    }
}
