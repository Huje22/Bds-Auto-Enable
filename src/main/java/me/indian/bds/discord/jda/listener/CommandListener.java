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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CommandListener extends ListenerAdapter {

    private final DiscordJda discordJda;
    private final BDSAutoEnable bdsAutoEnable;
    private final ExecutorService reserve;
    private final Logger logger;
    private final Config config;
    private JDA jda;
    private TextChannel textChannel;
    private TextChannel consoleChannel;
    private ServerProcess serverProcess;
    private BackupModule backupModule;
    
    public CommandListener(final DiscordJda discordJda, final BDSAutoEnable bdsAutoEnable) {
        this.discordJda = discordJda;
        this.bdsAutoEnable = bdsAutoEnable;
        this.reserve = Executors.newSingleThreadExecutor(new ThreadUtil("Discord-reserve"));
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
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

        if (channel == this.textChannel) {
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
        if (channel == this.textChannel) {
            final Role role = this.discordJda.getHighestRole(author.getIdLong());
            String higestRole = "";
            if (role != null) higestRole = role.getName();
            this.serverProcess.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.config.getMessages().getDiscordToMinecraftMessage()
                            .replaceAll("<name>", author.getName())
                            .replaceAll("<msg>", rawMessage))
                    .replaceAll("<role>", higestRole));
            this.logger.info("&7[&bDiscord&e ->&a Minecraft&7] " + author.getName() + " »» " + rawMessage);
        }
    }

    @Override
    public void onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
        final User user = event.getUser();
        final Member member = event.getMember();
        if (event.getChannel() != this.textChannel) {
            event.reply("Polecenia mogą zostać użyte tylko na <#" + this.textChannel.getId() + ">").setEphemeral(true).queue();
            return;
        }

        switch (event.getName()) {
            case "cmd" -> {
                if (member == null) return;
                if (member.hasPermission(Permission.ADMINISTRATOR)) {
                    final String command = event.getOption("command").getAsString();
                    if (command.isEmpty()) {
                        event.reply("Polecenie nie może być puste!").setEphemeral(true).queue();
                        return;
                    }
                    try {
                        final MessageEmbed embed = new EmbedBuilder()
                                .setTitle("Ostatnia linijka z kosoli")
                                .setDescription(this.serverProcess.commandAndResponse(command))
                                .setColor(Color.BLUE)
                                .setFooter("Wywołane przez: " + user.getName(), user.getEffectiveAvatarUrl())
                                .build();
                        event.replyEmbeds(embed).setEphemeral(true).queue();
                    } catch (final BadThreadException exception) {
                        exception.printStackTrace();
                        event.reply("Wystąpił błąd! " + exception.getMessage()).setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Nie posiadasz permisij!!").setEphemeral(true).queue();
                }
            }

            case "ping" -> {
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Ping Bot <-> Discord")
                        .setDescription("Aktualny ping z serverami discord wynosi: " + this.jda.getGatewayPing() + " ms")
                        .setColor(Color.BLUE)
                        .setFooter("Wywołane przez: " + user.getName(), user.getEffectiveAvatarUrl())
                        .build();

                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
            case "ip" -> {
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Nasze ip!")
                        .setDescription(MessageUtil.listToSpacedString(this.config.getDiscordBot().getIpMessage()))
                        .setColor(Color.BLUE)
                        .setFooter("Wywołane przez: " + user.getName(), user.getEffectiveAvatarUrl())
                        .build();

                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
            case "stats" -> {
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Statystyki ")
                        .setDescription(MessageUtil.listToSpacedString(StatusUtil.getStatus(true)))
                        .setColor(Color.BLUE)
                        .setFooter("Wywołane przez: " + user.getName(), user.getEffectiveAvatarUrl())
                        .build();

                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
            case "list" -> {
                final List<String> players = this.bdsAutoEnable.getPlayerManager().getOnlinePlayers();
                final String list = "`" + players.toString().replaceAll("\\[", "").replaceAll("]", "") + "`";
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Lista Graczy")
                        .setDescription(players.size() + "/" + this.bdsAutoEnable.getServerProperties().getMaxPlayers() + "\n" +
                                (players.isEmpty() ? " " : list) + "\n")
                        .setColor(Color.BLUE)
                        .setFooter("Wywołane przez: " + user.getName(), user.getEffectiveAvatarUrl())
                        .build();

                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
            case "backup" -> {
                final String bakupStatus = "`" + this.backupModule.getStatus() + "`\n";
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Backup info")
                        .setDescription("Status ostatniego backup: " + bakupStatus +
                                "Następny backup za: `" + DateUtil.formatTime(this.backupModule.calculateMillisUntilNextBackup()) + "`\n"
                        )
                        .setColor(Color.BLUE)
                        .setFooter("Wywołane przez: " + user.getName(), user.getEffectiveAvatarUrl())
                        .build();

                event.replyEmbeds(embed).addActionRow(
                        Button.primary("backup", "Backup").withEmoji(Emoji.fromFormatted("<:bds:1138355151258783745>"))).setEphemeral(true).queue();
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
