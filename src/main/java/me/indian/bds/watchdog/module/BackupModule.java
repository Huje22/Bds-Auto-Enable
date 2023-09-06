package me.indian.bds.watchdog.module;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.Defaults;
import me.indian.bds.config.Config;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.util.ZipUtil;
import me.indian.bds.watchdog.WatchDog;

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

public class BackupModule {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ExecutorService service;
    private final Timer timer;
    private final Config config;
    private final List<Path> backups;
    private final String worldPath, worldName;
    private final File worldFile;
    private WatchDog watchDog;
    private ServerProcess serverProcess;
    private String prefix;
    private File backupFolder;
    private String status;
    private long lastBackupMillis;
    private boolean backuping, loading;

    public BackupModule(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.backups = new ArrayList<>();
        this.service = Executors.newScheduledThreadPool(5, new ThreadUtil("Watchdog-BackupModule"));
        this.timer = new Timer("Backup", true);
        this.worldName = this.bdsAutoEnable.getServerProperties().getWorldName();
        this.worldPath = Defaults.getWorldsPath() + this.worldName;
        this.worldFile = new File(this.worldPath);
        if (this.config.getWatchDogConfig().getBackup().isBackup()) {
            this.logger.alert("Backupy są włączone");
            this.backupFolder = new File(Defaults.getAppDir() + File.separator + "backup");
            if (!this.backupFolder.exists()) {
                if (!this.backupFolder.mkdirs()) {
                    this.logger.error("Nie można utworzyć folderu backupów");
                }
            }
            if (!this.worldFile.exists()) {
                this.logger.critical("Folder świata \"" + this.worldName + "\" nie istnieje");
                this.logger.alert("Ścieżka " + this.worldPath);
                Thread.currentThread().interrupt();
                this.timer.cancel();
                return;
            }
        }

        this.status = "Brak";
        this.lastBackupMillis = 0;
        this.backuping = false;
        this.loading = false;
    }

    public void initBackupModule(final WatchDog watchDog, final ServerProcess serverProcess) {
        this.watchDog = watchDog;
        this.prefix = this.watchDog.getWatchDogPrefix();
        this.serverProcess = serverProcess;
        this.loadAvailableBackups();
    }

    public void backup() {
        this.service.execute(() -> {
            final long time = MathUtil.minutesToMillis(this.config.getWatchDogConfig().getBackup().getBackupFrequency());
            if (this.config.getWatchDogConfig().getBackup().isBackup()) {
                this.logger.debug("Ścieżka świata backupów " + Defaults.getWorldsPath() + this.worldName);
                final TimerTask backupTask = new TimerTask() {
                    @Override
                    public void run() {
                        BackupModule.this.forceBackup();
                        BackupModule.this.lastBackupMillis = System.currentTimeMillis();
                    }
                };
                this.timer.scheduleAtFixedRate(backupTask, time, time);
            }
        });
    }

    public void forceBackup() {
        if (!this.config.getWatchDogConfig().getBackup().isBackup()) return;
        if (!this.worldFile.exists()) return;
        if (StatusUtil.availableDiskSpace() < 10) {
            this.serverProcess.tellrawToAllAndLogger(this.prefix, "Wykryto zbyt małą ilość pamięci aby wykonać&b backup&c!", LogState.ERROR);
            return;
        }
        if (this.backuping) {
            this.logger.error("&cNie można wykonać backup gdy jeden jest już wykonywany");
            return;
        }

        if (this.serverProcess.isEnabled()) return;
        this.backuping = true;
        final long startTime = System.currentTimeMillis();
        this.service.execute(() -> {
            final File backup = new File(this.backupFolder.getAbsolutePath() + File.separator + this.worldName + " " + DateUtil.getFixedDate() + ".zip");
            try {
                this.watchDog.saveWorld();
                final double lastBackUpTime = this.config.getWatchDogConfig().getBackup().getLastBackupTime();
                this.serverProcess.tellrawToAllAndLogger(this.prefix, "&aTworzenie kopij zapasowej ostatnio trwało to&b " + lastBackUpTime + "&a sekund", LogState.INFO);
                this.status = "Tworzenie backupa...";
                ZipUtil.zipFolder(this.worldPath, backup.getPath());
                final double backUpTime = ((System.currentTimeMillis() - startTime) / 1000.0);
                this.config.getWatchDogConfig().getBackup().setLastBackupTime(backUpTime);
                this.serverProcess.tellrawToAllAndLogger(this.prefix, "&aUtworzono kopię zapasową w&b " + backUpTime + "&a sekund", LogState.INFO);
                this.status = "Utworzono backup";
                this.loadAvailableBackups();
            } catch (final Exception exception) {
                this.serverProcess.tellrawToAllAndLogger(this.prefix, "&4Nie można utworzyć kopii zapasowej", LogState.CRITICAL);
                this.status = "Nie udało sie utworzyć kopij zapasowej";
                this.logger.critical(exception);
                exception.printStackTrace();
                if (backup.delete()) {
                    this.serverProcess.tellrawToAllAndLogger(this.prefix, "&aUsunięto błędny backup", LogState.INFO);
                } else {
                    this.serverProcess.tellrawToAllAndLogger(this.prefix, "&4Nie można usunąć błędnego backupa", LogState.INFO);
                }
            } finally {
                this.backuping = false;
                this.watchDog.saveResume();
                this.config.save();
            }
        });
    }

    public synchronized void loadBackup(final String backupName) {
        if (this.loading) {
            this.logger.error("&cNie można załadować backup gdy jeden jest już ładowany ");
            return;
        }

        this.service.execute(() -> {
            this.loading = true;
            for (final Path path : this.backups) {
                final String fileName = path.getFileName().toString().replaceAll(".zip", "");
                if (backupName.equalsIgnoreCase(fileName)) {
                    this.logger.info("&aOdnaleziono backup: &b" + backupName);
                    if (this.serverProcess.getProcess() != null) {
                        if (this.serverProcess.isEnabled()) {
                            this.serverProcess.kickAllPlayers(this.prefix + " &aBackup&b " + backupName + " &a jest ładowany!");
                            this.serverProcess.setCanRun(false);
                            this.serverProcess.sendToConsole("stop");
                            try {
                                this.serverProcess.getProcess().waitFor();
//                                FileUtil.renameFolder(Path.of(this.worldPath), Path.of(Defaults.getWorldsPath() + "OLD_" + this.worldName));
                                ThreadUtil.sleep(10); //Usypiam ten wątek aby nie doprowadzić do awarii chunk bo juz tak mi sie wydarzyło
                                ZipUtil.unzipFile(path.toFile().getPath(), Defaults.getWorldsPath(), false);
                            } catch (final Exception ioException) {
                                this.bdsAutoEnable.getDiscord().sendMessage("Świat prawdopodobnie uległ awarii podczas próby załadowania backup");
                                ioException.printStackTrace();
                                System.exit(1);
                                return;
                            } finally {
                                this.loading = false;
                            }
                            this.logger.info("&aZaładowano backup: &b" + backupName);
                            this.serverProcess.setCanRun(true);
                            this.serverProcess.startProcess();
                        }
                    }
                    return;
                }
            }
            this.loading = false;
            this.logger.info("&cNie można odnaleźć backupa: &b" + backupName);
        });
    }

    private void loadAvailableBackups() {
        if (!this.config.getWatchDogConfig().getBackup().isBackup()) return;
        this.backups.clear();
        try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(this.backupFolder.getPath()))) {
            for (final Path path : directoryStream) {
                if (Files.isRegularFile(path) && path.toString().endsWith(".zip")) {
                    this.backups.add(path);
                }
            }
            this.backups.sort(Collections.reverseOrder());
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public long calculateMillisUntilNextBackup() {
        return Math.max(0, MathUtil.minutesToMillis(this.config.getWatchDogConfig().getBackup().getBackupFrequency()) - (System.currentTimeMillis() - this.lastBackupMillis));
    }

    public String getStatus() {
        return this.status;
    }

    public File getWorldFile() {
        return this.worldFile;
    }

    public List<Path> getBackups() {
        return this.backups;
    }
}
