package me.indian.bds.discord.jda.listener;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.*;
import me.indian.bds.watchdog.module.BackupModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CommandListener extends ListenerAdapter {

    private final DiscordJda discordJda;
    private final BDSAutoEnable bdsAutoEnable;
    private final ExecutorService reserve;
    private final Logger logger;
    private final Config config;
    private final List<Button> backupButtons;
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
        this.backupButtons = new ArrayList<>();
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
                    event.getChannel().sendMessage(this.serverProcess.commandAndResponse(rawMessage)).queue();
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
                        final MessageEmbed embed = new EmbedBuilder()
                                .setTitle("Ostatnia linijka z kosoli")
                                .setDescription(this.serverProcess.commandAndResponse(command))
                                .setColor(Color.BLUE)
                                .build();
                        event.replyEmbeds(embed).setEphemeral(true).queue();
                } else {
                    event.reply("Nie posiadasz permisij!!").setEphemeral(true).queue();
                }
            }

            case "ping" -> {
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Ping Bot <-> Discord")
                        .setDescription("Aktualny ping z serverami discord wynosi: " + this.jda.getGatewayPing() + " ms")
                        .setColor(Color.BLUE)
                        .build();

                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
            case "ip" -> {
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Nasze ip!")
                        .setDescription(MessageUtil.listToSpacedString(this.config.getDiscordBot().getIpMessage()))
                        .setColor(Color.BLUE)
                        .build();

                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
            case "stats" -> {
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Statystyki ")
                        .setDescription(MessageUtil.listToSpacedString(StatusUtil.getStatus(true)))
                        .setColor(Color.BLUE)
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
                        .build();

                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
            case "backup" -> event.replyEmbeds(this.getBackupEmbed())
                    .addActionRow(ActionRow.of(this.backupButtons).getComponents())
                    .setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(final ButtonInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) return;

        for (final Path path : this.backupModule.getBackups()) {
            final String fileName = path.getFileName().toString();
            if (event.getComponentId().equals("delete_backup:" + fileName)) {
                if (!member.hasPermission(Permission.ADMINISTRATOR)) {
                    event.reply("Nie posiadasz uprawnień!").setEphemeral(true).queue();
                    return;
                }
                try {
                    if (!Files.deleteIfExists(path)) {
                        event.reply("Nie udało się usunąć backupa " + fileName).setEphemeral(true).queue();
                        return;
                    }
                    this.backupModule.getBackups().remove(path);
                    event.replyEmbeds(this.getBackupEmbed())
                            .addActionRow(ActionRow.of(this.backupButtons).getComponents())
                            .setEphemeral(true).queue();
                    return;
                } catch (final Exception exception) {
                    event.reply("Nie udało się usunąć backupa " + fileName + " " + exception.getMessage()).setEphemeral(true).queue();
                    exception.printStackTrace();
                }
            }
        }

        switch (event.getComponentId()) {
            case "backup" -> {
                if (member.hasPermission(Permission.ADMINISTRATOR)) {
                    this.backupModule.forceBackup();
                    this.reserve.execute(() -> {
                        ThreadUtil.sleep((int) this.config.getLastBackupTime() + 2);
                        event.replyEmbeds(this.getBackupEmbed())
                                .addActionRow(ActionRow.of(this.backupButtons).getComponents())
                                .setEphemeral(true).queue();
                    });
                } else {
                    event.reply("Nie posiadasz uprawnień!").setEphemeral(true).queue();
                }
            }
            default -> event.reply("Nie znaleziono guzika").setEphemeral(true).queue();
        }
    }

    private MessageEmbed getBackupEmbed() {
        final String backupStatus = "`" + this.backupModule.getStatus() + "`\n";
        final long gbSpace = StatusUtil.availableGbSpace();
        final long mbSpace = StatusUtil.availableMbSpace() % 1024;
        final List<String> description = new ArrayList<>();
        this.backupButtons.clear();
        this.backupButtons.add(Button.primary("backup", "Backup").withEmoji(Emoji.fromFormatted("<:bds:1138355151258783745>")));

        for (final Path path : this.backupModule.getBackups()) {
            final String fileName = path.getFileName().toString();
            if (!(this.backupButtons.size() == 5)) {
                this.backupButtons.add(Button.danger("delete_backup:" + fileName, "Usuń " + fileName)
                        .withEmoji(Emoji.fromUnicode("🗑️")));
            }
            long fileSizeBytes;
            try {
                fileSizeBytes = Files.size(path);
            } catch (final IOException exception) {
                fileSizeBytes = -1;
            }
            
            long gigabytes = MathUtil.bytesToGB(fileSizeBytes);
            long remainderBytes = fileSizeBytes % (1024 * 1024 * 1024);
            long megabytes = MathUtil.bytesToMB(remainderBytes);
            long kilobytes = MathUtil.bytesToKB(remainderBytes % (1024 * 1024));
            description.add("Nazwa: `" + fileName.replaceAll(".zip", "") + "` Rozmiar: `" + gigabytes + "` GB `" + megabytes + "` MB `" + kilobytes + "` KB");
        }

        return new EmbedBuilder()
                .setTitle("Backup info")
                .setDescription("Status ostatniego backup: " + backupStatus +
                        "Następny backup za: `" + DateUtil.formatTime(this.backupModule.calculateMillisUntilNextBackup()) + "`\n" +
                        "Dostepa pamięć: `" + gbSpace + " GB " + mbSpace + " MB`\n" +
                        (description.isEmpty() ? "**Brak dostępnych backup**" : "**Dostępne backupy**:\n" + MessageUtil.listToSpacedString(description) + "\n") +
                        (gbSpace < 10 ? "**Zbyt mało pamięci aby wykonać backup!**" : "")
                )
                .setColor(Color.BLUE)
                .build();
    }
}
