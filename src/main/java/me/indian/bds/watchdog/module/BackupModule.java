package me.indian.bds.watchdog.module;

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
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.config.sub.watchdog.WatchDogConfig;
import me.indian.bds.event.watchdog.BackupDoneEvent;
import me.indian.bds.event.watchdog.BackupFailEvent;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerManager;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.properties.ServerProperties;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.FileUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.ServerUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.util.ZipUtil;
import me.indian.bds.watchdog.WatchDog;

public class BackupModule {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ExecutorService service;
    private final Timer timer;
    private final AppConfigManager appConfigManager;
    private final WatchDogConfig watchDogConfig;
    private final List<Path> backups;
    private final ServerProperties serverProperties;
    private final File worldFile;
    private final WatchDog watchDog;
    private final String prefix;
    private final boolean enabled;
    private final ServerManager serverManager;
    private ServerProcess serverProcess;
    private File backupFolder;
    private String status, worldName, worldPath;
    private long lastBackupMillis;
    private boolean backuping;

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
        this.serverProperties = this.bdsAutoEnable.getServerProperties();
        this.fixWorldName();
        this.worldFile = new File(this.worldPath);
        this.createWorldFile();
        this.prefix = this.watchDog.getWatchDogPrefix();
        this.enabled = this.watchDogConfig.getBackupConfig().isEnabled();
        this.serverManager = this.bdsAutoEnable.getServerManager();

        if (this.watchDogConfig.getBackupConfig().isEnabled()) {
            this.backupFolder = new File(DefaultsVariables.getAppDir() + "backup");
            if (!this.backupFolder.exists()) {
                if (!this.backupFolder.mkdirs()) {
                    this.logger.error("Nie można utworzyć folderu backupów");
                }
            }
        }

        this.status = "Brak";
        this.lastBackupMillis = System.currentTimeMillis();
        this.backuping = false;
        this.run();
    }

    public void init() {
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.loadAvailableBackups();
    }

    private void run() {
        if (this.watchDogConfig.getBackupConfig().isEnabled()) {
            final long time = MathUtil.minutesTo(this.watchDogConfig.getBackupConfig().getBackupFrequency(), TimeUnit.MILLISECONDS);

            final TimerTask backupTask = new TimerTask() {

                boolean cachedNonPlayers = false;

                @Override
                public void run() {
                    final boolean nonPlayers = BackupModule.this.serverManager.getOnlinePlayers().isEmpty();
                    if (this.cachedNonPlayers && nonPlayers) return;
                    this.cachedNonPlayers = nonPlayers;

                    BackupModule.this.backup();
                    BackupModule.this.lastBackupMillis = System.currentTimeMillis();
                }
            };
            this.timer.scheduleAtFixedRate(backupTask, time, time);
        }
    }

    public void backup() {
        if (!this.watchDogConfig.getBackupConfig().isEnabled()) {
            this.logger.info("Backupy są&4 wyłączone");
            return;
        }

        if (!this.worldFile.exists()) return;
        final long gb = MathUtil.bytesToGB(StatusUtil.availableDiskSpace());
        if (gb < MathUtil.bytesToGB(FileUtil.getFolderSize(this.worldFile)) + 1) {
            ServerUtil.tellrawToAllAndLogger(this.prefix,
                    "&aWykryto zbyt małą ilość pamięci &d(&b" + gb + "&e GB&d)&a aby wykonać&b backup&c!",
                    LogState.WARNING);
            return;
        }
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
                final double lastBackUpTime = this.watchDogConfig.getBackupConfig().getLastBackupTime();
                ServerUtil.tellrawToAllAndLogger(this.prefix, "&aTworzenie kopij zapasowej ostatnio trwało to&b " + lastBackUpTime + "&a sekund", LogState.INFO);
                this.status = "Tworzenie backupa...";
                ZipUtil.zipFolder(this.worldPath, backup.getPath());
                final double backUpTime = ((System.currentTimeMillis() - startTime) / 1000.0);
                this.watchDogConfig.getBackupConfig().setLastBackupTime(backUpTime);
                this.loadAvailableBackups();
                ServerUtil.tellrawToAllAndLogger(this.prefix,
                        "&aUtworzono kopię zapasową w&b " + backUpTime + "&a sekund, waży ona " + this.getBackupSize(backup, false),
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

    private void loadAvailableBackups() {
        if (!this.watchDogConfig.getBackupConfig().isEnabled()) return;
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

    public String getBackupSize(final File backup, final boolean markdown) {
        long fileSizeBytes;
        try {
            fileSizeBytes = Files.size(backup.toPath());
        } catch (final IOException exception) {
            fileSizeBytes = -1;
        }
        final long gb = MathUtil.bytesToGB(fileSizeBytes);
        final long mb = MathUtil.getMbFromBytesGb(fileSizeBytes);
        final long kb = MathUtil.getKbFromBytesGb(fileSizeBytes);

        if (markdown) {
            return gb + " GB " + mb + " MB " + kb + " KB";
        } else {
            return "&b" + gb + "&e GB &b" + mb + "&e MB &b" + kb + "&e KB";
        }
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

    public void fixWorldName() {
        String worldName = this.serverProperties.getWorldName();

        if (worldName == null || worldName.isEmpty() || worldName.equals("null")) {
            try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(DefaultsVariables.getWorldsPath()))) {
                for (final Path path : directoryStream) {
                    if (Files.isDirectory(path) && Files.exists(path)) {
                        worldName = path.toFile().getName();
                        this.worldPath = path.toString();
                        break;
                    }
                }
            } catch (final Exception exception) {
                worldName = "Bedrock level";
            }

            this.serverProperties.setWorldName(worldName);
            this.worldName = worldName;
            this.logger.debug("&aNaprawiono nazwe świata&c!");
        }
    }

    public long calculateMillisUntilNextBackup() {
        return Math.max(0, MathUtil.minutesTo(this.watchDogConfig.getBackupConfig().getBackupFrequency(), TimeUnit.MILLISECONDS) - (System.currentTimeMillis() - this.lastBackupMillis));
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

    public long getLastBackupMillis() {
        return this.lastBackupMillis;
    }

    public boolean isBackuping() {
        return this.backuping;
    }
}