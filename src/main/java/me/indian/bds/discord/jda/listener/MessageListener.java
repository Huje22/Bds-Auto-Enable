package me.indian.bds.discord.jda.listener;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.config.sub.discord.LinkingConfig;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.discord.jda.manager.LinkingManager;
import me.indian.bds.logger.ConsoleColors;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MessageUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter implements JDAListener {

    private final DiscordJda discordJda;
    private final Logger logger;
    private final DiscordConfig discordConfig;
    private TextChannel textChannel;
    private TextChannel consoleChannel;
    private ServerProcess serverProcess;

    public MessageListener(final DiscordJda discordJda, final BDSAutoEnable bdsAutoEnable) {
        this.discordJda = discordJda;
        this.logger = bdsAutoEnable.getLogger();
        this.discordConfig = bdsAutoEnable.getAppConfigManager().getDiscordConfig();
    }

    @Override
    public void init() {
        this.textChannel = this.discordJda.getTextChannel();
        this.consoleChannel = this.discordJda.getConsoleChannel();
    }

    @Override
    public void initServerProcess(final ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @Override
    public void onMessageUpdate(final MessageUpdateEvent event) {
        if (event.getAuthor().equals(this.discordJda.getJda().getSelfUser())) return;

        final Member member = event.getMember();
        final User author = event.getAuthor();
        final Message message = event.getMessage();

        if (event.getChannel().asTextChannel() == this.textChannel) {
            this.sendMessage(member, author, message, true);
        }
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().equals(this.discordJda.getJda().getSelfUser())) return;

        final Member member = event.getMember();
        final User author = event.getAuthor();
        final Message message = event.getMessage();
        final String rawMessage = message.getContentRaw();
        final LinkingConfig linkingConfig = this.discordConfig.getBotConfig().getLinkingConfig();

        if (member == null) return;

        if (event.getChannel().asTextChannel() == this.consoleChannel) {
            if (member.hasPermission(Permission.ADMINISTRATOR)) {
                this.serverProcess.sendToConsole(rawMessage);
                this.logger.print("[" + DateUtil.getDate() + " DISCORD] " +
                        author.getName() +
                        " (" + author.getIdLong() + ") -> " +
                        rawMessage);
            } else {
                event.getChannel().sendMessage("Nie masz uprawnień administratora aby wysłać tu wiadomość").queue(msg -> {
                    msg.delete().queueAfter(5, TimeUnit.SECONDS);
                    message.delete().queueAfter(4, TimeUnit.SECONDS);
                });
            }
            return;
        }

        if (event.getChannel().asTextChannel() == this.textChannel) {
            if (!linkingConfig.isCanType()) {
                final LinkingManager linkingManager = this.discordJda.getLinkingManager();
                if (!linkingManager.isLinked(member.getIdLong()) && !author.isBot()) {
                    this.discordJda.sendPrivateMessage(author, linkingConfig.getCantTypeMessage());
                    message.delete().queue();
                    return;
                }
            }

            this.sendMessage(member, author, message, false);
        }
    }

    private void sendMessage(final Member member, final User author, final Message message, final boolean edited) {
        final Role role = this.discordJda.getHighestRole(author.getIdLong());
        if (this.isMaxLength(message)) return;

        String msg = this.discordConfig.getDiscordMessagesConfig().getDiscordToMinecraftMessage()
                .replaceAll("<name>", this.discordJda.getUserName(member, author))
                .replaceAll("<msg>", this.generateRawMessage(message))
                .replaceAll("<reply>", this.generatorReply(message.getReferencedMessage()))
                .replaceAll("<role>", this.getColoredRole(role));
        if (edited) {
            msg += this.discordConfig.getDiscordMessagesConfig().getEdited();
        }
        if (message.isWebhookMessage()) {
            msg += this.discordConfig.getDiscordMessagesConfig().getWebhook();
        }

        this.serverProcess.tellrawToAll(msg);
        this.logger.info(msg);
        this.discordJda.writeConsole(ConsoleColors.removeColors(msg));
    }

    private String getRoleColor(final Role role) {
        if (role == null) return "";
        final Color col = role.getColor();
        return ConsoleColors.getMinecraftColorFromRGB(col.getRed(), col.getGreen(), col.getBlue());
    }

    private String getColoredRole(final Role role) {
        return role == null ? "" : (this.getRoleColor(role) + "@" + role.getName() + "&r");
    }

    private boolean isMaxLength(final Message message) {
        if (!this.discordConfig.getBotConfig().isDeleteOnReachLimit()) return false;

        if (message.getContentRaw().length() >= this.discordConfig.getBotConfig().getAllowedLength()) {
            this.discordJda.sendPrivateMessage(message.getAuthor(), this.discordConfig.getBotConfig().getReachedMessage());
            message.delete().queue();
            this.discordJda.sendPrivateMessage(message.getAuthor(), "`" + message.getContentRaw() + "`");
            return true;
        }
        return false;
    }

    private String generateRawMessage(final Message message) {
        String rawMessage = MessageUtil.fixMessage(message.getContentRaw());

        if (!message.getAttachments().isEmpty()) {
            rawMessage += this.discordConfig.getDiscordMessagesConfig().getAttachment();
        }

        for (final User user : message.getMentions().getUsers()) {
            if (user == null) continue;
            final long id = user.getIdLong();
            rawMessage = rawMessage.replaceAll("<@" + id + ">", "@" + this.discordJda.getUserName(null, user));
        }

        for (final GuildChannel guildChannel : message.getMentions().getChannels()) {
            if (guildChannel == null) continue;
            final long id = guildChannel.getIdLong();
            rawMessage = rawMessage.replaceAll("<#" + id + ">", "#" + guildChannel.getName());
        }

        for (final Role role : message.getMentions().getRoles()) {
            if (role == null) continue;
            final long id = role.getIdLong();
            rawMessage = rawMessage.replaceAll("<@&" + id + ">",
                    this.getColoredRole(role) + "&r");
        }

        //Daje to ostatnie aby okreslic czy wiadomosc nadal jest pusta
        if (rawMessage.isEmpty()) {
            rawMessage += message.getJumpUrl();
        }

        return rawMessage;
    }

    private String generatorReply(final Message messageReference) {
        if (messageReference == null) return "";

        final String replyStatement = this.discordConfig.getDiscordMessagesConfig().getReplyStatement()
                .replaceAll("<msg>", this.generateRawMessage(messageReference).replaceAll("\\*\\*", ""))
                .replaceAll("<author>", this.discordJda.getUserName(messageReference.getMember(), messageReference.getAuthor()));

        if (messageReference.getAuthor().equals(this.discordJda.getJda().getSelfUser())) {
            return this.discordConfig.getDiscordMessagesConfig().getBotReplyStatement()
                    .replaceAll("<msg>", this.generateRawMessage(messageReference).replaceAll("\\*\\*", ""));
        }

        return replyStatement;
    }
}