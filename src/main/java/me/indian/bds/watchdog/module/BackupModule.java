package me.indian.bds.watchdog.module;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.Defaults;
import me.indian.bds.config.Config;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MinecraftUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.util.ZipUtil;
import me.indian.bds.watchdog.WatchDog;

public class BackupModule {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ExecutorService service;
    private final Timer timer;
    private final Config config;
    private WatchDog watchDog;
    private ServerProcess serverProcess;
    private String prefix;
    private String worldPath;
    private File backupFolder;
    private File worldFile;
    private String worldName;
    private boolean backuping;
    private String status;
    private long lastBackupMillis;

    public BackupModule(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Watchdog-BackupModule"));
        this.timer = new Timer();
        if (this.config.isBackup()) {
            final String version = this.config.getVersion();
            this.logger.alert("Backupy są włączone");
            this.backupFolder = new File("BDS-Auto-Enable/backup/" + version);
            this.worldName = this.bdsAutoEnable.getServerProperties().getWorldName();
            this.worldPath = Defaults.getWorldsPath() + this.worldName;
            this.worldFile = new File(this.worldPath);
            if (!this.backupFolder.exists()) {
                this.logger.alert("Nie znaleziono foldera backupów dla versij " + version);
                this.logger.info("Tworzenie folderu backupów dla versij " + version);
                if (this.backupFolder.mkdirs()) {
                    this.logger.info("Utworzono folder backupów dla versij " + version);
                } else {
                    this.logger.error("Nie można utworzyć folderu backupów dla versij " + version);
                }
            }
            if (!this.worldFile.exists()) {
                this.logger.critical("Folder świata \"" + this.worldName + "\" nie istnieje");
                this.logger.alert("Ścieżka " + this.worldPath);
                Thread.currentThread().interrupt();
                this.timer.cancel();
                this.backuping = true;
                return;
            }
        }
        this.backuping = false;
        this.status = "Brak";
        this.lastBackupMillis = 0;
    }

    public void initBackupModule(final WatchDog watchDog, final ServerProcess serverProcess) {
        this.watchDog = watchDog;
        this.prefix = this.watchDog.getWatchDogPrefix();
        this.serverProcess = serverProcess;
    }

    public void backup() {
        this.service.execute(() -> {
            if (this.config.isBackup()) {
                this.logger.debug("Ścieżka świata backupów " + Defaults.getWorldsPath() + this.worldName);
                final TimerTask backupTask = new TimerTask() {
                    @Override
                    public void run() {
                        BackupModule.this.forceBackup();
                        BackupModule.this.lastBackupMillis = System.currentTimeMillis();
                    }
                };
                this.timer.scheduleAtFixedRate(backupTask, 0, MathUtil.minutesToMilliseconds(this.config.getBackupFrequency()));
            }
        });
    }

    public void forceBackup() {
        if (!this.config.isBackup()) return;
        if (!this.worldFile.exists()) return;
        if (this.backuping) {
            this.logger.error("Nie można zrobić kopi podczas robienia już jednej");
            return;
        }
        if (this.serverProcess.getProcess() == null || !this.serverProcess.getProcess().isAlive()) return;
        final long startTime = System.currentTimeMillis();
        this.service.execute(() -> {
            final File backup = new File(this.backupFolder.getAbsolutePath() + File.separator + this.worldName + " " + DateUtil.getFixedDate() + ".zip");
            try {
                this.backuping = true;
                this.watchDog.saveWorld();
                final double lastBackUpTime = this.config.getLastBackupTime();
                MinecraftUtil.tellrawToAllAndLogger(this.prefix, "&aTworzenie kopij zapasowej ostatnio trwało to&b " + lastBackUpTime + "&a sekund", LogState.INFO);
                this.status = "Tworzenie backupa...";
                ZipUtil.zipFolder(this.worldPath, backup.getPath());
                final double backUpTime = ((System.currentTimeMillis() - startTime) / 1000.0);
                this.config.setLastBackupTime(backUpTime);
                MinecraftUtil.tellrawToAllAndLogger(this.prefix, "&aUtworzono kopię zapasową w&b " + backUpTime + "&a sekund", LogState.INFO);
                this.status = "Utworzono backup";
            } catch (final Exception exception) {
                MinecraftUtil.tellrawToAllAndLogger(this.prefix, "&4Nie można utworzyć kopii zapasowej", LogState.CRITICAL);
                this.status = "Nie udało sie utworzyć kopij zapasowej";
                this.logger.critical(exception);
                exception.printStackTrace();
                if (backup.delete()) {
                    MinecraftUtil.tellrawToAllAndLogger(this.prefix, "&aUsunięto błędny backup", LogState.INFO);
                } else {
                    MinecraftUtil.tellrawToAllAndLogger(this.prefix, "&4Nie można usunać błędnego backupa", LogState.INFO);
                }
            }
            this.backuping = false;
            this.watchDog.saveResume();
            this.config.save();
        });
    }

    public long calculateMillisUntilNextBackup() {
        return Math.max(0, MathUtil.minutesToMilliseconds(this.config.getBackupFrequency()) - (System.currentTimeMillis() - this.lastBackupMillis));
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isBackuping() {
        return this.backuping;
    }
}