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
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.DiscordType;
import me.indian.bds.discord.NoneDiscord;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.discord.webhook.WebHook;
import me.indian.bds.logger.Logger;
import me.indian.bds.rest.RestWebsite;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.manager.ServerManager;
import me.indian.bds.server.properties.ServerProperties;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.FileUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.version.VersionManager;
import me.indian.bds.watchdog.WatchDog;

public class BDSAutoEnable {

    private final Thread mainThread;
    private final long startTime;
    private final String projectVersion, runDate;
    private final Scanner scanner;
    private final Logger logger;
    private final ServerProperties serverProperties;
    private final AppConfigManager appConfigManager;
    private final AppConfig appConfig;
    private final Settings settings;
    private final ServerProcess serverProcess;
    private final ServerManager serverManager;
    private final VersionManager versionManager;
    private final DiscordIntegration discord;
    private CommandManager commandManager;
    private WatchDog watchDog;

    public BDSAutoEnable() {
        this.mainThread = Thread.currentThread();
        this.startTime = System.currentTimeMillis();
        this.runDate = DateUtil.getFixedDate();
        this.projectVersion = "0.0.1-Dev";
        this.scanner = new Scanner(System.in);
        this.appConfigManager = new AppConfigManager();
        this.appConfig = this.appConfigManager.getAppConfig();
        this.logger = new Logger(this);
        this.logger.alert("&lNumer wersji projektu:&1 &n" + this.projectVersion);
        this.logger.debug("&aUUID&r aplikacji:&b " + this.getAppUUID());
        DefaultsVariables.init(this);
        this.discord = this.determinateDiscordIntegration();
        this.isJavaVersionLessThan17();
        this.checkSystemSupport();
        this.checkEncoding();
        this.checkFlags();
        this.checkMemory();
        this.checkTimeZone();
        this.serverProperties = new ServerProperties(this);
        this.settings = new Settings(this);
        this.serverManager = new ServerManager(this);
        this.serverProcess = new ServerProcess(this);
        this.serverManager.init();
        this.versionManager = new VersionManager(this);
        StatusUtil.init(this);

        this.init();
    }

    public static void main(final String[] args) {
        new BDSAutoEnable();
    }

    public void init() {
        new ShutdownHandler(this);
        this.settings.loadSettings(this.scanner);
        this.watchDog = new WatchDog(this);
        this.serverProcess.init();
        this.watchDog.init(this.discord);
        this.watchDog.getRamMonitor().monitRamUsage();
        this.versionManager.loadVersion();
        this.checkExecutable();
        this.watchDog.getPackModule().initPackModule();
        new RestWebsite(this).init();
        this.discord.init();
        this.serverManager.getStatsManager().startCountServerTime(this.serverProcess);
        this.serverProcess.startProcess();
        this.versionManager.getVersionUpdater().checkForUpdate();
        this.commandManager = new CommandManager(this);
        new ConsoleInput(this.scanner, this);
        new AutoMessages(this).start();
        new Metrics(this);
    }

    public void isJavaVersionLessThan17() {
        final String javaVersion = System.getProperty("java.version");

        if (DefaultsVariables.isJavaLoverThan17()) {
            if (this.appConfig.isDebug()) {
                this.logger.warning("&aDebug włączony, twoja wersja javy &d(&1" + javaVersion
                        + "&d)&a nie jest wspierana, robisz to na własne&c ryzyko&c!");
                return;
            }

            this.logger.critical("Twoja wersja javy (&1" + javaVersion
                    + "&r) jest zbyt niska! Potrzebujesz javy &117+ ");
            System.exit(0);
        }
    }

    private void checkSystemSupport() {
        if (DefaultsVariables.getSystem() == SystemOS.UNSUPPORTED) {
            /*
            if (this.config.isDebug()) {
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

    private DiscordIntegration determinateDiscordIntegration() {
        final DiscordType integration = this.appConfigManager.getDiscordConfig().getIntegrationType();
        if (integration == null) throw new RuntimeException("Integracja z discord nie może być nullem!");

        switch (integration) {
            case WEBHOOK -> {
                return new WebHook(this);
            }
            case JDA -> {
                return new DiscordJda(this);
            }
            case NONE -> {
                return new NoneDiscord();
            }
            default -> throw new RuntimeException("Nieznany typ integracji " + integration);
        }
    }

    public long getStartTime() {
        return this.startTime;
    }

    public boolean isMainThread(){
        return Thread.currentThread() == this.mainThread;
    }

    public String getRunDate() {
        return this.runDate;
    }

    public String getProjectVersion() {
        return this.projectVersion;
    }

    public String getAppUUID() {
        if (this.appConfig.getUuid().isEmpty()) {
            this.appConfig.setUuid(UUID.randomUUID().toString());
            this.appConfig.save();
        }
        return this.appConfig.getUuid();
    }

    public AppConfigManager getAppConfigManager() {
        return this.appConfigManager;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public ServerProcess getServerProcess() {
        return this.serverProcess;
    }

    public VersionManager getVersionManager() {
        return this.versionManager;
    }

    public ServerManager getServerManager() {
        return this.serverManager;
    }

    public DiscordIntegration getDiscord() {
        return this.discord;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public ServerProperties getServerProperties() {
        return this.serverProperties;
    }

    public WatchDog getWatchDog() {
        return this.watchDog;
    }
}
