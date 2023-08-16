package me.indian.bds.discord.jda;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.MinecraftUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiscordJda extends ListenerAdapter implements DiscordIntegration {


    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Config config;
    private final String prefix;
    private final long serverID;
    private final long channelID;
    private final long consolelID;
    private ServerProcess serverProcess;
    private JDA jda;
    private Guild guild;
    private TextChannel textChannel;
    private TextChannel consoleChannel;


    public DiscordJda(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.prefix = this.config.getPrefix();
        this.serverID = this.config.getServerID();
        this.channelID = this.config.getChannelID();
        this.consolelID = this.config.getConsoleID();
    }

    public void initServerProcess(final ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @Override
    public void init() {
        this.logger.info("&aŁadowanie bota....");
        if (this.config.getToken().isEmpty()) {
            this.logger.alert("&aNie znaleziono tokenu , pomijanie ładowania.");
            return;
        }

        try {
            this.jda = JDABuilder.create(this.config.getToken(), GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.MESSAGE_CONTENT)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS).enableCache(CacheFlag.EMOJI)
                    .setActivity(Activity.playing("Minecraft"))
                    .setEnableShutdownHook(false)
                    .addEventListeners(this)
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
            this.consoleChannel = this.guild.getTextChannelById(this.consolelID);
        } catch (final Exception exception) {
            this.logger.info("(konola) Nie można odnaleźc kanału z ID &a" + this.consolelID);
            this.jda.shutdown();
        }

    }


    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        final Member member = event.getMember();
        final User author = event.getAuthor();
        final Message message = event.getMessage();
        final String rawMessage = message.getContentRaw();
        final TextChannel channel = event.getChannel().asTextChannel();

        if (event.getAuthor().isBot()) {
            return;
        }

        if (rawMessage.contains(event.getJDA().getSelfUser().getAsMention())) {
            event.getChannel().sendMessage("Mój prefix to `" + this.prefix + "` \n").queue(msg -> {
                msg.delete().queueAfter(10, TimeUnit.SECONDS);
                message.delete().queueAfter(9, TimeUnit.SECONDS);
            });
            return;
        }

        if (channel == this.consoleChannel) {
            if (member == null) return;
            if (member.hasPermission(Permission.ADMINISTRATOR)) {
                this.serverProcess.sendToConsole(rawMessage);
            } else {
                event.getChannel().sendMessage("Nie masz uprawnień administratora aby wysłać tu wiadomość").queue(msg -> {
                    msg.delete().queueAfter(5, TimeUnit.SECONDS);
                    message.delete().queueAfter(4, TimeUnit.SECONDS);
                });
            }
            return;
        }
        if (channel == this.textChannel && !rawMessage.startsWith(this.prefix)) {
            this.serverProcess.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.config.getMessages().get("DiscordToMinecraft")
                    .replaceAll("<name>", author.getName())
                    .replaceAll("<msg>", rawMessage.toString())));
        }


        if (rawMessage.startsWith(this.prefix)) {
            if (channel != this.textChannel) {
                channel.sendMessage("Polecenia można wykonywać tylko na kanale czatu <#" + this.channelID + ">").queue(msg ->
                        msg.delete().queueAfter(5, TimeUnit.SECONDS));
                message.delete().queueAfter(1, TimeUnit.SECONDS);
                return;
            }

            final String command = rawMessage.substring(this.prefix.length());
            switch (command.toLowerCase()) {
                case "help", "pomoc" -> {
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Lista poleceń")
                            .setDescription("`" + this.prefix + "help` - lista poleceń\n" +
                                    "`" + this.prefix + "list` - lista graczy online\n" +
                                    "`" + this.prefix + "ping` - aktualny ping bota\n" +
                                    "`" + this.prefix + "uptime` - czas działania aplikacij i bota\n" +
                                    "`" + this.prefix + "ip` - ip ustawione w config\n"
                            )
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(10, TimeUnit.SECONDS);
                        event.getMessage().delete().queueAfter(9, TimeUnit.SECONDS);
                    });
                }
                case "ping" -> {
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Lista poleceń")
                            .setDescription("Aktualny ping z serverami discord wynosi: " + this.jda.getGatewayPing() + " ms")
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(10, TimeUnit.SECONDS);
                        event.getMessage().delete().queueAfter(9, TimeUnit.SECONDS);
                    });
                }
                case "ip" -> {
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Nasze ip!")
                            .setDescription(MessageUtil.listToSpacedString(this.config.getIPmessage()))
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(10, TimeUnit.SECONDS);
                        event.getMessage().delete().queueAfter(9, TimeUnit.SECONDS);
                    });
                }
                case "uptime" -> {
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Czas działania")
                            .setDescription("Czas działania aplikacij `" + MathUtil.formatTime(System.currentTimeMillis() - this.bdsAutoEnable.getStartTime()) + "`\n" +
                                    "Czas działania servera `" + MathUtil.formatTime(System.currentTimeMillis() - this.serverProcess.getStartTime()) + "`\n")
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(10, TimeUnit.SECONDS);
                        event.getMessage().delete().queueAfter(9, TimeUnit.SECONDS);
                    });
                }

                case "list" -> {
                    final List<String> players = this.bdsAutoEnable.getPlayerManager().getOnlinePlayers();
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Lista Graczy")
                            .setDescription(players.size() + "/" + this.bdsAutoEnable.getServerProperties().getMaxPlayers() + "\n`" +
                                    players.toString().replaceAll("\\[\\]", "") + "`\n")
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(10, TimeUnit.SECONDS);
                        event.getMessage().delete().queueAfter(9, TimeUnit.SECONDS);
                    });
                }
                default -> event.getChannel().sendMessage("Nieznana komenda: " + command).queue(msg -> {
                    msg.delete().queueAfter(5, TimeUnit.SECONDS);
                    message.delete().queue();
                });
            }
        }
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
        this.sendMessage(this.config.getMessages().get("Join").replaceAll("<name>", playerName));
    }

    @Override
    public void sendLeaveMessage(final String playerName) {
        this.sendMessage(this.config.getMessages().get("Leave").replaceAll("<name>", playerName));
    }

    @Override
    public void sendPlayerMessage(final String playerName, final String playerMessage) {
        this.sendMessage(this.config.getMessages().get("MinecraftToDiscord")
                .replaceAll("<name>", playerName)
                .replaceAll("<msg>", playerMessage)
        );
    }

    @Override
    public void sendDeathMessage(final String playerName, final String deathMessage) {
        this.sendMessage(this.config.getMessages().get("Death")
                .replaceAll("<name>", playerName)
                .replaceAll("<casue>", deathMessage)
        );
    }

    @Override
    public void sendDisabledMessage() {
        this.sendMessage(this.config.getMessages().get("Disabled"));
    }

    @Override
    public void sendDisablingMessage() {
        this.sendMessage(this.config.getMessages().get("Disabled"));
    }

    @Override
    public void sendStopMessage() {
        this.sendMessage(this.config.getMessages().get("Disabling"));
    }

    @Override
    public void sendEnabledMessage() {
        this.sendMessage(this.config.getMessages().get("Enabled"));
    }

    @Override
    public void sendDestroyedMessage() {
        this.sendMessage(this.config.getMessages().get("Destroyed"));
    }

    @Override
    public void disableBot() {
        if (this.jda != null) {
            if (this.jda.getStatus() == JDA.Status.CONNECTED) {
                this.jda.shutdown();
            }
        }
    }
}
