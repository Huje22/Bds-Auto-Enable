package me.indian.bds;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Scanner;
import me.indian.bds.config.Config;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.discord.webhook.WebHook;
import me.indian.bds.file.ServerProperties;
import me.indian.bds.logger.Logger;
import me.indian.bds.manager.PlayerManager;
import me.indian.bds.manager.VersionManager;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MinecraftUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.watchdog.WatchDog;


public class BDSAutoEnable {

    private final long startTime;
    private final String projectVersion;
    private final Scanner scanner;
    private final Logger logger;
    private final ServerProperties serverProperties;
    private final Config config;
    private final Settings settings;
    private final ServerProcess serverProcess;
    private final PlayerManager playerManager;
    private final VersionManager versionManager;
    private DiscordIntegration discord;
    private WatchDog watchDog;
    private String runDate;

    public BDSAutoEnable() {
        this.initRunDate();
        this.startTime = System.currentTimeMillis();
        this.projectVersion = "1.0.0-Dev";
        this.scanner = new Scanner(System.in);
        this.config = ConfigManager.create(Config.class, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile("BDS-Auto-Enable/config.yml");
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
        Defaults.init(this);
        this.logger = new Logger(this);
        this.logger.alert("Numer wersji projektu: " + this.projectVersion);
        this.checkEncoding();
        this.checkFlags();
        this.checkMemory();
        switch (this.config.getIntegrationType()) {
            case WEBHOOK -> this.discord = new WebHook(this);
            case JDA -> this.discord = new DiscordJda(this);
            default -> {

            }
        }
        this.serverProperties = new ServerProperties(this);
        this.settings = new Settings(this);
        this.playerManager = new PlayerManager(this);
        this.serverProcess = new ServerProcess(this);
        this.versionManager = new VersionManager(this);
        MinecraftUtil.initMinecraftUtil(this);
        StatusUtil.init(this , this.serverProcess);
        if (this.discord instanceof DiscordJda jda) jda.initServerProcess(this.serverProcess);

        this.init();
    }

    public static void main(final String[] args) {
        new BDSAutoEnable();
    }

    public void init() {
        this.settings.loadSettings(this.scanner);
        this.shutdownHook();
        this.watchDog = new WatchDog(this);
        this.serverProcess.initWatchDog(this.watchDog);
        this.watchDog.getBackupModule().initBackupModule(this.watchDog, this.serverProcess);
        this.watchDog.getBackupModule().backup();
        this.versionManager.loadVersion();
        this.config.save();
        this.discord.init();
        this.serverProcess.startProcess();
    }

    private void initRunDate() {
        this.runDate = DateUtil.getDate().replaceAll(":", "-");
    }

    private void checkFlags() {
        final List<String> flags = ManagementFactory.getRuntimeMXBean().getInputArguments();
        if (flags.isEmpty()) return;
        this.logger.debug("Wykryte flagi startowe:&b " + flags.toString().replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll(",", " &a,&b"));
    }

    private void checkEncoding() {
        final String encoding = System.getProperty("file.encoding");
        if (!encoding.equalsIgnoreCase("UTF-8")) {
            this.logger.critical("&cTwoje kodowanie to:&b " + encoding + ", &cmy wspieramy tylko&b: UTF-8");
            this.logger.critical("&cProsimy ustawić swoje kodowanie na&b UTF-8&c abyśmy mogli dalej kontunować!");
            if (!this.config.isDebug()) {
                System.exit(-2137);
            } else {
                this.logger.debug("&aDebug włączony, omijasz wymóg &bUTF-8&a na własne ryzyko&c!");
            }
        } else {
            this.logger.debug("Wykryto wspierane kodowanie");
        }
    }

    private void checkMemory() {
        final long maxMem = MathUtil.bytesToMB(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());
        if (maxMem < 1000) this.logger.critical("&cWykryto małą ilość pamieci przeznaczonej dla aplikacij! &b(&a" + maxMem + " mb&b)");
    }

    public long getStartTime() {
        return this.startTime;
    }

    private void shutdownHook() {
        final Thread shutdown = new Thread(() -> {
            try {
                if (this.scanner != null) this.scanner.close();
                this.serverProcess.instantShutdown();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
        shutdown.setName("Shutdown");
        Runtime.getRuntime().addShutdownHook(shutdown);
    }

    public String getRunDate() {
        return this.runDate;
    }

    public String getProjectVersion() {
        return this.projectVersion;
    }

    public Config getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public ServerProcess getServerProcess() {
        return this.serverProcess;
    }

    public VersionManager getVersionManager() {
        return this.versionManager;
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public DiscordIntegration getDiscord() {
        return this.discord;
    }

    public ServerProperties getServerProperties() {
        return this.serverProperties;
    }

    public WatchDog getWatchDog() {
        return this.watchDog;
    }
}