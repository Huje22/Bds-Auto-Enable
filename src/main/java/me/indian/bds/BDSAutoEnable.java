package me.indian.bds;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.ZoneId;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import me.indian.bds.command.CommandManager;
import me.indian.bds.config.AppConfig;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.event.EventManager;
import me.indian.bds.exception.MissingDllException;
import me.indian.bds.extension.ExtensionManager;
import me.indian.bds.logger.impl.MainLogger;
import me.indian.bds.metrics.AppMetrics;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.manager.ServerManager;
import me.indian.bds.server.properties.ServerProperties;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.FileUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.util.ZipUtil;
import me.indian.bds.util.system.SystemArch;
import me.indian.bds.util.system.SystemOS;
import me.indian.bds.util.system.SystemUtil;
import me.indian.bds.version.VersionManager;
import me.indian.bds.watchdog.WatchDog;

public class BDSAutoEnable {


     /*
        TODO: Doda zarządzanie allow listą
      */

    private final Thread mainThread;
    private final long startTime;
    private final String projectVersion, runDate;
    private final Scanner mainScanner;
    private final MainLogger logger;
    private final ServerProperties serverProperties;
    private final AppConfigManager appConfigManager;
    private final AppConfig appConfig;
    private final Settings settings;
    private final ServerProcess serverProcess;
    private final ServerManager serverManager;
    private final VersionManager versionManager;
    private final EventManager eventManager;
    private final ExtensionManager extensionManager;
    private CommandManager commandManager;
    private WatchDog watchDog;

    public BDSAutoEnable() {
        this.mainThread = Thread.currentThread();
        this.startTime = System.currentTimeMillis();
        this.runDate = DateUtil.getFixedDate();
        this.projectVersion = "0.0.1-Dev";
        this.mainScanner = new Scanner(System.in);
        this.appConfigManager = new AppConfigManager();
        this.appConfig = this.appConfigManager.getAppConfig();
        this.logger = new MainLogger(this);
        this.isJavaVersionLessThan17();
        this.checkSystemSupport();
        this.checkEncoding();
        this.checkDlls();
        this.checkFlags();
        this.checkMemory();
        this.checkTimeZone();
        this.logger.print("  ____  _____   _____                 _          ______             _     _      \n |  _ \\|  __ \\ / ____|     /\\        | |        |  ____|           | |   | |     \n | |_) | |  | | (___      /  \\  _   _| |_ ___   | |__   _ __   __ _| |__ | | ___ \n |  _ <| |  | |\\___ \\    / /\\ \\| | | | __/ _ \\  |  __| | '_ \\ / _` | '_ \\| |/ _ \\ \n | |_) | |__| |____) |  / ____ \\ |_| | || (_) | | |____| | | | (_| | |_) | |  __/ \n |____/|_____/|_____/  /_/    \\_\\__,_|\\__\\___/  |______|_| |_|\\__,_|_.__/|_|\\___|");
        this.logger.alert("&lNumer wersji projektu:&1 &n" + this.projectVersion);
        this.logger.debug("&aUUID&r aplikacji:&b " + this.getAppUUID());
        DefaultsVariables.init(this);
        this.serverProperties = new ServerProperties(this);
        this.settings = new Settings(this);
        this.eventManager = new EventManager(this);
        this.serverManager = new ServerManager(this);
        this.serverProcess = new ServerProcess(this);
        this.versionManager = new VersionManager(this);
        this.extensionManager = new ExtensionManager(this);
        this.serverManager.init();
        StatusUtil.init(this);
        ZipUtil.init(this);

        this.init();
    }

    public static void main(final String[] args) {
        new BDSAutoEnable();
    }

    public void init() {
        new ShutdownHandler(this);
        this.settings.loadSettings(this.mainScanner);
        this.watchDog = new WatchDog(this);
        this.serverProcess.init();
        this.watchDog.init();
        this.watchDog.getRamMonitor().monitRamUsage();
        this.versionManager.loadVersion();
        this.checkExecutable();
        this.watchDog.getPackModule().initPackModule();
        this.serverManager.getStatsManager().startCountServerTime(this.serverProcess);
        this.extensionManager.loadExtensions();
        this.versionManager.getVersionUpdater().checkForUpdate();
        this.commandManager = new CommandManager(this);

        new ConsoleInput(this.mainScanner, this);
        new AppMetrics(this);

        this.extensionManager.enableExtensions();
        this.serverProcess.startProcess();
    }

    private void isJavaVersionLessThan17() {
        final String javaVersion = System.getProperty("java.version");

        if (DefaultsVariables.isJavaLoverThan17()) {
            if (this.appConfig.isDebug()) {
                this.logger.warning("&aDebug włączony, twoja wersja javy &d(&1" + javaVersion
                        + "&d)&a nie jest wspierana, robisz to na własne&c ryzyko&c!");
                return;
            }

            this.logger.critical("Twoja wersja javy &d(&1" + javaVersion
                    + "&d)&r jest zbyt niska! Potrzebujesz javy &117+ ");
            System.exit(0);
        }
    }

    private void checkSystemSupport() {
        final SystemArch arch = SystemArch.getCurrentArch();
        final SystemOS systemOS = SystemUtil.getSystem();

        if (arch == SystemArch.ARM || arch == SystemArch.AMD_X32) {
            if (this.appConfig.isDebug()) {
                this.logger.warning("&aTwoja architektura systemu nie jest wspierana," +
                        " lecz masz włączony&1 Debug&a robisz to na własne&c ryzyko&c!");
                this.logger.alert("&aMoże nie udać się nam uruchomić &bBedrock Dedicated Server&a na twoim systemie");
                return;
            }
            this.logger.critical("&cTwoja architektura systemu nie jest wspierana! Twoja architektura to&b "
                    + SystemArch.getFullyArchCode());
            System.exit(0);
        }

        if (systemOS == SystemOS.UNSUPPORTED) {
            /*
            if (this.appConfig.isDebug()) {
                this.logger.warning("&aTwój system nie jest wspierany lecz masz włączony&1 Debug&a robisz to na własne&c ryzyko&c!");
                return;
            }
            */

            this.logger.critical("&cTwój system nie jest wspierany!!&r Twój system to:&1 " + System.getProperty("os.name"));
            System.exit(0);
        }
    }

    private void checkFlags() {
        final List<String> flags = ManagementFactory.getRuntimeMXBean().getInputArguments();
        if (flags.isEmpty()) return;
        this.logger.debug("Wykryte flagi startowe:&b " + MessageUtil.stringListToString(flags, " &a,&b "));
    }

    private void checkEncoding() {
        final String encoding = System.getProperty("file.encoding");
        if (!encoding.equalsIgnoreCase("UTF-8")) {
            if (!this.appConfig.isDebug()) {
                this.logger.critical("&cTwoje kodowanie to:&b " + encoding + ", &cmy wspieramy tylko&b: UTF-8");
                this.logger.critical("&cProsimy ustawić swoje kodowanie na&b UTF-8&c abyśmy mogli dalej kontynuować!");
                System.exit(-2137);
            } else {
                this.logger.warning("&aDebug włączony, omijasz wymóg &bUTF-8&a na własne&c ryzyko&c!");
                System.setProperty("file.encoding", "UTF-8");
            }
        } else {
            this.logger.debug("Wykryto wspierane kodowanie");
        }
    }

    private void checkMemory() {
        final long maxMem = MathUtil.bytesToMB(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());
        if (maxMem < 1000)
            this.logger.warning("&cWykryto małą ilość pamięci przeznaczonej dla aplikacji! &b(&a" + maxMem + " mb&b)");
    }

    private void checkExecutable() {
        final String serverPath = this.appConfig.getFilesPath() + File.separator + DefaultsVariables.getDefaultFileName();
        if (!FileUtil.canExecute(serverPath)) {
            if (!FileUtil.addExecutePerm(serverPath)) {
                this.logger.critical("&cBrak odpowiednich uprawnień!");
                System.exit(0);
            }
        }
    }

    private void checkTimeZone() {
        if (!DefaultsVariables.isPolisTimeZone()) {
            this.logger.warning("Twoja strefa czasowa to:&1 " + ZoneId.systemDefault() + "&r czas logów aplikacji bedzie polski a servera inny ");
        }
    }

    private void checkDlls() {
        if (SystemUtil.getSystem() == SystemOS.WINDOWS) {
            final File vcruntime = new File("C:\\Windows\\System32\\vcruntime140_1.dll");
            final File msvcp = new File("C:\\Windows\\System32\\msvcp140.dll");

            if (!vcruntime.exists() || !msvcp.exists()) {
                if (!vcruntime.exists()) {
                    this.logger.critical("Brak&b vcruntime140_1.dll&r bez tego nie możemy uruchomić&e BDS");
                }

                if (!msvcp.exists()) {
                    this.logger.critical("Brak&b msvcp140.dll&r bez tego nie możemy uruchomić&e BDS");
                }
                throw new MissingDllException("Musisz pobrać pakiet redystrybucyjny programu Visual C++ (x64 i x86)");
            }
        }
    }

    public long getStartTime() {
        return this.startTime;
    }

    public boolean isMainThread() {
        return Thread.currentThread() == this.mainThread;
    }

    public String getAppUUID() {
        if (this.appConfig.getUuid().isEmpty()) {
            this.appConfig.setUuid(UUID.randomUUID().toString());
            this.appConfig.save();
        }
        return this.appConfig.getUuid();
    }

    public String getProjectVersion() {
        return this.projectVersion;
    }

    public String getRunDate() {
        return this.runDate;
    }

    public Scanner getMainScanner() {
        return this.mainScanner;
    }

    public MainLogger getLogger() {
        return this.logger;
    }

    public ServerProperties getServerProperties() {
        return this.serverProperties;
    }

    public AppConfigManager getAppConfigManager() {
        return this.appConfigManager;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public ServerProcess getServerProcess() {
        return this.serverProcess;
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public ServerManager getServerManager() {
        return this.serverManager;
    }

    public VersionManager getVersionManager() {
        return this.versionManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public WatchDog getWatchDog() {
        return this.watchDog;
    }

    public ExtensionManager getExtensionManager() {
        return this.extensionManager;
    }
}