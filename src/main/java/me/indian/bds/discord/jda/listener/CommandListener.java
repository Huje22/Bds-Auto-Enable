package me.indian.bds.discord.jda.listener;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.BackupModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
    private final Config config;
    private final List<Button> backupButtons;
    private JDA jda;
    private ServerProcess serverProcess;
    private BackupModule backupModule;

    public CommandListener(final DiscordJda discordJda, final BDSAutoEnable bdsAutoEnable) {
        this.discordJda = discordJda;
        this.bdsAutoEnable = bdsAutoEnable;
        this.reserve = Executors.newSingleThreadExecutor(new ThreadUtil("Discord-reserve"));
        this.config = this.bdsAutoEnable.getConfig();
        this.backupButtons = new ArrayList<>();
    }

    public void init() {
        this.jda = this.discordJda.getJda();
        this.backupModule = this.bdsAutoEnable.getWatchDog().getBackupModule();
    }

    public void initServerProcess(final ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @Override
    public void onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
        final Member member = event.getMember();

        this.discordJda.logCommand(this.getCommandEmbed(event));

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
                            .setTitle("Ostatnia linijka z konsoli")
                            .setDescription(this.serverProcess.commandAndResponse(command))
                            .setColor(Color.BLUE)
                            .build();
                        event.replyEmbeds(embed).setEphemeral(true).queue();
                } else {
                    event.reply("Nie posiadasz permisji!!").setEphemeral(true).queue();
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
                final String list = "`" + MessageUtil.listToString(players, "`, `") + "`";
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Lista Graczy")
                        .setDescription(players.size() + "/" + this.bdsAutoEnable.getServerProperties().getMaxPlayers() + "\n" +
                                (players.isEmpty() ? " " : list) + "\n")
                        .setColor(Color.BLUE)
                        .build();

                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
            case "backup" -> {
                final OptionMapping command = event.getOption("load");
                if (command != null && !command.getAsString().isEmpty()) {
                    if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
                        final String backupName = command.getAsString();

                        for (final Path path : this.backupModule.getBackups()) {
                            final String fileName = path.getFileName().toString().replaceAll(".zip", "");
                            if (backupName.equalsIgnoreCase(fileName)) {
                                event.reply("Znaleziono: " + backupName).setEphemeral(true).queue();
                                this.backupModule.loadBackup(backupName);
                                return;
                            }
                        }
                        event.reply("Nie można znaleźć: " + backupName).setEphemeral(true).queue();
                        return;
                    } else {
                        event.reply("Nie posiadasz permisji").setEphemeral(true).queue();
                    }
                }

                event.replyEmbeds(this.getBackupEmbed())
                        .addActionRow(ActionRow.of(this.backupButtons).getComponents())
                        .setEphemeral(true).queue();
            }

            case "playtime" -> {
                final List<String> playTime = StatusUtil.getTopPlayTime(true, 20);
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Top 20 Czasu gry")
                        .setDescription((playTime.isEmpty() ? "**Brak Danych**" : MessageUtil.listToSpacedString(playTime)))
                        .setColor(Color.BLUE)
                        .build();

                event.replyEmbeds(embed).setEphemeral(true).queue();
            }

            case "deaths" -> {
                final List<String> deaths = StatusUtil.getTopDeaths(true, 20);
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Top 20 ilości śmierci")
                        .setDescription((deaths.isEmpty() ? "**Brak Danych**" : MessageUtil.listToSpacedString(deaths)))
                        .setColor(Color.BLUE)
                        .build();

                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
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
//                        ThreadUtil.sleep((int) this.config.getWatchDogConfig().getBackup().getLastBackupTime() + 2);
                        event.deferReply().addEmbeds(this.getBackupEmbed()).addActionRow(ActionRow.of(this.backupButtons)
                                        .getComponents()).setEphemeral(true)
                                .queueAfter((long) (this.config.getWatchDogConfig().getBackup().getLastBackupTime() + 2), TimeUnit.SECONDS);
//ZBAGOWANE GÓWNO
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
        final long gbSpace = MathUtil.bytesToGB(StatusUtil.availableDiskSpace());
        final String rom = "Dostępny: " + gbSpace + " GB " + MathUtil.getMbFromBytesGb(StatusUtil.availableDiskSpace()) + " MB";
        final String maxRom = "Całkowity: " + MathUtil.bytesToGB(StatusUtil.maxDiskSpace()) + " GB " + MathUtil.getMbFromBytesGb(StatusUtil.maxDiskSpace()) + " MB";

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

            final long gb = MathUtil.bytesToGB(fileSizeBytes);
            final long mb = MathUtil.getMbFromBytesGb(fileSizeBytes);
            final long kb = MathUtil.getKbFromBytesGb(fileSizeBytes);
            description.add("Nazwa: `" + fileName.replaceAll(".zip", "") + "` Rozmiar: `" + gb + "` GB `" + mb + "` MB `" + kb+ "` KB");
        }

        return new EmbedBuilder()
                .setTitle("Backup info")
                .setDescription("Status ostatniego backup: " + backupStatus +
                        "Następny backup za: `" + DateUtil.formatTime(this.backupModule.calculateMillisUntilNextBackup()) + "`\n" +
                        "Pamięć ROM: `" + rom + " / " + maxRom + "`\n" +
                        (description.isEmpty() ? "**Brak dostępnych backup**" : "**Dostępne backupy**:\n" + MessageUtil.listToSpacedString(description) + "\n") +
                        (gbSpace < 10 ? "**Zbyt mało pamięci aby wykonać backup!**" : ""))
                .setColor(Color.BLUE)
                .build();
    }

    private MessageEmbed getCommandEmbed(final SlashCommandInteractionEvent event) {
        final User user = event.getUser();
        return new EmbedBuilder()
                .setTitle("Command info")
                .setDescription("`" + user.getName() + "` **używa** `/" + event.getName() + "`")
                .setColor(Color.BLUE)
                .setFooter(user.getName(), user.getAvatarUrl())
                .build();
    }
}