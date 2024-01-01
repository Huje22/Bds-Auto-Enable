package me.indian.bds.discord.jda.listener;

import java.util.List;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.config.sub.discord.LinkingConfig;
import me.indian.bds.discord.jda.DiscordJDA;
import me.indian.bds.discord.jda.manager.LinkingManager;
import me.indian.bds.logger.ConsoleColors;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.manager.ServerManager;
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

public class MessageListener extends ListenerAdapter implements JDAListener {

    private final DiscordJDA DiscordJDA;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final DiscordConfig discordConfig;
    private TextChannel textChannel;
    private TextChannel consoleChannel;
    private ServerProcess serverProcess;

    public MessageListener(final DiscordJDA DiscordJDA, final BDSAutoEnable bdsAutoEnable) {
        this.DiscordJDA = DiscordJDA;
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.discordConfig = this.bdsAutoEnable.getAppConfigManager().getDiscordConfig();
    }

    @Override
    public void init() {
        this.textChannel = this.DiscordJDA.getTextChannel();
        this.consoleChannel = this.DiscordJDA.getConsoleChannel();
    }

    @Override
    public void initServerProcess(final ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @Override
    public void onMessageUpdate(final MessageUpdateEvent event) {
        if (event.getAuthor().equals(this.DiscordJDA.getJda().getSelfUser())) return;

        final Member member = event.getMember();
        final User author = event.getAuthor();
        final Message message = event.getMessage();

        if (event.getChannel().asTextChannel() == this.textChannel) this.sendMessage(member, author, message, true);
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().equals(this.DiscordJDA.getJda().getSelfUser())) return;

        final Member member = event.getMember();
        final User author = event.getAuthor();
        final Message message = event.getMessage();
        final String rawMessage = message.getContentRaw();
        final ServerManager serverManager = this.bdsAutoEnable.getServerManager();
        final LinkingConfig linkingConfig = this.discordConfig.getBotConfig().getLinkingConfig();

        if (member == null) return;
        final long id = member.getIdLong();

        if (event.getChannel().asTextChannel() == this.consoleChannel) {
            if(!this.serverProcess.isEnabled()) return;
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
            final LinkingManager linkingManager = this.DiscordJDA.getLinkingManager();
            if (linkingManager != null) {
                if (!linkingConfig.isCanType()) {
                    if (!linkingManager.isLinked(id) && !author.isBot()) {
                        this.DiscordJDA.mute(member, 1, TimeUnit.MINUTES);
                        message.delete().queue();
                        this.DiscordJDA.sendPrivateMessage(author, linkingConfig.getCantTypeMessage());
                        return;
                    }
                }

                if (serverManager.isMuted(linkingManager.getNameByID(id))) {
                    this.DiscordJDA.mute(member, 1, TimeUnit.MINUTES);
                    message.delete().queue();
                    this.DiscordJDA.sendPrivateMessage(author, "Jesteś wyciszony!");
                    return;
                }
            }

            this.sendMessage(member, author, message, false);
        }
    }

    private void sendMessage(final Member member, final User author, final Message message, final boolean edited) {
        if (!this.serverProcess.isEnabled() || this.isMaxLength(message)) return;

        final Role role = this.DiscordJDA.getHighestRole(author.getIdLong());
        String msg = this.discordConfig.getDiscordMessagesConfig().getDiscordToMinecraftMessage()
                .replaceAll("<name>", this.DiscordJDA.getUserName(member, author))
                .replaceAll("<msg>", this.generateRawMessage(message))
                .replaceAll("<reply>", this.generatorReply(message.getReferencedMessage()))
                .replaceAll("<role>", this.DiscordJDA.getColoredRole(role));

        if (edited) {
            msg += this.discordConfig.getDiscordMessagesConfig().getEdited();
        }
        if (message.isWebhookMessage()) {
            msg += this.discordConfig.getDiscordMessagesConfig().getWebhook();
        }

        this.serverProcess.tellrawToAll(msg);
        this.logger.info(msg);
        this.DiscordJDA.writeConsole(ConsoleColors.removeColors(msg));
    }

    private boolean isMaxLength(final Message message) {
        if (!this.discordConfig.getBotConfig().isDeleteOnReachLimit()) return false;

        if (message.getContentRaw().length() >= this.discordConfig.getBotConfig().getAllowedLength()) {
            this.DiscordJDA.sendPrivateMessage(message.getAuthor(), this.discordConfig.getBotConfig().getReachedMessage());
            message.delete().queue();
            this.DiscordJDA.sendPrivateMessage(message.getAuthor(), "`" + message.getContentRaw() + "`");
            return true;
        }
        return false;
    }

    private String generateRawMessage(final Message message) {
        final List<Member> members = message.getMentions().getMembers();
        String rawMessage = MessageUtil.fixMessage(message.getContentRaw());

        if (!message.getAttachments().isEmpty()) rawMessage += this.discordConfig.getDiscordMessagesConfig().getAttachment();
        if (members.isEmpty()) {
            for (final User user : message.getMentions().getUsers()) {
                if (user != null)
                    rawMessage = rawMessage.replaceAll("<@" + user.getIdLong() + ">", "@" + this.DiscordJDA.getUserName(null, user));
            }
        } else {
            for (final Member member : members) {
                if (member != null)
                    rawMessage = rawMessage.replaceAll("<@" + member.getIdLong() + ">", "@" + this.DiscordJDA.getUserName(member, member.getUser()));
            }
        }

        for (final GuildChannel guildChannel : message.getMentions().getChannels()) {
            if (guildChannel != null) rawMessage = rawMessage.replaceAll("<#" + guildChannel.getIdLong() + ">", "#" + guildChannel.getName());
        }

        for (final Role role : message.getMentions().getRoles()) {
            if (role != null)
                rawMessage = rawMessage.replaceAll("<@&" + role.getIdLong() + ">", this.DiscordJDA.getColoredRole(role) + "&r");
        }

        //Daje to aby określić czy wiadomość nadal jest pusta
        if (rawMessage.isEmpty()) rawMessage += message.getJumpUrl();

        return rawMessage;
    }

    private String generatorReply(final Message messageReference) {
        if (messageReference == null) return "";

        final Member member = messageReference.getMember();
        final User author = messageReference.getAuthor();

        final String replyStatement = this.discordConfig.getDiscordMessagesConfig().getReplyStatement()
                .replaceAll("<msg>", this.generateRawMessage(messageReference).replaceAll("\\*\\*", ""))
                .replaceAll("<author>", this.DiscordJDA.getUserName(member, author));

        if (author.equals(this.DiscordJDA.getJda().getSelfUser()))
            return this.discordConfig.getDiscordMessagesConfig().getBotReplyStatement()
                    .replaceAll("<msg>", this.generateRawMessage(messageReference)
                            .replaceAll("\\*\\*", ""));


        return replyStatement;
    }
}
