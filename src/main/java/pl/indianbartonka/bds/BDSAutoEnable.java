package pl.indianbartonka.bds;

import java.awt.SystemTray;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import pl.indianbartonka.bds.command.CommandManager;
import pl.indianbartonka.bds.config.AppConfig;
import pl.indianbartonka.bds.config.AppConfigManager;
import pl.indianbartonka.bds.config.LogbackConfig;
import pl.indianbartonka.bds.event.EventManager;
import pl.indianbartonka.bds.exception.MissingDllException;
import pl.indianbartonka.bds.extension.ExtensionManager;
import pl.indianbartonka.bds.logger.MainLogger;
import pl.indianbartonka.bds.metrics.AppMetrics;
import pl.indianbartonka.bds.pack.PackManager;
import pl.indianbartonka.bds.server.ServerManager;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.bds.server.allowlist.AllowlistManager;
import pl.indianbartonka.bds.server.properties.ServerProperties;
import pl.indianbartonka.bds.shutdown.ShutdownHandler;
import pl.indianbartonka.bds.util.DefaultsVariables;
import pl.indianbartonka.bds.util.PlayerStatsUtil;
import pl.indianbartonka.bds.util.ServerUtil;
import pl.indianbartonka.bds.util.StatusUtil;
import pl.indianbartonka.bds.util.geyser.GeyserUtil;
import pl.indianbartonka.bds.version.VersionManager;
import pl.indianbartonka.bds.watchdog.WatchDog;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.util.MemoryUnit;
import pl.indianbartonka.util.MessageUtil;
import pl.indianbartonka.util.ZipUtil;
import pl.indianbartonka.util.FileUtil;
import pl.indianbartonka.util.system.SystemArch;
import pl.indianbartonka.util.system.SystemFamily;
import pl.indianbartonka.util.system.SystemOS;
import pl.indianbartonka.util.system.SystemUtil;

public class BDSAutoEnable {

    private static boolean instanceRuned = false;
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
    private final McLog mcLog;
    private final EventManager eventManager;
    private final ExtensionManager extensionManager;
    private final ExecutorService service;
    private AllowlistManager allowlistManager;
    private PackManager packManager;
    private CommandManager commandManager;
    private WatchDog watchDog;

    public BDSAutoEnable() {
        if (instanceRuned) {
            throw new IllegalStateException("Można utworzyć tylko jedną instancję klasy BDSAutoEnable.");
        }
        instanceRuned = true;
        this.mainThread = Thread.currentThread();
        this.startTime = System.currentTimeMillis();
        this.runDate = DateUtil.getFixedDate();
        this.projectVersion = "0.0.1-Dev";
        this.mainScanner = new Scanner(System.in);
        this.appConfigManager = new AppConfigManager();
        this.appConfig = this.appConfigManager.getAppConfig();
        this.logger = new MainLogger(this);
        this.logger.println("""
                &e ____  _____   _____ &1                _          ______             _     _     \s
                &e|  _ \\|  __ \\ / ____|&1     /\\        | |        |  ____|           | |   | |    \s
                &e| |_) | |  | | (___  &1    /  \\  _   _| |_ ___   | |__   _ __   __ _| |__ | | ___\s
                &e|  _ <| |  | |\\___ \\ &1   / /\\ \\| | | | __/ _ \\  |  __| | '_ \\ / _` | '_ \\| |/ _ \\
                &e| |_) | |__| |____) |&1  / ____ \\ |_| | || (_) | | |____| | | | (_| | |_) | |  __/
                &e|____/|_____/|_____/ &1 /_/    \\_\\__,_|\\__\\___/  |______|_| |_|\\__,_|_.__/|_|\\___|
                """);
        DefaultsVariables.init(this);
        this.isJavaVersionLessThan17();
        this.checkSystemSupport();
        this.checkEncoding();
        this.checkDlls();
        this.checkFlags();
        this.checkMemory();
        this.checkTimeZone();
        this.logger.info("&lNumer wersji projektu:&1 &n" + this.projectVersion);
        this.logger.debug("&aUUID&r aplikacji:&b " + this.getAppUUID());
        LogbackConfig.init();
        this.serverProperties = new ServerProperties(this);
        this.settings = new Settings(this);
        this.eventManager = new EventManager(this);
        this.serverManager = new ServerManager(this);
        this.serverProcess = new ServerProcess(this);
        this.versionManager = new VersionManager(this);
        this.mcLog = new McLog(this);
        this.extensionManager = new ExtensionManager(this);
        this.service = Executors.newSingleThreadExecutor();
        this.serverManager.init();
        ServerUtil.init(this);
        GeyserUtil.init(this);
        StatusUtil.init(this);
        ZipUtil.init(this.logger, 9);
        PlayerStatsUtil.init(this);

        this.init();
    }

    public static void main(final String[] args) {
        new BDSAutoEnable();
        if (SystemTray.isSupported()) new AppTray();
    }

    public static boolean isImportantThread() {
        final String threadName = Thread.currentThread().getName();
        return threadName.contains("Console") || threadName.contains("Server process");
    }

    public void init() {
        this.setAppWindowName("Inicjalizowanie.....");
        new ShutdownHandler(this);
        this.settings.loadSettings();
        this.packManager = new PackManager(this);
        this.allowlistManager = new AllowlistManager(this);
        this.watchDog = new WatchDog(this);
        this.serverProcess.init();
        this.watchDog.init();
        this.versionManager.loadVersion();
        this.checkExecutable();
        this.serverManager.getStatsManager().startCountServerTime(this.serverProcess);
        this.extensionManager.loadExtensions();
        this.versionManager.getVersionUpdater().checkForUpdate();
        this.commandManager = new CommandManager(this);

        new ConsoleInput(this.mainScanner, this);
        new AppMetrics(this);

        this.extensionManager.enableExtensions();
        this.serverProcess.startProcess();
        this.setAppWindowName("Zainicjowano");
        this.runAutoPromotion();
        this.setAppName();
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
            System.exit(9);
        }

        this.logger.info("&aUżyto Java: &b" + System.getProperty("java.vm.name") + " &1" + javaVersion + " &5(&d" + System.getProperty("java.vendor") + "&5)&r na&f "
                + SystemUtil.getFullOSNameWithDistribution() + " &5(&c" + SystemUtil.getFullyArchCode() + "&5)");
    }

    private void checkSystemSupport() {
        final SystemArch arch = SystemUtil.getCurrentArch();
        final SystemOS systemOS = SystemUtil.getSystem();

        if (arch == SystemArch.AMD_X32) {
            this.logger.critical("&cTwoja architektura systemu nie jest wspierana! Twoja architektura to&b " + SystemUtil.getFullyArchCode());
            System.exit(9);
        }

        if (arch == SystemArch.ARM_32X || arch == SystemArch.ARM_64X) {
            //Windows ma jakis emulator na ARM do apek AMD ale nie mialem okazij uzywac BDS na nim
            if (DefaultsVariables.box64) {
                this.logger.warning("&cTwoja architektura systemu nie jest wspierana!&a Ale posiadasz&e Box64&c!");
                return;
            } else {
                this.logger.critical("&cTwoja architektura systemu nie jest wspierana! Twoja architektura to&b " + SystemUtil.getFullyArchCode());
                if (SystemUtil.getSystemFamily() == SystemFamily.UNIX) {
                    this.logger.warning("&aSpróbuj użyć&b Box64");
                }
                System.exit(9);
            }
        }

        if (systemOS == SystemOS.UNKNOWN) {
            this.logger.critical("&cTwój system nie jest znany!!&r Twój system to:&1 " + SystemUtil.getFullOSNameWithDistribution());
            System.exit(9);
        }
    }

    private void checkFlags() {
        final List<String> flags = ManagementFactory.getRuntimeMXBean().getInputArguments();
        if (flags.isEmpty()) return;
        this.logger.debug("Wykryte flagi startowe &d(&1" + flags.size() + "&d):&b " + MessageUtil.stringListToString(flags, " &a,&b "));
    }

    private void checkEncoding() {
        final String encoding = Charset.defaultCharset().displayName();
        if (!encoding.equalsIgnoreCase("UTF-8")) {
            if (!this.appConfig.isDebug()) {
                this.logger.critical("&cTwoje kodowanie to:&b " + encoding + ", &cmy wspieramy tylko&b: UTF-8");
                this.logger.critical("&cProsimy ustawić swoje kodowanie na&b UTF-8&c abyśmy mogli dalej kontynuować!");
                System.exit(-10);
            } else {
                this.logger.warning("&aDebug włączony, omijasz wymóg &bUTF-8&a na własne&c ryzyko&c!");
                System.setProperty("file.encoding", "UTF-8");
            }
        } else {
            this.logger.debug("Wykryto wspierane kodowanie:&b " + encoding);
        }
    }

    private void checkMemory() {
        final long maxComputerMem = MemoryUnit.BYTES.to(SystemUtil.getMaxRam(), MemoryUnit.MEGABYTES);
        final long maxMem = MemoryUnit.BYTES.to(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax(), MemoryUnit.MEGABYTES);

        if (maxMem < 1000) {
            this.logger.warning("&cWykryto małą ilość pamięci przeznaczonej dla aplikacji! &b(&a" + maxMem + " mb&b)");
        }

        if (maxComputerMem < 4000) {
            this.logger.warning("&cTwoja maszyna posiada tylko&b " + maxComputerMem + "mb&c ramu, zalecane jest&b 4024mb");
        }
    }

    private void checkExecutable() {
        try {
            final String serverPath = this.appConfig.getFilesPath() + File.separator + DefaultsVariables.getDefaultFileName();
            if (!FileUtil.canExecute(serverPath)) {
                if (!FileUtil.addExecutePerm(serverPath)) {
                    if (!FileUtil.canExecute(serverPath)) {
                        this.logger.critical("&cBrak odpowiednich uprawnień!");
                        System.exit(9);
                    }
                }
            }
        } catch (final Exception exception) {
            this.logger.critical("&cNie udało się nadać uprawnień do wystartowania servera", exception);
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

    private void runAutoPromotion() {
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ServerUtil.tellrawToAll("&bTen server używa &aBDS-Auto-Enable&3 https://github.com/Huje22/Bds-Auto-Enable");
            }
        };

        new Timer("AutoPromotion", true)
                .scheduleAtFixedRate(timerTask, 0, DateUtil.minutesTo(10, TimeUnit.MILLISECONDS));
    }

    public void setAppWindowName(final String name) {
        if (System.console() == null) return;

        this.service.execute(() -> SystemUtil.setConsoleName(name));
    }

    private void setAppName() {
        final long seconds = DateUtil.secondToMillis(1);

        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!ShutdownHandler.isShutdownHookCalled()) {
                    if (BDSAutoEnable.this.serverProcess.isEnabled()) {
                        BDSAutoEnable.this.setAppWindowName(StatusUtil.getShortStatus());
                    }
                } else {
                    this.cancel();
                }
            }
        };

        if (System.console() == null) {
            this.logger.debug("&cOkno konsoli jest nie dostępne");
        } else {
            this.logger.debug("&aOkno konsoli jest dostępne");
            new Timer("Console Name Changer", true).scheduleAtFixedRate(timerTask, seconds, seconds);
        }
    }

    public long getStartTime() {
        return this.startTime;
    }

    public boolean isMainThread() {
        return Thread.currentThread() == this.mainThread;
    }

    public boolean isMainThread(final Thread thread) {
        return thread == this.mainThread;
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

    public McLog getMcLog() {
        return this.mcLog;
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

    public AllowlistManager getAllowlistManager() {
        return this.allowlistManager;
    }

    public PackManager getPackManager() {
        return this.packManager;
    }
}