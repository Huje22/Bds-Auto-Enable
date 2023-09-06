package me.indian.bds.discord.jda;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.jda.listener.CommandListener;
import me.indian.bds.discord.jda.listener.MessageListener;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.PackModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscordJda extends ListenerAdapter implements DiscordIntegration {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Config config;
    private final long serverID, channelID, consoleID, logID;
    private final CommandListener commandListener;
    private final MessageListener messageListener;
    private final ExecutorService consoleService;
    private JDA jda;
    private Guild guild;
    private TextChannel textChannel, consoleChannel, logChannel;


    public DiscordJda(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.serverID = this.config.getDiscordBotConfig().getServerID();
        this.channelID = this.config.getDiscordBotConfig().getChannelID();
        this.consoleID = this.config.getDiscordBotConfig().getConsoleID();
        this.logID = this.config.getDiscordBotConfig().getLogID();
        this.commandListener = new CommandListener(this, this.bdsAutoEnable);
        this.messageListener = new MessageListener(this, this.bdsAutoEnable);
        this.consoleService = Executors.newSingleThreadExecutor(new ThreadUtil("Discord-Console"));
    }

    public void initServerProcess(final ServerProcess serverProcess) {
        this.commandListener.initServerProcess(serverProcess);
        this.messageListener.initServerProcess(serverProcess);
    }

    @Override
    public void init() {
        final PackModule packModule = this.bdsAutoEnable.getWatchDog().getPackModule();
        this.logger.info("&aŁadowanie bota....");
        if (this.config.getDiscordBotConfig().getToken().isEmpty()) {
            this.logger.alert("&aNie znaleziono tokenu , pomijanie ładowania.");
            return;
        }
        if (!packModule.isLoaded()) {
            this.logger.error("&cNie załadowano paczki&b " + packModule.getPackName() + "&4.&cBot nie może bez niej normalnie działać");
            this.logger.error("Możesz znaleźć ją tu:&b https://github.com/Huje22/BDS-Auto-Enable-Managment-Pack");
            return;
        }

        try {
            this.jda = JDABuilder.create(this.config.getDiscordBotConfig().getToken(), GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.MESSAGE_CONTENT)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                    .enableCache(CacheFlag.EMOJI)
                    .setActivity(this.getCustomActivity())
                    .setEnableShutdownHook(false)
                    .build();
            this.jda.awaitReady();
            this.logger.info("&aZaładowano bota");
        } catch (final Exception exception) {
            this.logger.critical("&aNie można uruchomić bota , sprawdź podany &bTOKEN");
            exception.printStackTrace();
            return;
        }

        try {
            this.guild = this.jda.getGuildById(this.serverID);
        } catch (final Exception exception) {
            this.logger.info("Nie można odnaleźć servera z ID &a" + this.serverID);
            this.jda.shutdown();
            this.jda = null;
            return;
        }

        try {
            this.textChannel = this.guild.getTextChannelById(this.channelID);
        } catch (final Exception exception) {
            this.logger.info("Nie można odnaleźć kanału z ID &a" + this.channelID);
            this.jda.shutdown();
            this.jda = null;
            return;
        }

        try {
            this.consoleChannel = this.guild.getTextChannelById(this.consoleID);
        } catch (final Exception exception) {
            this.logger.info("(konsola) Nie można odnaleźć kanału z ID &a" + this.consoleID);
        }

        try {
            this.logChannel = this.guild.getTextChannelById(this.logID);
        } catch (final Exception exception) {
            this.logger.info("(log) Nie można odnaleźć kanału z ID &a" + this.logID);
        }

        this.commandListener.init();
        this.messageListener.init();
        this.jda.addEventListener(this.commandListener);
        this.jda.addEventListener(this.messageListener);

        this.guild.updateCommands().addCommands(
                Commands.slash("list", "lista graczy online."),
                Commands.slash("backup", "Tworzenie bądź ostatni czas backupa")
                        .addOption(OptionType.STRING, "load", "Załaduj backup po jego pełnej nazwie", false),
                Commands.slash("difficulty", "Zmienia poziom trudności"),
                Commands.slash("ping", "aktualny ping bot z serwerami discord"),
                Commands.slash("stats", "Statystyki Servera i aplikacji."),
                Commands.slash("cmd", "Wykonuje polecenie w konsoli.")
                        .addOption(OptionType.STRING, "command", "Polecenie które zostanie wysłane do konsoli.", true),
                Commands.slash("ip", "Informacje o ip ustawione w config"),
                Commands.slash("playtime", "Top 20 graczy z największą ilością przegranego czasu"),
                Commands.slash("deaths", "Top 20 graczy z największą ilością śmierci")
        ).queue();

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

    public String getOwnerMention() {
        if (this.guild == null) return "";
        return (this.guild.getOwner() == null ? " " : "<@" + this.guild.getOwner().getIdLong() + ">");
    }

    private Activity getCustomActivity() {
        final String activityMessage = this.config.getDiscordBotConfig().getActivityMessage();
        switch (Activity.ActivityType.valueOf(this.config.getDiscordBotConfig().getActivity().toUpperCase())) {
            case PLAYING -> {
                return Activity.playing(activityMessage);
            }
            case WATCHING -> {
                return Activity.watching(activityMessage);
            }
            case COMPETING -> {
                return Activity.competing(activityMessage);
            }
            case LISTENING -> {
                return Activity.listening(activityMessage);
            }
            case STREAMING -> {
                return Activity.streaming(activityMessage, this.config.getDiscordBotConfig().getStreamUrl());
            }
            default -> {
                this.logger.error("Wykryto nie wspierany status! ");
                return Activity.playing(activityMessage);
            }
        }
    }

    public void logCommand(final MessageEmbed embed) {
        if (this.jda != null && this.logChannel != null) {
            this.consoleService.execute(() -> this.logChannel.sendMessageEmbeds(embed).queue());
        }
    }

    @Override
    public void sendMessage(final String message) {
        if (this.jda != null && this.textChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            this.textChannel.sendMessage(message.replaceAll("<owner>", this.getOwnerMention())).queue();
        }
    }

    @Override
    public void sendEmbedMessage(final String title, final String message, final String footer) {
        if (this.jda != null && this.textChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
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
    public void writeConsole(final String message) {
        if (this.jda != null && this.consoleChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            this.consoleService.execute(() -> this.consoleChannel.sendMessage(message.replaceAll("<owner>", this.getOwnerMention())).queue());
        }
    }

    @Override
    public void sendJoinMessage(final String playerName) {
        this.sendMessage(this.config.getDiscordMessagesConfig().getJoinMessage().replaceAll("<name>", playerName));
    }

    @Override
    public void sendLeaveMessage(final String playerName) {
        this.sendMessage(this.config.getDiscordMessagesConfig().getLeaveMessage().replaceAll("<name>", playerName));
    }

    @Override
    public void sendPlayerMessage(final String playerName, final String playerMessage) {
        this.sendMessage(this.config.getDiscordMessagesConfig().getMinecraftToDiscordMessage()
                .replaceAll("<name>", playerName)
                .replaceAll("<msg>", playerMessage)
                .replaceAll("@everyone", "/everyone/")
                .replaceAll("@here", "/here/")
        );
    }

    @Override
    public void sendDeathMessage(final String playerName, final String deathMessage) {
        this.sendMessage(this.config.getDiscordMessagesConfig().getDeathMessage()
                .replaceAll("<name>", playerName)
                .replaceAll("<casue>", deathMessage)
        );
    }

    @Override
    public void sendDisabledMessage() {
        this.sendMessage(this.config.getDiscordMessagesConfig().getDisabledMessage());
    }

    @Override
    public void sendDisablingMessage() {
        this.sendMessage(this.config.getDiscordMessagesConfig().getDisablingMessage());
    }

    @Override
    public void sendProcessEnabledMessage() {
        this.sendMessage(this.config.getDiscordMessagesConfig().getProcessEnabledMessage());
    }

    @Override
    public void sendEnabledMessage() {
        this.sendMessage(this.config.getDiscordMessagesConfig().getEnabledMessage());
    }

    @Override
    public void sendDestroyedMessage() {
        this.sendMessage(this.config.getDiscordMessagesConfig().getDestroyedMessage());
    }

    @Override
    public void sendAppRamAlert() {
        this.sendMessage(this.getOwnerMention() + this.config.getDiscordMessagesConfig().getAppRamAlter());
    }

    @Override
    public void sendMachineRamAlert() {
        this.sendMessage(this.getOwnerMention() + this.config.getDiscordMessagesConfig().getMachineRamAlter());
    }

    @Override
    public void disableBot() {
        if (this.jda != null) {
            if (this.jda.getStatus() == JDA.Status.CONNECTED) {
                this.jda.shutdown();
            }
        }
    }

    public JDA getJda() {
        return this.jda;
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

    public long getServerID() {
        return this.serverID;
    }

    public long getChannelID() {
        return this.channelID;
    }

    public long getConsoleID() {
        return this.consoleID;
    }
}
