package me.indian.bds.discord.jda.listener;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.ServerSetting;
import me.indian.bds.server.properties.Difficulty;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.watchdog.module.BackupModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
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

public class CommandListener extends ListenerAdapter implements JDAListener {

    private final DiscordJda discordJda;
    private final BDSAutoEnable bdsAutoEnable;
    private final Config config;
    private final List<Button> backupButtons, difficultyButtons;
    private JDA jda;
    private ServerProcess serverProcess;
    private BackupModule backupModule;

    public CommandListener(final DiscordJda discordJda, final BDSAutoEnable bdsAutoEnable) {
        this.discordJda = discordJda;
        this.bdsAutoEnable = bdsAutoEnable;
        this.config = this.bdsAutoEnable.getConfig();
        this.backupButtons = new ArrayList<>();
        this.difficultyButtons = new ArrayList<>();
    }

    @Override
    public void init() {
        this.jda = this.discordJda.getJda();
        this.backupModule = this.bdsAutoEnable.getWatchDog().getBackupModule();
    }

    @Override
    public void initServerProcess(final ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @Override
    public void onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) return;
        this.discordJda.logCommand(this.getCommandEmbed(event));

        switch (event.getName()) {
            case "cmd" -> {
                if (member.hasPermission(Permission.ADMINISTRATOR)) {
                    if (!this.serverProcess.isEnabled()) {
                        event.reply("Server jest wyłączony").setEphemeral(true).queue();
                        return;
                    }

                    final String command = event.getOption("command").getAsString();
                    if (command.isEmpty()) {
                        event.reply("Polecenie nie może być puste!").setEphemeral(true).queue();
                        return;
                    }

                    this.bdsAutoEnable.getLogger().print(command, this.discordJda);

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
                        .setDescription(MessageUtil.listToSpacedString(this.config.getDiscordConfig().getDiscordBotConfig().getIpMessage()))
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
                final String list = "`" + MessageUtil.stringListToString(players, "`, `") + "`";
                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Lista Graczy")
                        .setDescription(players.size() + "/" + this.bdsAutoEnable.getServerProperties().getMaxPlayers() + "\n" +
                                (players.isEmpty() ? " " : list) + "\n")
                        .setColor(Color.BLUE)
                        .build();

                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
            case "backup" -> {
                if (!this.config.getWatchDogConfig().getBackupConfig().isBackup()) {
               event.reply("Backupy są wyłączone")
                   .setEphemeral(true).queue();
                    return;
                }
                
                final OptionMapping command = event.getOption("load");
                if (command != null && !command.getAsString().isEmpty()) {
                    if (member.hasPermission(Permission.ADMINISTRATOR)) {
                        final String backupName = command.getAsString();

                        for (final Path path : this.backupModule.getBackups()) {
                            final String fileName = path.getFileName().toString().replaceAll(".zip", "");
                            if (backupName.equalsIgnoreCase(fileName)) {
                                event.reply("Znaleziono: `" + backupName + "`").setEphemeral(true).queue();
                                this.backupModule.loadBackup(backupName);
                                return;
                            }
                        }
                        event.reply("Nie można znaleźć: `" + backupName + "`").setEphemeral(true).queue();
                    } else {
                        event.reply("Nie posiadasz permisji").setEphemeral(true).queue();
                    }
                    return;
                }

                if (member.hasPermission(Permission.ADMINISTRATOR)) {
                    event.replyEmbeds(this.getBackupEmbed())
                            .addActionRow(ActionRow.of(this.backupButtons).getComponents())
                            .setEphemeral(true).queue();
                } else {
                    event.replyEmbeds(this.getBackupEmbed()).setEphemeral(true).queue();
                }
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
            case "difficulty" -> {
                if (member.hasPermission(Permission.ADMINISTRATOR)) {
                    event.replyEmbeds(this.getDifficultyEmbed())
                            .addActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                            .setEphemeral(true).queue();
                } else {
                    event.replyEmbeds(this.getDifficultyEmbed()).setEphemeral(true).queue();
                }
            }
            case "version" -> {
                final String current = this.config.getVersionManagerConfig().getVersion();
                String latest = this.bdsAutoEnable.getVersionManager().getLatestVersion();
                if (latest.equals("")) {
                    latest = current;
                }

                final String checkLatest = (current.equals(latest) ? "`" + latest + "`" : "`" + current + "` (Najnowsza to: `" + latest + "`)");

                final MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Informacje o wersji")
                        .setDescription("**Wersjia __BDS-Auto-Enable__**: `" + this.bdsAutoEnable.getProjectVersion() + "`\n" +
                                "**Wersjia Servera **: " + checkLatest + "\n"
                        )
                        .setColor(Color.BLUE)
                        .build();

                if (member.hasPermission(Permission.ADMINISTRATOR)) {
                    Button button = Button.primary("update", "Update")
                            .withEmoji(Emoji.fromUnicode("\uD83D\uDD3C"));
                    if (current.equals(latest)) {
                        button = button.asDisabled();
                    } else {
                        button = button.asEnabled();
                    }

                    event.replyEmbeds(embed).addActionRow(button).setEphemeral(true).queue();
                } else {
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                }
            }
        }
    }

    @Override
    public void onButtonInteraction(final ButtonInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) return;
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("Nie masz permisji!").setEphemeral(true).queue();
            return;
        }
        this.serveDifficultyButton(event);
        this.serveBackupButton(event);
        this.serveDeleteBackupButton(event);
        this.serveUpdateButton(event);
    }

    private void serveDifficultyButton(final ButtonInteractionEvent event) {
        switch (event.getComponentId()) {
            case "peaceful" -> {
                this.serverProcess.changeSetting(ServerSetting.difficulty, Difficulty.PEACEFUL);
                event.replyEmbeds(this.getDifficultyEmbed())
                        .addActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .setEphemeral(true).queue();
            }
            case "easy" -> {
                this.serverProcess.changeSetting(ServerSetting.difficulty, Difficulty.EASY);
                event.replyEmbeds(this.getDifficultyEmbed())
                        .addActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .setEphemeral(true).queue();
            }
            case "normal" -> {
                this.serverProcess.changeSetting(ServerSetting.difficulty, Difficulty.NORMAL);
                event.replyEmbeds(this.getDifficultyEmbed())
                        .addActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .setEphemeral(true).queue();
            }
            case "hard" -> {
                this.serverProcess.changeSetting(ServerSetting.difficulty, Difficulty.HARD);
                event.replyEmbeds(this.getDifficultyEmbed())
                        .addActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .setEphemeral(true).queue();
            }
        }
    }

    private void serveDeleteBackupButton(final ButtonInteractionEvent event) {
        for (final Path path : this.backupModule.getBackups()) {
            final String fileName = path.getFileName().toString();
            if (event.getComponentId().equals("delete_backup:" + fileName)) {
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
    }

    private void serveBackupButton(final ButtonInteractionEvent event) {
        if (event.getComponentId().equals("backup")) {
            this.backupModule.forceBackup();
            event.reply("Backup jest tworzony").setEphemeral(true).queue();
        }
    }

    private void serveUpdateButton(final ButtonInteractionEvent event) {
        if (event.getComponentId().equals("update")) {
            event.reply("Server jest już prawdopodobnie aktualizowany , jeśli nie zajrzyj w konsole")
                    .setEphemeral(true).queue();
            this.bdsAutoEnable.getVersionManager().getVersionUpdater().updateToLatest();
        }
    }

    private MessageEmbed getDifficultyEmbed() {
        final Difficulty currentDifficulty = this.bdsAutoEnable.getServerProperties().getDifficulty();
        final int currentDifficultyId = this.bdsAutoEnable.getServerProperties().getDifficulty().getDifficultyId();
        this.difficultyButtons.clear();

        final Button peaceful = Button.primary("peaceful", "Pokojowy").withEmoji(Emoji.fromUnicode("☮️"));
        final Button easy = Button.primary("easy", "Easy").withEmoji(Emoji.fromFormatted("<:NOOB:717474733167476766>"));
        final Button normal = Button.primary("normal", "Normalny").withEmoji(Emoji.fromFormatted("<:bao_block_grass:1019717534976577617>"));
        final Button hard = Button.primary("hard", "Trudny").withEmoji(Emoji.fromUnicode("⚠️"));

        if (currentDifficultyId != 0) {
            this.difficultyButtons.add(peaceful);
        } else {
            this.difficultyButtons.add(peaceful.asDisabled());
        }

        if (currentDifficultyId != 1) {
            this.difficultyButtons.add(easy);
        } else {
            this.difficultyButtons.add(easy.asDisabled());
        }

        if (currentDifficultyId != 2) {
            this.difficultyButtons.add(normal);
        } else {
            this.difficultyButtons.add(normal.asDisabled());
        }

        if (currentDifficultyId != 3) {
            this.difficultyButtons.add(hard);
        } else {
            this.difficultyButtons.add(hard.asDisabled());
        }


        return new EmbedBuilder()
                .setTitle("Difficulty")
                .setDescription("Aktualny poziom trudności to: " + "`" + currentDifficulty.getDifficultyName() + "`")
                .setColor(Color.BLUE)
                .build();
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
            description.add("Nazwa: `" + fileName.replaceAll(".zip", "") + "` Rozmiar: `" + gb + "` GB `" + mb + "` MB `" + kb + "` KB");
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
        final Guild guild = event.getGuild();
        String guildName = "";
        if (guild != null && !guild.getName().isEmpty()) {
            guildName = guild.getName();
        }

        return new EmbedBuilder()
                .setTitle("**Command info**")
                .setDescription("`/" + event.getName() + "`")
                .setColor(Color.BLUE)
                .setFooter(user.getName() + " | " + guildName + " | " + DateUtil.getDate(), user.getAvatarUrl())
                .build();
    }
}