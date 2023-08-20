package me.indian.bds.discord.jda.listener;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.exception.BadThreadException;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.MinecraftUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.BackupModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class MessageListener extends ListenerAdapter {

    private final DiscordJda discordJda;
    private final BDSAutoEnable bdsAutoEnable;
    private final ExecutorService reserve;
    private final Logger logger;
    private final Config config;
    private final String prefix;
    private final int selfDestruction, messageDestruction;
    private JDA jda;
    private TextChannel textChannel;
    private TextChannel consoleChannel;
    private ServerProcess serverProcess;
    private BackupModule backupModule;


    public MessageListener(final DiscordJda discordJda, final BDSAutoEnable bdsAutoEnable) {
        this.discordJda = discordJda;
        this.bdsAutoEnable = bdsAutoEnable;
        this.reserve = Executors.newSingleThreadExecutor(new ThreadUtil("Discord-reserve"));
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.prefix = this.config.getDiscordBot().getPrefix();
        this.selfDestruction = 30;
        this.messageDestruction = 30;
    }

    public void init() {
        this.textChannel = this.discordJda.getTextChannel();
        this.consoleChannel = this.discordJda.getConsoleChannel();
        this.jda = this.discordJda.getJda();
        this.backupModule = this.bdsAutoEnable.getWatchDog().getBackupModule();
    }

    public void initServerProcess(final ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @Override
    public void onMessageUpdate(final MessageUpdateEvent event) {
        final User author = event.getAuthor();
        final Message message = event.getMessage();
        final String rawMessage = message.getContentRaw();
        final TextChannel channel = event.getChannel().asTextChannel();

        if (channel == this.textChannel && !rawMessage.startsWith(this.prefix)) {
            final Role role = this.discordJda.getHighestRole(author.getIdLong());
            String higestRole = "";
            if (role != null) {
                higestRole = role.getName();
            }
            this.serverProcess.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.config.getMessages().getDiscordToMinecraftMessage()
                            .replaceAll("<name>", author.getName())
                            .replaceAll("<msg>", rawMessage) + "&r (edytowane)")
                    .replaceAll("<role>", higestRole));
            this.logger.info("&7[&bDiscord&e ->&a Minecraft&7] " + author.getName() + " »» " + rawMessage + "&r (edytowane)");
        }
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        final Member member = event.getMember();
        final User author = event.getAuthor();
        final Message message = event.getMessage();
        final String rawMessage = message.getContentRaw();
        final TextChannel channel = event.getChannel().asTextChannel();

        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;

        if (rawMessage.contains(event.getJDA().getSelfUser().getAsMention())) {
            event.getChannel().sendMessage("Mój prefix to `" + this.prefix + "` \n").queue(msg -> {
                msg.delete().queueAfter(this.selfDestruction, TimeUnit.SECONDS);
                message.delete().queueAfter(this.messageDestruction, TimeUnit.SECONDS);
            });
            return;
        }

        if (channel == this.consoleChannel) {
            if (member == null) return;
            if (member.hasPermission(Permission.ADMINISTRATOR)) {
                try {
                    event.getChannel().sendMessage(this.serverProcess.commandAndResponse(rawMessage)).queue();
                } catch (final BadThreadException e) {
                    throw new RuntimeException(e);
                }
            } else {
                event.getChannel().sendMessage("Nie masz uprawnień administratora aby wysłać tu wiadomość").queue(msg -> {
                    msg.delete().queueAfter(5, TimeUnit.SECONDS);
                    message.delete().queueAfter(4, TimeUnit.SECONDS);
                });
            }
            return;
        }
        if (channel == this.textChannel && !rawMessage.startsWith(this.prefix)) {
            final Role role = this.discordJda.getHighestRole(author.getIdLong());
            String higestRole = "";
            if (role != null) {
                higestRole = role.getName();
            }
            this.serverProcess.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.config.getMessages().getDiscordToMinecraftMessage()
                            .replaceAll("<name>", author.getName())
                            .replaceAll("<msg>", rawMessage))
                    .replaceAll("<role>", higestRole));
            this.logger.info("&7[&bDiscord&e ->&a Minecraft&7] " + author.getName() + " »» " + rawMessage);
        }

        if (rawMessage.startsWith(this.prefix)) {
            if (channel != this.textChannel) {
                channel.sendMessage("Polecenia można wykonywać tylko na kanale czatu <#" + this.discordJda.getChannelID() + ">").queue(msg ->
                        msg.delete().queueAfter(5, TimeUnit.SECONDS));
                message.delete().queueAfter(4, TimeUnit.SECONDS);
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
                                    "`" + this.prefix + "stats` - Statystyki Servera i aplikacij\n" +
                                    "`" + this.prefix + "backup` - tworzenie bądź ostatni czas backupa\n" +
                                    "`" + this.prefix + "ip` - ip ustawione w config\n"
                            )
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez: " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(this.selfDestruction, TimeUnit.SECONDS);
                        message.delete().queueAfter(this.messageDestruction, TimeUnit.SECONDS);
                    });
                }
                case "ping" -> {
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Ping Bot <-> Discord")
                            .setDescription("Aktualny ping z serverami discord wynosi: " + this.jda.getGatewayPing() + " ms")
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez: " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(this.selfDestruction, TimeUnit.SECONDS);
                        message.delete().queueAfter(this.messageDestruction, TimeUnit.SECONDS);
                    });
                }
                case "ip" -> {
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Nasze ip!")
                            .setDescription(MessageUtil.listToSpacedString(this.config.getDiscordBot().getIpMessage()))
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez: " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(this.selfDestruction, TimeUnit.SECONDS);
                        message.delete().queueAfter(this.messageDestruction, TimeUnit.SECONDS);
                    });
                }
                 case "stats" -> {
                     final MessageEmbed embed = new EmbedBuilder()
                             .setTitle("Statystyki ")
                             .setDescription(MessageUtil.listToSpacedString(StatusUtil.getStatus(true)))
                             .setColor(Color.BLUE)
                             .setFooter("Wywołane przez: " + author.getName(), author.getEffectiveAvatarUrl())
                             .build();

                     event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                         msg.delete().queueAfter(this.selfDestruction, TimeUnit.SECONDS);
                         message.delete().queueAfter(this.messageDestruction, TimeUnit.SECONDS);
                     });
                 }
                 case "list" -> {
                     final List<String> players = this.bdsAutoEnable.getPlayerManager().getOnlinePlayers();
                     final String list = "`" + players.toString().replaceAll("\\[", "").replaceAll("]", "") + "`";
                     final MessageEmbed embed = new EmbedBuilder()
                             .setTitle("Lista Graczy")
                             .setDescription(players.size() + "/" + this.bdsAutoEnable.getServerProperties().getMaxPlayers() + "\n" +
                                     (players.isEmpty() ? " " : list) + "\n")
                             .setColor(Color.BLUE)
                             .setFooter("Wywołane przez: " + author.getName(), author.getEffectiveAvatarUrl())
                             .build();

                     event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                         msg.delete().queueAfter(this.selfDestruction, TimeUnit.SECONDS);
                         message.delete().queueAfter(this.messageDestruction, TimeUnit.SECONDS);
                     });
                 }
                 case "backup" -> {
                     final String bakupStatus = "`" + this.backupModule.getStatus() + "`\n";
                     final MessageEmbed embed = new EmbedBuilder()
                             .setTitle("Backup info")
                             .setDescription("Status ostatniego backup: " + bakupStatus +
                                     "Następny backup za: `" + DateUtil.formatTime(this.backupModule.calculateMillisUntilNextBackup()) + "`\n"
                             )
                             .setColor(Color.BLUE)
                             .setFooter("Wywołane przez: " + author.getName(), author.getEffectiveAvatarUrl())
                             .build();

                     event.getChannel().sendMessageEmbeds(embed).addActionRow(
                                     Button.primary("backup", "Backup").withEmoji(Emoji.fromFormatted("<:bds:1138355151258783745>"))
                             )
                             .queue(msg -> {
                                 msg.delete().queueAfter(this.selfDestruction, TimeUnit.SECONDS);
                                 message.delete().queueAfter(this.messageDestruction, TimeUnit.SECONDS);
                             });
                 }
                 default -> event.getChannel().sendMessage("Nieznana komenda: " + command).queue(msg -> {
                     msg.delete().queueAfter(10, TimeUnit.SECONDS);
                     message.delete().queueAfter(9, TimeUnit.SECONDS);
                 });
             }
        }
    }

    @Override
    public void onButtonInteraction(final ButtonInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) return;

        switch (event.getComponentId()) {
            case "backup" -> {
                if (member.hasPermission(Permission.ADMINISTRATOR)) {
                    this.backupModule.forceBackup();
                    this.reserve.execute(() -> {
                        ThreadUtil.sleep((int) this.config.getLastBackupTime() + 2);
                        event.reply("Status backupa: `" + this.backupModule.getStatus() + "`").setEphemeral(true).queue();
                    });
                } else {
                    event.reply("Nie posiadasz uprawnień!").setEphemeral(true).queue();
                }
            }
        }
    }
}
