package me.indian.bds.discord.jda;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.jda.listener.CommandListener;
import me.indian.bds.discord.jda.listener.JDAListener;
import me.indian.bds.discord.jda.listener.MessageListener;
import me.indian.bds.discord.jda.manager.LinkingManager;
import me.indian.bds.discord.jda.manager.StatsChannelsManager;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.PackModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class DiscordJda implements DiscordIntegration {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfigManager appConfigManager;
    private final DiscordConfig discordConfig;
    private final long serverID, channelID, consoleID;
    private final ExecutorService consoleService;
    private final List<JDAListener> listeners;
    private JDA jda;
    private Guild guild;
    private TextChannel textChannel, consoleChannel;
    private StatsChannelsManager statsChannelsManager;
    private LinkingManager linkingManager;

    public DiscordJda(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfigManager = bdsAutoEnable.getAppConfigManager();
        this.discordConfig = this.appConfigManager.getDiscordConfig();
        this.serverID = this.discordConfig.getDiscordBotConfig().getServerID();
        this.channelID = this.discordConfig.getDiscordBotConfig().getChannelID();
        this.consoleID = this.discordConfig.getDiscordBotConfig().getConsoleID();
        this.consoleService = Executors.newSingleThreadExecutor(new ThreadUtil("Discord-Console"));

        this.listeners = new ArrayList<>();
        this.listeners.add(new CommandListener(this, this.bdsAutoEnable));
        this.listeners.add(new MessageListener(this, this.bdsAutoEnable));

    }

    @Override
    public void init() {
        final PackModule packModule = this.bdsAutoEnable.getWatchDog().getPackModule();
        this.logger.info("&aŁadowanie bota....");
        if (this.discordConfig.getDiscordBotConfig().getToken().isEmpty()) {
            this.logger.alert("&aNie znaleziono tokenu , pomijanie ładowania.");
            return;
        }
        if (!packModule.isLoaded()) {
            this.logger.error("&cNie załadowano paczki&b " + packModule.getPackName() + "&4.&cBot nie może bez niej normalnie działać");
            this.logger.error("Możesz znaleźć ją tu:&b https://github.com/Huje22/BDS-Auto-Enable-Managment-Pack");
            return;
        }

        try {
            this.jda = JDABuilder.create(this.discordConfig.getDiscordBotConfig().getToken(), GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.MESSAGE_CONTENT)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                    .enableCache(CacheFlag.EMOJI)
                    .setEnableShutdownHook(false)
                    .build();
            this.jda.awaitReady();
            this.logger.info("&aZaładowano bota");
        } catch (final Exception exception) {
            this.logger.critical("&aNie można uruchomić bota , sprawdź podany &bTOKEN", exception);
            return;
        }

        this.guild = this.jda.getGuildById(this.serverID);
        if (this.guild == null) {
            this.jda.shutdown();
            this.jda = null;
            throw new NullPointerException("Nie można odnaleźć servera o ID&b " + this.serverID);
        }

        this.textChannel = this.guild.getTextChannelById(this.channelID);
        if (this.textChannel == null) {
            this.jda.shutdown();
            this.jda = null;
            throw new NullPointerException("Nie można odnaleźć kanału z ID&b " + this.channelID);
        }

        this.textChannel.getManager().setSlowmode(1).queue();

        this.consoleChannel = this.guild.getTextChannelById(this.consoleID);

        if (this.consoleChannel == null) {
            this.logger.debug("(konsola) Nie można odnaleźć kanału z ID &b " + this.consoleID);
        }

        this.linkingManager = new LinkingManager(this.bdsAutoEnable, this);
        this.statsChannelsManager = new StatsChannelsManager(this.bdsAutoEnable, this);
        this.statsChannelsManager.init();

        for (final JDAListener listener : this.listeners) {
            try {
                listener.init();
                listener.initServerProcess(this.bdsAutoEnable.getServerProcess());
                this.jda.addEventListener(listener);
                this.logger.debug("Zarejestrowano listener JDA:&b " + listener.getClass().getSimpleName());
            } catch (final Exception exception) {
                this.logger.critical("Wystąpił błąd podczas ładowania listeneru: &b" + listener.getClass().getSimpleName(), exception);
                System.exit(0);
            }
        }
        this.checkBotPermissions();

        this.guild.updateCommands().addCommands(
                Commands.slash("list", "lista graczy online."),
                Commands.slash("backup", "Tworzenie bądź ostatni czas backupa")
                        .addOption(OptionType.STRING, "load", "Załaduj backup po jego pełnej nazwie", false),
                Commands.slash("difficulty", "Zmienia poziom trudności"),
                Commands.slash("version", "Wersja BDS-Auto-Enable i severa, umożliwia update servera"),
                Commands.slash("ping", "aktualny ping bot z serwerami discord"),
                Commands.slash("stats", "Statystyki Servera i aplikacji."),
                Commands.slash("cmd", "Wykonuje polecenie w konsoli.")
                        .addOption(OptionType.STRING, "command", "Polecenie które zostanie wysłane do konsoli.", true),
                Commands.slash("link", "Łączy konto discord z kontem nickiem Minecraft.")
                        .addOption(OptionType.STRING, "code", "Kod aby połączyć konta", false),
                Commands.slash("ip", "Informacje o ip ustawione w config"),
                Commands.slash("playtime", "Top 100 graczy z największą ilością przegranego czasu"),
                Commands.slash("deaths", "Top 100 graczy z największą ilością śmierci")
        ).queue();

        this.customStatusUpdate();
        this.leaveGuilds();
    }

    private void checkBotPermissions() {
        final Member botMember = this.guild.getMember(this.jda.getSelfUser());
        if (botMember == null) return;

        if (!botMember.hasPermission(Permission.ADMINISTRATOR)) {
            this.logger.error("Bot nie ma uprawnień administratora , są one wymagane");
            this.sendEmbedMessage("Brak uprawnień",
                    "**Bot nie posiada uprawnień administratora**\n" +
                            "Są one wymagane do `100%` pewności że wszytko bedzie działać w nim",
                    "Brak uprawnień");
        }
    }

    public void sendPrivateMessage(final User user, final String message) {
        if (!user.hasPrivateChannel()) return;
        user.openPrivateChannel()
                .queue(privateChannel -> privateChannel.sendMessage(message)
                        .queue());
    }

    public void sendPrivateMessage(final User user, final MessageEmbed embed) {
        if (!user.hasPrivateChannel()) return;
        user.openPrivateChannel()
                .queue(privateChannel -> privateChannel.sendMessageEmbeds(embed)
                        .queue());
    }

    public Role getHighestRole(final long memberID) {
        final Member member = this.guild.getMemberById(memberID);
        if (member == null) return null;
        Role highestRole = null;
        for (final Role role : member.getRoles()) {
            if (highestRole == null || role.getPosition() > highestRole.getPosition()) {
                highestRole = role;
            }
        }
        return highestRole;
    }

    public String getUserName(final Member member, final User author) {
        if (member != null && member.getNickname() != null) {
            return member.getNickname();
        }
        return author.getName();
    }

    public String getOwnerMention() {
        if (this.guild == null) return "";
        return (this.guild.getOwner() == null ? " " : "<@" + this.guild.getOwner().getIdLong() + ">");
    }

    private void customStatusUpdate() {
        final Timer timer = new Timer("Discord Status Changer", true);
        final TimerTask statusTask = new TimerTask() {

            @Override
            public void run() {
                DiscordJda.this.jda.getPresence().setActivity(DiscordJda.this.getCustomActivity());
            }
        };

        timer.scheduleAtFixedRate(statusTask, MathUtil.minutesTo(1, TimeUnit.MILLISECONDS), MathUtil.minutesTo(10, TimeUnit.MILLISECONDS));
    }

    private Activity getCustomActivity() {
        final String replacement = String.valueOf(MathUtil.millisTo((System.currentTimeMillis() - this.bdsAutoEnable.getServerProcess().getStartTime()), TimeUnit.MINUTES));
        final String activityMessage = this.discordConfig.getDiscordBotConfig().getActivityMessage().replaceAll("<time>", replacement);

        switch (Activity.ActivityType.valueOf(this.discordConfig.getDiscordBotConfig().getActivity().toUpperCase())) {
            case PLAYING -> {
                return Activity.playing(activityMessage.replaceAll("<time>", replacement));
            }
            case WATCHING -> {
                return Activity.watching(activityMessage.replaceAll("<time>", replacement));
            }
            case COMPETING -> {
                return Activity.competing(activityMessage.replaceAll("<time>", replacement));
            }
            case LISTENING -> {
                return Activity.listening(activityMessage.replaceAll("<time>", replacement));
            }
            case STREAMING -> {
                return Activity.streaming(activityMessage.replaceAll("<time>", replacement), this.discordConfig.getDiscordBotConfig().getStreamUrl());
            }
            default -> {
                this.logger.error("Wykryto nie wspierany status! ");
                return Activity.playing(activityMessage.replaceAll("<time>", replacement));
            }
        }
    }

    private void leaveGuilds() {
        if (!this.discordConfig.getDiscordBotConfig().isLeaveServers()) return;
        for (final Guild guild1 : this.jda.getGuilds()) {
            if (guild1 != this.guild) {
                final String inviteLink = guild1.getDefaultChannel().createInvite().complete().getUrl();
                guild1.leave().queue();

                this.sendMessage("Opuściłem serwer o ID: " + guild1.getId() +
                        "\n Nazwie: " + guild1.getName() +
                        "\n Zaproszenie: " + inviteLink

                );
            }
        }
    }

    private List<Member> getMembersOfGuild(final Guild guild) {
        return guild.getMembers().stream()
                .filter(member -> !member.getUser().isBot()).sorted(Comparator.comparing(Member::getTimeJoined)).toList();
    }

    @Override
    public void sendMessage(final String message) {
        if (this.jda != null && this.textChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            if (message.isEmpty()) return;
            this.textChannel.sendMessage(message.replaceAll("<owner>", this.getOwnerMention())).queue();
        }
    }

    @Override
    public void sendMessage(final String message, final Throwable throwable) {
        this.sendMessage(message +
                "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```");
    }

    @Override
    public void sendEmbedMessage(final String title, final String message, final String footer) {
        if (this.jda != null && this.textChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            if (title.isEmpty() || message.isEmpty() || footer.isEmpty()) return;
            final MessageEmbed embed = new EmbedBuilder()
                    .setTitle(title)
                    .setDescription(message.replaceAll("<owner>", this.getOwnerMention()))
                    .setColor(Color.BLUE)
                    .setFooter(footer)
                    .build();

            this.textChannel.sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public void sendEmbedMessage(final String title, final String message, final Throwable throwable, final String footer) {
        this.sendEmbedMessage(title, message +
                "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```", footer);
    }

    @Override
    public void writeConsole(final String message) {
        if (this.jda != null && this.consoleChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            if (message.isEmpty()) return;
            this.consoleService.execute(() -> this.consoleChannel.sendMessage(message.replaceAll("<owner>", this.getOwnerMention())).queue());
        }
    }

    @Override
    public void writeConsole(final String message, final Throwable throwable) {
        this.writeConsole(message + "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```");
    }

    @Override
    public void sendJoinMessage(final String playerName) {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendJoinMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getJoinMessage().replaceAll("<name>", playerName));
        }
    }

    @Override
    public void sendLeaveMessage(final String playerName) {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendLeaveMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getLeaveMessage().replaceAll("<name>", playerName));
        }
    }

    @Override
    public void sendPlayerMessage(final String playerName, final String playerMessage) {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendPlayerMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getMinecraftToDiscordMessage()
                    .replaceAll("<name>", playerName)
                    .replaceAll("<msg>", playerMessage)
                    .replaceAll("@", "\\@")
                    .replaceAll("@everyone", "/everyone/")
                    .replaceAll("@here", "/here/")
            );
        }
    }

    @Override
    public void sendDeathMessage(final String playerName, final String deathMessage) {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendDeathMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getDeathMessage()
                    .replaceAll("<name>", playerName)
                    .replaceAll("<casue>", deathMessage)
            );
        }
    }

    @Override
    public void sendDisabledMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendDisabledMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getDisabledMessage());
        }
    }

    @Override
    public void sendDisablingMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendDisablingMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getDisablingMessage());
        }
    }

    @Override
    public void sendProcessEnabledMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendProcessEnabledMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getProcessEnabledMessage());
        }
    }

    @Override
    public void sendEnabledMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendEnabledMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getEnabledMessage());
        }
    }

    @Override
    public void sendDestroyedMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendDestroyedMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getDestroyedMessage());
        }
    }

    @Override
    public void sendBackupDoneMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendBackupMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getBackupDoneMessage());
        }
    }

    @Override
    public void sendAppRamAlert() {
        if (this.appConfigManager.getWatchDogConfig().getRamMonitorConfig().isDiscordAlters()) {
            this.sendMessage(this.getOwnerMention() + this.discordConfig.getDiscordMessagesConfig().getAppRamAlter());
        }
    }

    @Override
    public void sendMachineRamAlert() {
        if (this.appConfigManager.getWatchDogConfig().getRamMonitorConfig().isDiscordAlters()) {
            this.sendMessage(this.getOwnerMention() + this.discordConfig.getDiscordMessagesConfig().getMachineRamAlter());
        }
    }

    @Override
    public void sendServerUpdateMessage(final String version) {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendServerUpdateMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getServerUpdate()
                    .replaceAll("<version>", version)
                    .replaceAll("<current>", this.appConfigManager.getVersionManagerConfig().getVersion())

            );
        }
    }

    @Override
    public void sendRestartMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendRestartMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getRestartMessage());
        }
    }

    @Override
    public void startShutdown(){
        if (this.linkingManager != null) this.linkingManager.saveLinedAccounts();
        if (this.statsChannelsManager != null) this.statsChannelsManager.onShutdown();
    }

    @Override
    public void shutdown() {
        if (this.jda != null) {
            if (this.jda.getStatus() == JDA.Status.CONNECTED) {
                try {
                    this.jda.shutdown();
                    if (!this.jda.awaitShutdown(10L, TimeUnit.SECONDS)) {
                        this.logger.info("Wyłączono bota");
                    }
                } catch (final Exception exception) {
                    this.logger.critical("Nie można wyłączyć bota", exception);
                }
            }
        }
    }

    public JDA getJda() {
        return this.jda;
    }

    public long getServerID() {
        return this.serverID;
    }

    public long getChannelID() {
        return this.channelID;
    }

    public long getConsoleID() {
        return this.consoleID;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public TextChannel getTextChannel() {
        return this.textChannel;
    }

    public TextChannel getConsoleChannel() {
        return this.consoleChannel;
    }

    public StatsChannelsManager getStatsChannelsManager() {
        return this.statsChannelsManager;
    }

    public LinkingManager getLinkingManager() {
        return this.linkingManager;
    }
}