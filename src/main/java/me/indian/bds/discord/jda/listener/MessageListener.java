package me.indian.bds.discord.jda.listener;

import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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

        final User author = event.getAuthor();
        final Message message = event.getMessage();
        final String rawMessage = message.getContentRaw();

        if (event.getChannel().asTextChannel() == this.textChannel) {
            final Role role = this.discordJda.getHighestRole(author.getIdLong());
            if (this.checkLength(message)) return;

            final String msg = this.discordConfig.getDiscordMessagesConfig().getDiscordToMinecraftMessage()
                    .replaceAll("<name>", this.getUserName(event.getMember(), author))
                    .replaceAll("<msg>", rawMessage)
                    .replaceAll("<reply>", this.generatorReply(message.getReferencedMessage()))
                    .replaceAll("<role>", role == null ? "" : role.getName()) + this.discordConfig.getDiscordMessagesConfig().getEdited();

            this.serverProcess.tellrawToAll(msg);
            this.logger.info(msg);
        }
    }

//TODO: Add method to do message receive and Update in one method
//TODO: Add geting mention users/chanel/roles as name

    
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
            final Role role = this.discordJda.getHighestRole(author.getIdLong());
            if (this.checkLength(message)) return;

            final String msg = this.discordConfig.getDiscordMessagesConfig().getDiscordToMinecraftMessage()
                    .replaceAll("<name>", this.getUserName(member, author))
                    .replaceAll("<msg>", rawMessage)
                    .replaceAll("<reply>", this.generatorReply(message.getReferencedMessage()))
                    .replaceAll("<role>", role == null ? "" : role.getName());

            this.serverProcess.tellrawToAll(msg);
            this.logger.info(msg);
        }
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


    private String generatorReply(final Message messageReference) {
        return messageReference == null ? "" : this.discordConfig.getDiscordMessagesConfig()
                .getReplyStatement().replaceAll("<msg>", messageReference.getContentRaw())
                .replaceAll("<author>", this.getUserName(messageReference.getMember(), messageReference.getAuthor()));
    }
//TODO: Użyć getUserName();
    
}
