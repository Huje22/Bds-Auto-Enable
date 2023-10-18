package me.indian.bds.discord.jda.listener;

import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.logger.ConsoleColors;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
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

    private final DiscordJda discordJda;
    private final Logger logger;
    private final DiscordConfig discordConfig;
    private TextChannel textChannel;
    private TextChannel consoleChannel;
    private ServerProcess serverProcess;

    public MessageListener(final DiscordJda discordJda, final BDSAutoEnable bdsAutoEnable) {
        this.discordJda = discordJda;
        this.logger = bdsAutoEnable.getLogger();
        this.discordConfig = bdsAutoEnable.getConfig().getDiscordConfig();
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
        if (event.getAuthor().isBot()) return;

        final Member member = event.getMember();
        final User author = event.getAuthor();
        final Message message = event.getMessage();

        if (event.getChannel().asTextChannel() == this.textChannel) {
            this.sendMessage(member, author, message, true);
        }
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;

        final Member member = event.getMember();
        final User author = event.getAuthor();
        final Message message = event.getMessage();
        final String rawMessage = message.getContentRaw();

        if (event.getChannel().asTextChannel() == this.consoleChannel) {
            if (member == null) return;
            if (member.hasPermission(Permission.ADMINISTRATOR)) {
                this.serverProcess.sendToConsole(rawMessage);
                this.logger.print(rawMessage);
            } else {
                event.getChannel().sendMessage("Nie masz uprawnień administratora aby wysłać tu wiadomość").queue(msg -> {
                    msg.delete().queueAfter(5, TimeUnit.SECONDS);
                    message.delete().queueAfter(4, TimeUnit.SECONDS);
                });
            }
            return;
        }
        if (event.getChannel().asTextChannel() == this.textChannel) {
            this.sendMessage(member, author, message, false);
        }
    }

    private void sendMessage(final Member member, final User author, final Message message, final boolean edited) {
        final Role role = this.discordJda.getHighestRole(author.getIdLong());
        if (this.checkLength(message)) return;

        String msg = this.discordConfig.getDiscordMessagesConfig().getDiscordToMinecraftMessage()
                .replaceAll("<name>", this.getUserName(member, author))
                .replaceAll("<msg>", this.generateRawMessage(message))
                .replaceAll("<reply>", this.generatorReply(message.getReferencedMessage()))
                .replaceAll("<role>", role == null ? "" : role.getName());
        if (edited) {
            msg += this.discordConfig.getDiscordMessagesConfig().getEdited();
        }

        this.serverProcess.tellrawToAll(msg);
        this.logger.info(msg);
        this.discordJda.writeConsole(ConsoleColors.removeColors(msg));
    }

    private String getUserName(final Member member, final User author) {
        if (member != null) {
            if (member.getNickname() != null) {
                return member.getNickname();
            }
        }
        return author.getName();
    }

    private boolean checkLength(final Message message) {
        if (message.getContentRaw().length() >= this.discordConfig.getDiscordBotConfig().getAllowedLength()) {
            this.discordJda.sendPrivateMessage(message.getAuthor(), this.discordConfig.getDiscordBotConfig().getReachedMessage());
            if (this.discordConfig.getDiscordBotConfig().isDeleteOnReachLimit()) {
                message.delete().queue();
                this.discordJda.sendPrivateMessage(message.getAuthor(), "`" + message.getContentRaw() + "`");
            }
            return true;
        }
        return false;
    }

    private String generateRawMessage(final Message message){
        String rawMessage = MessageUtil.fixMessage(message.getContentRaw());

        if(!message.getAttachments().isEmpty()){
            rawMessage += " (załącznik) ";
        }

        for(final Member member : message.getMentions().getMembers()){
            if(member == null) continue;
            final long id = member.getIdLong();
            rawMessage = rawMessage.replaceAll("<@" + id + ">", "@"+ this.getUserName(member, member.getUser()));
        }

        for(final GuildChannel guildChannel : message.getMentions().getChannels()){
            if(guildChannel == null) continue;
            final long id = guildChannel.getIdLong();
            rawMessage = rawMessage.replaceAll("<#" + id + ">", "#" + guildChannel.getName());
        }

        for(final Role role : message.getMentions().getRoles()){
            if(role == null) continue;
            final long id = role.getIdLong();
            rawMessage = rawMessage.replaceAll("<@" + id + ">", "@"+ role.getName());
        }

        return rawMessage;
    }

    private String generatorReply(final Message messageReference) {
        return messageReference == null ? "" : this.discordConfig.getDiscordMessagesConfig().getReplyStatement()
                .replaceAll("<msg>", this.generateRawMessage(messageReference))
                .replaceAll("<author>", this.getUserName(messageReference.getMember(), messageReference.getAuthor()));
    }   
}