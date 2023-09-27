package me.indian.bds.discord.jda;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.jda.listener.CommandListener;
import me.indian.bds.discord.jda.listener.JDAListener;
import me.indian.bds.discord.jda.listener.MessageListener;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MessageUtil;
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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DiscordJda implements DiscordIntegration {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Config config;
    private final DiscordConfig discordConfig;
    private final long serverID, channelID, consoleID, logID;
    private final List<JDAListener> listeners;
    private final ExecutorService consoleService;
    private JDA jda;
    private Guild guild;
    private TextChannel textChannel, consoleChannel, logChannel;

    public DiscordJda(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.discordConfig = this.config.getDiscordConfig();
        this.serverID = this.discordConfig.getDiscordBotConfig().getServerID();
        this.channelID = this.discordConfig.getDiscordBotConfig().getChannelID();
        this.consoleID = this.discordConfig.getDiscordBotConfig().getConsoleID();
        this.logID = this.discordConfig.getDiscordBotConfig().getLogID();
        this.listeners = new ArrayList<>();
        this.listeners.add(new CommandListener(this, this.bdsAutoEnable));
        this.listeners.add(new MessageListener(this, this.bdsAutoEnable));

        this.consoleService = Executors.newSingleThreadExecutor(new ThreadUtil("Discord-Console"));
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
                    .setActivity(this.getCustomActivity())
                    .setEnableShutdownHook(false)
                    .build();
            this.jda.awaitReady();
            this.logger.info("&aZaładowano bota");
        } catch (final Exception exception) {
            this.logger.critical("&aNie można uruchomić bota , sprawdź podany &bTOKEN", exception);
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

        for (final JDAListener listener : this.listeners) {
            try {
                listener.init();
                listener.initServerProcess(this.bdsAutoEnable.getServerProcess());
                this.jda.addEventListener(listener);
                this.logger.debug("Zarejestrowano listener JDA:&b " + listener.getClass().getSimpleName());
            } catch (final Exception exception) {
                this.logger.critical("Wystąpił błąd podczas ładowania listenera: &b" + listener.getClass().getSimpleName(), exception);
                System.exit(0);
            }
        }

        this.guild.updateCommands().addCommands(
                Commands.slash("list", "lista graczy online."),
                Commands.slash("backup", "Tworzenie bądź ostatni czas backupa")
                        .addOption(OptionType.STRING, "load", "Załaduj backup po jego pełnej nazwie", false),
                Commands.slash("difficulty", "Zmienia poziom trudności"),
                Commands.slash("version", "Wersja BDS-Auto-Enable i severa"),
                Commands.slash("ping", "aktualny ping bot z serwerami discord"),
                Commands.slash("stats", "Statystyki Servera i aplikacji."),
                Commands.slash("cmd", "Wykonuje polecenie w konsoli.")
                        .addOption(OptionType.STRING, "command", "Polecenie które zostanie wysłane do konsoli.", true),
                Commands.slash("ip", "Informacje o ip ustawione w config"),
                Commands.slash("playtime", "Top 20 graczy z największą ilością przegranego czasu"),
                Commands.slash("deaths", "Top 20 graczy z największą ilością śmierci")
        ).queue();

        this.leaveGuilds();
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
        final String activityMessage = this.discordConfig.getDiscordBotConfig().getActivityMessage();
        switch (Activity.ActivityType.valueOf(this.discordConfig.getDiscordBotConfig().getActivity().toUpperCase())) {
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
                return Activity.streaming(activityMessage, this.discordConfig.getDiscordBotConfig().getStreamUrl());
            }
            default -> {
                this.logger.error("Wykryto nie wspierany status! ");
                return Activity.playing(activityMessage);
            }
        }
    }

    public void logCommand(final MessageEmbed embed) {
        if (this.jda != null && this.logChannel != null) {
            if(embed.isEmpty()) return;
            this.consoleService.execute(() -> this.logChannel.sendMessageEmbeds(embed).queue());
        }
    }
    
    private void leaveGuilds(){
        //TODO: Complet it
        if(!this.discordConfig.isLeaveServers()) return 
        for (final Guild guild1 : this.jda.getGuilds()) {
            if (guild1 != guild) {
                final String inviteLink = guild1.getDefaultChannel().createInvite().complete().getUrl();
                guild1.leave().queue();

                    
                 this.sendMessage("Opuściłem serwer o ID: " + guild1.getId() + 
               "/n Nazwie: " + guild1.getName() +
               "/n Zaproszenie: " + inviteLink
               
               );
                 }
        }
      }

    private void getMembersOfGuild(final Guild guild){
        //TODO: Complet it
        final List<Member> members = guild.getMembers().stream()
                    .filter(member -> !member.getUser().isBot())
                    .collect(Collectors.toList());

            members.sort(Comparator.comparing(Member::getTimeJoined));

            for (final Member member : members) {
                final User user = member.getUser();
           System.out.println(user.getName() + "#" + user.getDiscriminator());
            
             }
    }

    @Override
    public void sendMessage(final String message) {
        if (this.jda != null && this.textChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            if(message.isEmpty()) return;
            this.textChannel.sendMessage(message.replaceAll("<owner>", this.getOwnerMention())).queue();
        }
    }
    
    @Override
    public void sendMessage(final String message , final Throwable throwable) {
        this.sendMessage(message + 
           "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```");
    }	
    
    
    @Override
    public void sendEmbedMessage(final String title, final String message, final String footer) {
        if (this.jda != null && this.textChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            if(title.isEmpty() || message.isEmpty() || footer.isEmpty()) return;
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
        this.sendEmbedMessage(title , message + 
                    "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```" , footer);
    }	

    @Override
    public void writeConsole(final String message) {
        if (this.jda != null && this.consoleChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            if(message.isEmpty()) return;
            this.consoleService.execute(() -> this.consoleChannel.sendMessage(message.replaceAll("<owner>", this.getOwnerMention())).queue());
        }
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
        if (this.config.getWatchDogConfig().getRamMonitor().isDiscordAlters()) {
            this.sendMessage(this.getOwnerMention() + this.discordConfig.getDiscordMessagesConfig().getAppRamAlter());
        }
    }

    @Override
    public void sendMachineRamAlert() {
        if (this.config.getWatchDogConfig().getRamMonitor().isDiscordAlters()) {
            this.sendMessage(this.getOwnerMention() + this.discordConfig.getDiscordMessagesConfig().getMachineRamAlter());
        }
    }

    @Override
    public void disableBot() {
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
