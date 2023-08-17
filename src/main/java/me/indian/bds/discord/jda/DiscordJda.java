package me.indian.bds.discord.jda;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.jda.listener.MessageReceived;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class DiscordJda extends ListenerAdapter implements DiscordIntegration {


    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Config config;
    private final long serverID;
    private final long channelID;
    private final long consoleID;
    private final MessageReceived messageReceived;
    private JDA jda;
    private Guild guild;
    private TextChannel textChannel;
    private TextChannel consoleChannel;


    public DiscordJda(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.serverID = this.config.getDiscordBot().getServerID();
        this.channelID = this.config.getDiscordBot().getChannelID();
        this.consoleID = this.config.getDiscordBot().getConsoleID();
        this.messageReceived = new MessageReceived(this, this.bdsAutoEnable);
    }

    public void initServerProcess(final ServerProcess serverProcess) {
        this.messageReceived.initServerProcess(serverProcess);
    }

    @Override
    public void init() {
        this.logger.info("&aŁadowanie bota....");
        if (this.config.getDiscordBot().getToken().isEmpty()) {
            this.logger.alert("&aNie znaleziono tokenu , pomijanie ładowania.");
            return;
        }
        try {
            this.jda = JDABuilder.create(this.config.getDiscordBot().getToken(), GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.MESSAGE_CONTENT)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS).enableCache(CacheFlag.EMOJI)
                    .setActivity(Activity.playing("Minecraft"))
                    .setEnableShutdownHook(false)
                    .build();
            this.jda.awaitReady();

            this.logger.info("&aZaładowano bota");
        } catch (final Exception exception) {
            this.logger.critical("&aNie można uruchomic bota , sprawdź podany &bTOKEN");
            exception.printStackTrace();
            return;
        }

        try {
            this.guild = this.jda.getGuildById(this.serverID);
        } catch (final Exception exception) {
            this.logger.info("Nie można odnaleźc servera z ID &a" + this.serverID);
            this.jda.shutdown();
            return;
        }

        try {
            this.textChannel = this.guild.getTextChannelById(this.channelID);
        } catch (final Exception exception) {
            this.logger.info("Nie można odnaleźc kanału z ID &a" + this.channelID);
            this.jda.shutdown();
            return;
        }
        try {
            this.consoleChannel = this.guild.getTextChannelById(this.consoleID);
        } catch (final Exception exception) {
            this.logger.info("(konola) Nie można odnaleźc kanału z ID &a" + this.consoleID);
        }
        this.messageReceived.initChannels();
        this.jda.addEventListener(this.messageReceived);

    }

    private void sendMessage(final String message) {
        if (this.jda != null) {
            this.textChannel.sendMessage(message).queue();
        }
    }

    @Override
    public void writeConsole(final String message) {
        if (this.jda != null && this.consoleChannel != null) {
            this.consoleChannel.sendMessage(message).queue();
        }
    }

    @Override
    public void sendJoinMessage(final String playerName) {
        this.sendMessage(this.config.getMessages().getJoinMessage().replaceAll("<name>", playerName));
    }

    @Override
    public void sendLeaveMessage(final String playerName) {
        this.sendMessage(this.config.getMessages().getLeaveMessage().replaceAll("<name>", playerName));
    }

    @Override
    public void sendPlayerMessage(final String playerName, final String playerMessage) {
        this.sendMessage(this.config.getMessages().getMinecraftToDiscordMessage()
                .replaceAll("<name>", playerName)
                .replaceAll("<msg>", playerMessage)
                .replaceAll("@everyone", "/everyone/")
                .replaceAll("@here", "/here/")
        );
    }

    @Override
    public void sendDeathMessage(final String playerName, final String deathMessage) {
        this.sendMessage(this.config.getMessages().getDeathMessage()
                .replaceAll("<name>", playerName)
                .replaceAll("<casue>", deathMessage)
                .replaceAll("@everyone", "/everyone/")
                .replaceAll("@here", "/here/")
        );
    }

    @Override
    public void sendDisabledMessage() {
        this.sendMessage(this.config.getMessages().getDisabledMessage());
    }

    @Override
    public void sendDisablingMessage() {
        this.sendMessage(this.config.getMessages().getDisablingMessage());
    }

    @Override
    public void sendStopMessage() {
        this.sendMessage(this.config.getMessages().getDisablingMessage());
    }

    @Override
    public void sendEnabledMessage() {
        this.sendMessage(this.config.getMessages().getEnabledMessage());
    }

    @Override
    public void sendDestroyedMessage() {
        this.sendMessage(this.config.getMessages().getDestroyedMessage());
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
