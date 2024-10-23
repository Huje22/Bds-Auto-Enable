package pl.indianbartonka.bds.watchdog.module;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.config.AppConfigManager;
import pl.indianbartonka.bds.config.sub.watchdog.BackupConfig;
import pl.indianbartonka.bds.config.sub.watchdog.WatchDogConfig;
import pl.indianbartonka.bds.event.watchdog.BackupDoneEvent;
import pl.indianbartonka.bds.event.watchdog.BackupFailEvent;
import pl.indianbartonka.bds.server.ServerManager;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.bds.util.DefaultsVariables;
import pl.indianbartonka.bds.util.ServerUtil;
import pl.indianbartonka.bds.util.StatusUtil;
import pl.indianbartonka.bds.watchdog.WatchDog;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.util.MemoryUnit;
import pl.indianbartonka.util.file.FileUtil;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.ZipUtil;
import pl.indianbartonka.util.logger.LogState;
import pl.indianbartonka.util.logger.Logger;

public class BackupModule {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ExecutorService service;
    private final Timer timer;
    private final AppConfigManager appConfigManager;
    private final WatchDogConfig watchDogConfig;
    private final List<Path> backups;
    private final String worldName, worldPath;
    private final File worldFile;
    private final WatchDog watchDog;
    private final String prefix;
    private final boolean enabled;
    private final ServerManager serverManager;
    private final BackupConfig backupConfig;
    private ServerProcess serverProcess;
    private File backupFolder;
    private String status;
    private long lastPlanedBackupMillis;
    private boolean backuping, canDoBackupOnPlayerJoin;

    public BackupModule(final BDSAutoEnable bdsAutoEnable, final WatchDog watchDog) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
        this.watchDogConfig = this.bdsAutoEnable.getAppConfigManager().getWatchDogConfig();
        this.watchDog = watchDog;
        this.backups = new ArrayList<>();
        this.service = Executors.newFixedThreadPool(2, new ThreadUtil("Watchdog-BackupModule"));
        this.timer = new Timer("Backup-Timer", true);
        this.worldName = this.bdsAutoEnable.getServerProperties().getWorldName();
        this.worldPath = DefaultsVariables.getWorldsPath() + this.worldName;
        this.worldFile = new File(this.worldPath);
        this.createWorldFile();
        this.prefix = this.watchDog.getWatchDogPrefix();
        this.enabled = this.watchDogConfig.getBackupConfig().isEnabled();
        this.serverManager = this.bdsAutoEnable.getServerManager();
        this.backupConfig = this.watchDogConfig.getBackupConfig();

        if (this.watchDogConfig.getBackupConfig().isEnabled()) {
            this.backupFolder = new File(DefaultsVariables.getAppDir() + "backup");
            if (!this.backupFolder.exists()) {
                if (!this.backupFolder.mkdirs()) {
                    this.logger.error("Nie można utworzyć folderu backupów");
                }
            }
        }

        this.status = "Brak";
        this.lastPlanedBackupMillis = System.currentTimeMillis();
        this.backuping = false;
        this.canDoBackupOnPlayerJoin = false;
        this.run();
    }

    public void init() {
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.loadAvailableBackups();
    }

    private void run() {
        if (this.watchDogConfig.getBackupConfig().isEnabled()) {
            final long time = DateUtil.minutesTo(this.backupConfig.getBackupFrequency(), TimeUnit.MILLISECONDS);
            final TimerTask backupTask = new TimerTask() {

                boolean cachedNonPlayers = false;

                @Override
                public void run() {
                    final boolean nonPlayers = BackupModule.this.serverManager.getOnlinePlayers().isEmpty();
                    if (this.cachedNonPlayers && nonPlayers) {
                        BackupModule.this.lastPlanedBackupMillis = System.currentTimeMillis();
                        BackupModule.this.canDoBackupOnPlayerJoin = true;
                        return;
                    }

                    this.cachedNonPlayers = nonPlayers;

                    BackupModule.this.backup();
                    BackupModule.this.lastPlanedBackupMillis = System.currentTimeMillis();
                }
            };
            this.timer.scheduleAtFixedRate(backupTask, time, time);
        }
    }

    public void backupOnPlayerJoin() {
        if (this.canDoBackupOnPlayerJoin) {
            this.backup();
            this.canDoBackupOnPlayerJoin = false;
        }
    }

    public void backup() {
        if (!this.backupConfig.isEnabled()) {
            this.logger.info("Backupy są&4 wyłączone");
            return;
        }

        if (!this.worldFile.exists() || !this.canDoBackup()) return;

        if (this.backuping) {
            this.logger.error("&cNie można wykonać backup gdy jeden jest już wykonywany");
            return;
        }

        if (!this.serverProcess.isEnabled()) return;
        this.backuping = true;
        final long startTime = System.currentTimeMillis();
        this.service.execute(() -> {
            final File backup = new File(this.backupFolder.getAbsolutePath() + File.separator + this.worldName + " " + DateUtil.getFixedDate() + ".zip");
            try {
                this.watchDog.saveWorld();
                final double lastBackUpTime = this.backupConfig.getLastBackupTime();
                ServerUtil.tellrawToAllAndLogger(this.prefix, "&aTworzenie kopij zapasowej ostatnio trwało to&b " + lastBackUpTime + "&a sekund", LogState.INFO);
                this.status = "Tworzenie backupa...";
                ZipUtil.zipFolder(this.worldPath, backup.getPath());
                final double backUpTime = ((System.currentTimeMillis() - startTime) / 1000.0);
                this.backupConfig.setLastBackupTime(backUpTime);
                this.loadAvailableBackups();
                ServerUtil.tellrawToAllAndLogger(this.prefix,
                        "&aUtworzono kopię zapasową w&b " + backUpTime + "&a sekund, waży ona " + this.getBackupSize(backup),
                        LogState.INFO);
                ServerUtil.tellrawToAllAndLogger(this.prefix,
                        "&aDostępne jest&d " + this.backups.size() + "&a kopi zapasowych",
                        LogState.INFO);

                this.status = "Utworzono backup";
                this.bdsAutoEnable.getEventManager().callEvent(new BackupDoneEvent());
            } catch (final Exception exception) {
                this.status = "Nie udało sie utworzyć kopij zapasowej";
                ServerUtil.tellrawToAllAndLogger(this.prefix, "&4" + this.status, exception, LogState.CRITICAL);
                if (backup.delete()) {
                    ServerUtil.tellrawToAllAndLogger(this.prefix, "&aUsunięto błędny backup", LogState.INFO);
                } else {
                    ServerUtil.tellrawToAllAndLogger(this.prefix, "&4Nie można usunąć błędnego backupa", LogState.ERROR);
                }
                this.bdsAutoEnable.getEventManager().callEvent(new BackupFailEvent(exception));
            } finally {
                this.backuping = false;
                this.watchDog.saveResume();
                this.appConfigManager.getWatchDogConfig().save();
            }
        });
    }

    private boolean canDoBackup() {
        final int maxBackups = this.backupConfig.getMaxBackups();
        if (maxBackups == -1 || maxBackups == 0) {

            //TODO: Usunac te casty do longu jak wyjdzie nowe IndianUtils
            final long gb = (long) MemoryUnit.BYTES.to(StatusUtil.availableDiskSpace(), MemoryUnit.GIGABYTES);
            if (gb < MemoryUnit.BYTES.to(FileUtil.getFileSize(this.worldFile), MemoryUnit.GIGABYTES) + 1) {
                ServerUtil.tellrawToAllAndLogger(this.prefix,
                        "&aWykryto zbyt małą ilość pamięci &d(&b" + gb + "&e GB&d)&a aby wykonać&b backup&c!",
                        LogState.WARNING);
                return false;
            }

        } else {
            if (this.backups.size() >= maxBackups) {
                ServerUtil.tellrawToAllAndLogger(this.prefix,
                        "&cOsiągnięto maksymalną liczbę backup!&d (&3" + maxBackups + "&d)",
                        LogState.WARNING);
                return false;
            }
        }

        return true;
    }

    private void loadAvailableBackups() {
        if (!this.backupConfig.isEnabled()) return;
        this.backups.clear();
        try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(this.backupFolder.getPath()))) {
            for (final Path path : directoryStream) {
                if (Files.isRegularFile(path) && path.toString().endsWith(".zip") && Files.exists(path)) {
                    this.backups.add(path);
                }
            }
            this.backups.sort(Collections.reverseOrder());
        } catch (final Exception exception) {
            this.logger.error("Nie można załadować dostępnych backup", exception);
        }
    }

    public String getBackupSize(final File backup) {
        long fileSizeBytes;
        try {
            fileSizeBytes = Files.size(backup.toPath());
        } catch (final IOException exception) {
            fileSizeBytes = -1;
        }

        return MathUtil.formatBytesDynamic(fileSizeBytes, true);
    }

    private void createWorldFile() {
        if (!this.worldFile.exists()) {
            this.logger.critical("Folder świata \"" + this.worldName + "\" nie istnieje");
            this.logger.alert("Ścieżka " + this.worldPath);

            if (!this.worldFile.mkdirs()) {
                if (!this.worldFile.mkdir()) {
                    throw new RuntimeException("Nie można utworzyć folderu świata!");
                }
            }
            this.logger.info("Utworzono brakujący folder świata");
        }
    }

    public long calculateMillisUntilNextBackup() {
        return Math.max(0, DateUtil.minutesTo(this.backupConfig.getBackupFrequency(), TimeUnit.MILLISECONDS) - (System.currentTimeMillis() - this.lastPlanedBackupMillis));
    }

    public double getLastBackupTime() {
        return this.backupConfig.getLastBackupTime();
    }

    public String getStatus() {
        return this.status;
    }

    public File getWorldFile() {
        return this.worldFile;
    }

    public List<String> getBackupsNames() {
        return this.backups.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();
    }

    public List<Path> getBackups() {
        return this.backups;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public long getLastPlanedBackupMillis() {
        return this.lastPlanedBackupMillis;
    }

    public boolean isBackuping() {
        return this.backuping;
    }

    public boolean isCanDoBackupOnPlayerJoin() {
        return this.canDoBackupOnPlayerJoin;
    }
}
