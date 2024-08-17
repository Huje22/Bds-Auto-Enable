package me.indian.bds;

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
import me.indian.bds.command.CommandManager;
import me.indian.bds.config.AppConfig;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.config.LogbackConfig;
import me.indian.bds.event.EventManager;
import me.indian.bds.exception.MissingDllException;
import me.indian.bds.extension.ExtensionManager;
import me.indian.bds.logger.impl.MainLogger;
import me.indian.bds.metrics.AppMetrics;
import me.indian.bds.pack.PackManager;
import me.indian.bds.server.ServerManager;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.allowlist.AllowlistManager;
import me.indian.bds.server.properties.ServerProperties;
import me.indian.bds.shutdown.ShutdownHandler;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.PlayerStatsUtil;
import me.indian.bds.util.ServerUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.util.geyser.GeyserUtil;
import me.indian.bds.version.VersionManager;
import me.indian.bds.watchdog.WatchDog;
import me.indian.util.DateUtil;
import me.indian.util.FileUtil;
import me.indian.util.MathUtil;
import me.indian.util.MessageUtil;
import me.indian.util.ZipUtil;
import me.indian.util.system.SystemArch;
import me.indian.util.system.SystemOS;
import me.indian.util.system.SystemUtil;

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
        this.logger.print("""
                &e ____  _____   _____ &1                _          ______             _     _     \s
                &e|  _ \\|  __ \\ / ____|&1     /\\        | |        |  ____|           | |   | |    \s
                &e| |_) | |  | | (___  &1    /  \\  _   _| |_ ___   | |__   _ __   __ _| |__ | | ___\s
                &e|  _ <| |  | |\\___ \\ &1   / /\\ \\| | | | __/ _ \\  |  __| | '_ \\ / _` | '_ \\| |/ _ \\
                &e| |_) | |__| |____) |&1  / ____ \\ |_| | || (_) | | |____| | | | (_| | |_) | |  __/
                &e|____/|_____/|_____/ &1 /_/    \\_\\__,_|\\__\\___/  |______|_| |_|\\__,_|_.__/|_|\\___|
                """);
        this.isJavaVersionLessThan17();
        this.checkSystemSupport();
        this.checkEncoding();
        this.checkDlls();
        this.checkFlags();
        this.checkMemory();
        this.checkTimeZone();
        this.logger.info("&lNumer wersji projektu:&1 &n" + this.projectVersion);
        this.logger.debug("&aUUID&r aplikacji:&b " + this.getAppUUID());
        DefaultsVariables.init(this);
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
        ZipUtil.init(this.logger);
        PlayerStatsUtil.init(this);

        this.init();
    }

    public static void main(final String[] args) {
        new BDSAutoEnable();
        if (SystemTray.isSupported()) new AppTray();
    }

    public void init() {
        this.setAppWindowName("Inicjalizowanie.....");
        new ShutdownHandler(this);
        this.settings.loadSettings(this.mainScanner);
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
                + SystemUtil.getFullOsNameWithDistribution() + " &5(&c" + SystemUtil.getFullyArchCode() + "&5)");
    }

    private void checkSystemSupport() {
        final SystemArch arch = SystemUtil.getCurrentArch();
        final SystemOS systemOS = SystemUtil.getSystem();

        if (arch == SystemArch.ARM_32X || arch == SystemArch.ARM_64X || arch == SystemArch.AMD_X32) {
            if (this.appConfig.isDebug()) {
                this.logger.warning("&aTwoja architektura systemu nie jest wspierana," +
                        " lecz masz włączony&1 Debug&a robisz to na własne&c ryzyko&c!");
                this.logger.alert("&aMoże nie udać się nam uruchomić &bBedrock Dedicated Server&a na twoim systemie");
                return;
            }

            this.logger.critical("&cTwoja architektura systemu nie jest wspierana! Twoja architektura to&b " + SystemUtil.getFullyArchCode());
            System.exit(9);
        }

        if (systemOS == SystemOS.UNSUPPORTED) {
            this.logger.critical("&cTwój system nie jest wspierany!!&r Twój system to:&1 " + SystemUtil.getFullOsNameWithDistribution());
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
        final long maxComputerMem = MathUtil.bytesToMB(StatusUtil.getAvailableRam());
        final long maxMem = MathUtil.bytesToMB(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());

        if (maxMem < 1000) {
            this.logger.warning("&cWykryto małą ilość pamięci przeznaczonej dla aplikacji! &b(&a" + maxMem + " mb&b)");
        }

        if (maxComputerMem < 4000) {
            this.logger.warning("&cTwoja maszyna posiada tylko&b " + maxComputerMem + "mb&c ramu, zalecane jest&b 4024mb");
        }
    }

    private void checkExecutable() {
        final String serverPath = this.appConfig.getFilesPath() + File.separator + DefaultsVariables.getDefaultFileName();
        if (!FileUtil.canExecute(serverPath)) {
            if (!FileUtil.addExecutePerm(serverPath)) {
                if (!FileUtil.canExecute(serverPath)) {
                    this.logger.critical("&cBrak odpowiednich uprawnień!");
                    System.exit(9);
                }
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
        this.service.execute(() -> {
            try {
                final ProcessBuilder processBuilder = new ProcessBuilder();

                switch (SystemUtil.getSystem()) {
                    case WINDOWS -> processBuilder.command("cmd.exe", "/c", "title", name);
                    case LINUX -> processBuilder.command("bash", "-c", "printf '\\033]0;%s\\007' \"" + name + "\"");
                }

                processBuilder.inheritIO().start().waitFor();
            } catch (final IOException | InterruptedException exception) {
                this.logger.debug("&cNie udało się zmienić nazwy okna na:&d \"&1" + name + "&d\"", exception);
            }
        });
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

        new Timer("Console Name Changer", true).scheduleAtFixedRate(timerTask, seconds, seconds);
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