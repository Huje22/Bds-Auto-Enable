package me.indian.bds.discord.jda.listener;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.config.sub.discord.BotConfig;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.discord.DiscordLogChannelType;
import me.indian.bds.discord.jda.DiscordJDA;
import me.indian.bds.discord.jda.manager.LinkingManager;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.ServerStats;
import me.indian.bds.server.manager.StatsManager;
import me.indian.bds.server.properties.Difficulty;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.BackupModule;
import me.indian.bds.watchdog.module.PackModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CommandListener extends ListenerAdapter implements JDAListener {

    private final DiscordJDA DiscordJDA;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfigManager appConfigManager;
    private final DiscordConfig discordConfig;
    private final BotConfig botConfig;
    private final List<Button> backupButtons, difficultyButtons, statsButtons;
    private final ExecutorService service;
    private JDA jda;
    private StatsManager statsManager;
    private ServerProcess serverProcess;
    private BackupModule backupModule;
    private PackModule packModule;
    private LinkingManager linkingManager;


    public CommandListener(final DiscordJDA DiscordJDA, final BDSAutoEnable bdsAutoEnable) {
        this.DiscordJDA = DiscordJDA;
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
        this.discordConfig = this.appConfigManager.getDiscordConfig();
        this.botConfig = this.discordConfig.getBotConfig();
        this.backupButtons = new ArrayList<>();
        this.difficultyButtons = new ArrayList<>();
        this.statsButtons = new ArrayList<>();
        this.service = Executors.newScheduledThreadPool(3, new ThreadUtil("Discord Command Listener"));
    }

    @Override
    public void init() {
        this.jda = this.DiscordJDA.getJda();
        this.statsManager = this.bdsAutoEnable.getServerManager().getStatsManager();
        this.backupModule = this.bdsAutoEnable.getWatchDog().getBackupModule();
        this.packModule = this.bdsAutoEnable.getWatchDog().getPackModule();
        this.linkingManager = this.DiscordJDA.getLinkingManager();
    }

    @Override
    public void initServerProcess(final ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @Override
    public void onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) return;

        this.service.execute(() -> {
            //Robie to na paru wątkach gdyby jakieś polecenie miało zblokować ten od JDA
            try {
                event.deferReply().setEphemeral(true).queue();

                switch (event.getName()) {
                    case "cmd" -> {
                        if (member.hasPermission(Permission.ADMINISTRATOR)) {
                            if (!this.serverProcess.isEnabled()) {
                                event.getHook().editOriginal("Server jest wyłączony").queue();
                                return;
                            }

                            final String command = event.getOption("command").getAsString();
                            if (command.isEmpty()) {
                                event.getHook().editOriginal("Polecenie nie może być puste!").queue();
                                return;
                            }

                            this.bdsAutoEnable.getLogger().print(command, this.DiscordJDA, DiscordLogChannelType.CONSOLE);

                            final MessageEmbed embed = new EmbedBuilder()
                                    .setTitle("Ostatnia linijka z konsoli")
                                    .setDescription(this.serverProcess.commandAndResponse(command))
                                    .setColor(Color.BLUE)
                                    .setFooter("Używasz: " + command)
                                    .build();

                            event.getHook().editOriginalEmbeds(embed).queue();

                        } else {
                            event.getHook().editOriginal("Nie posiadasz permisji!!").queue();
                        }
                    }
                    case "link" -> {
                        if (!this.packModule.isLoaded()) {
                            event.getHook().editOriginal("Paczka **" + this.packModule.getPackName() + "** nie została załadowana").queue();
                            return;
                        }
                        final OptionMapping codeMapping = event.getOption("code");
                        if (codeMapping != null && !codeMapping.getAsString().isEmpty()) {
                            final String code = codeMapping.getAsString();
                            final long id = member.getIdLong();
                            final long roleID = this.discordConfig.getBotConfig().getLinkingConfig().getLinkedPlaytimeRoleID();
                            final long hours = MathUtil.hoursFrom(this.bdsAutoEnable.getServerManager().getStatsManager()
                                    .getPlayTimeByName(this.linkingManager.getNameByID(id)), TimeUnit.MILLISECONDS);
                            final EmbedBuilder linkingEmbed = new EmbedBuilder().setTitle("Łączenie kont").setColor(Color.BLUE)
                                    /* .setFooter("Aby rozłączyć konto wpisz /unlink") */;

                            String hoursMessage = "";

                            if (hours < 5) {
                                if (this.jda.getRoleById(roleID) != null) {
                                    hoursMessage = "\nMasz za mało godzin gry aby otrzymać <@&" + roleID + "**" + hours + "** godzin gry)" +
                                            "\nDostaniesz role gdy wbijesz **5** godzin gry";
                                }
                            }

                            if (this.linkingManager.isLinked(id)) {
                                linkingEmbed.setDescription("Twoje konto jest już połączone z: **" + this.linkingManager.getNameByID(id) + "**" + hoursMessage);
                                event.getHook().editOriginalEmbeds(linkingEmbed.build()).queue();

                                return;
                            }

                            if (this.linkingManager.linkAccount(code, id)) {
                                linkingEmbed.setDescription("Połączono konto z nickiem: **" + this.linkingManager.getNameByID(id) + "**" + hoursMessage);
                                event.getHook().editOriginalEmbeds(linkingEmbed.build()).queue();
                                this.serverProcess.tellrawToPlayer(this.linkingManager.getNameByID(id),
                                        "&aPołączono konto z ID:&b " + id);
                            } else {
                                event.getHook().editOriginal("Kod nie jest poprawny").queue();
                            }
                        } else {
                            final List<String> linkedAccounts = this.getLinkedAccounts();
                            final MessageEmbed messageEmbed = new EmbedBuilder()
                                    .setTitle("Osoby z połączonym kontami")
                                    .setDescription((linkedAccounts.isEmpty() ? "**Brak połączonych kont**" : MessageUtil.listToSpacedString(linkedAccounts)))
                                    .setColor(Color.BLUE)
                                    .setFooter("Aby połączyć konto wpisz /link KOD")
                                    .build();

                            event.getHook().editOriginalEmbeds(messageEmbed).queue();
                        }
                    }

                    case "ping" -> {
                        final MessageEmbed embed = new EmbedBuilder()
                                .setTitle("Ping Bot <-> Discord")
                                .setDescription("Aktualny ping z serverami discord wynosi: " + this.jda.getGatewayPing() + " ms")
                                .setColor(Color.BLUE)
                                .build();

                        event.getHook().editOriginalEmbeds(embed).queue();
                    }

                    case "ip" -> {
                        final MessageEmbed embed = new EmbedBuilder()
                                .setTitle("Nasze ip!")
                                .setDescription(MessageUtil.listToSpacedString(this.botConfig.getIpMessage()))
                                .setColor(Color.BLUE)
                                .build();

                        event.getHook().editOriginalEmbeds(embed).queue();
                    }

                    case "stats" -> {
                        if (member.hasPermission(Permission.ADMINISTRATOR)) {
                            event.getHook().editOriginalEmbeds(this.getStatsEmbed())
                                    .setActionRow(ActionRow.of(this.statsButtons).getComponents())
                                    .queue();
                        } else {
                            event.getHook().editOriginalEmbeds(this.getStatsEmbed()).queue();
                        }
                    }

                    case "list" -> {
                        if (!this.packModule.isLoaded()) {
                            event.getHook().editOriginal("Paczka **" + this.packModule.getPackName() + "** nie została załadowana").queue();
                            return;
                        }
                        final List<String> players = this.bdsAutoEnable.getServerManager().getOnlinePlayers();
                        final String list = "`" + MessageUtil.stringListToString(players, "`, `") + "`";
                        final EmbedBuilder embed = new EmbedBuilder()
                                .setTitle("Lista Graczy")
                                .setColor(Color.BLUE);

                        if (this.botConfig.isAdvancedPlayerList()) {
                            int counter = 0;

                            for (final String player : players) {
                                if (counter != 24) {
                                    embed.addField(player,
                                            "> Czas gry: **" + DateUtil.formatTime(this.statsManager.getPlayTimeByName(player), "days hours minutes seconds")
                                                    + "**  \n> Śmierci:** " + this.statsManager.getDeathsByName(player) + "**",
                                            true);
                                    counter++;
                                } else {
                                    embed.addField("**I pozostałe**", players.size() - 24 + " osób", false);
                                    break;
                                }
                            }
                        } else {
                            embed.setDescription(players.size() + "/" + this.bdsAutoEnable.getServerProperties().getMaxPlayers() + "\n" +
                                    (players.isEmpty() ? " " : list) + "\n");
                        }

                        event.getHook().editOriginalEmbeds(embed.build()).queue();
                    }

                    case "backup" -> {
                        if (!this.bdsAutoEnable.getAppConfigManager().getWatchDogConfig().getBackupConfig().isEnabled()) {
                            event.getHook().editOriginal("Backupy są wyłączone")
                                    .queue();
                            return;
                        }
                        if (member.hasPermission(Permission.ADMINISTRATOR)) {
                            event.getHook().editOriginalEmbeds(this.getBackupEmbed())
                                    .setActionRow(ActionRow.of(this.backupButtons).getComponents())
                                    .queue();
                        } else {
                            event.getHook().editOriginalEmbeds(this.getBackupEmbed()).queue();
                        }
                    }

                    case "playtime" -> {
                        if (!this.packModule.isLoaded()) {
                            event.getHook().editOriginal("Paczka **" + this.packModule.getPackName() + "** nie została załadowana").queue();
                            return;
                        }
                        final List<String> playTime = StatusUtil.getTopPlayTime(true, 100);
                        final ServerStats serverStats = this.bdsAutoEnable.getServerManager().getStatsManager().getServerStats();
                        final String totalUpTime = "Łączny czas działania servera: "
                                + DateUtil.formatTime(serverStats.getTotalUpTime(), "days hours minutes seconds ");

                        final MessageEmbed embed = new EmbedBuilder()
                                .setTitle("Top 100 Czasu gry")
                                .setDescription((playTime.isEmpty() ? "**Brak Danych**" : MessageUtil.listToSpacedString(playTime)))
                                .setColor(Color.BLUE)
                                .setFooter(totalUpTime)
                                .build();

                        event.getHook().editOriginalEmbeds(embed).queue();
                    }

                    case "deaths" -> {
                        if (!this.packModule.isLoaded()) {
                            event.getHook().editOriginal("Paczka **" + this.packModule.getPackName() + "** nie została załadowana").queue();
                            return;
                        }
                        final List<String> deaths = StatusUtil.getTopDeaths(true, 100);
                        final MessageEmbed embed = new EmbedBuilder()
                                .setTitle("Top 100 ilości śmierci")
                                .setDescription((deaths.isEmpty() ? "**Brak Danych**" : MessageUtil.listToSpacedString(deaths)))
                                .setColor(Color.BLUE)
                                .build();

                        event.getHook().editOriginalEmbeds(embed).queue();
                    }
                    case "difficulty" -> {
                        if (member.hasPermission(Permission.ADMINISTRATOR)) {
                            event.getHook().editOriginalEmbeds(this.getDifficultyEmbed())
                                    .setActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                                    .queue();
                        } else {
                            event.getHook().editOriginalEmbeds(this.getDifficultyEmbed()).queue();
                        }
                    }
                    case "version" -> {
                        final String current = this.appConfigManager.getVersionManagerConfig().getVersion();
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

                            event.getHook().editOriginalEmbeds(embed).setActionRow(button).queue();
                        } else {
                            event.getHook().editOriginalEmbeds(embed).queue();
                        }
                    }
                }
            } catch (final Exception exception) {
                this.logger.error("Wystąpił błąd przy próbie wykonania&b " + event.getName() + "&r przez&e " + member.getNickname(), exception);
                event.getHook().editOriginal("Wystąpił błąd " + exception).queue();
            }
        });
    }

    @Override
    public void onButtonInteraction(final ButtonInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) return;

        event.deferReply().setEphemeral(true).queue();

        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.getHook().editOriginal("Nie masz permisji!").queue();
            return;
        }

        this.serveDifficultyButton(event);
        this.serveBackupButton(event);
        this.serveDeleteBackupButton(event);
        this.serveUpdateButton(event);
        this.serveStatsButtons(event);
    }

    private List<String> getLinkedAccounts() {
        final Map<String, Long> linkedAccounts = this.linkingManager.getLinkedAccounts();
        final List<String> linked = new ArrayList<>();
        int place = 1;

        for (final Map.Entry<String, Long> entry : linkedAccounts.entrySet()) {
            final long hours = MathUtil.hoursFrom(this.bdsAutoEnable.getServerManager().getStatsManager()
                    .getPlayTimeByName(entry.getKey()), TimeUnit.MILLISECONDS);

            linked.add(place + ". **" + entry.getKey() + "**: " + entry.getValue() + " " + (hours < 5 ? "❌" : "✅"));
            place++;
        }

        return linked;
    }

    private void serveDifficultyButton(final ButtonInteractionEvent event) {
        switch (event.getComponentId()) {
            case "peaceful" -> {
                this.serverProcess.sendToConsole("difficulty " + Difficulty.PEACEFUL.getDifficultyName());
                this.bdsAutoEnable.getServerProperties().setDifficulty(Difficulty.PEACEFUL);
                event.getHook().editOriginalEmbeds(this.getDifficultyEmbed())
                        .setActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .queue();
            }
            case "easy" -> {
                this.serverProcess.sendToConsole("difficulty " + Difficulty.EASY.getDifficultyName());
                this.bdsAutoEnable.getServerProperties().setDifficulty(Difficulty.EASY);
                event.getHook().editOriginalEmbeds(this.getDifficultyEmbed())
                        .setActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .queue();
            }
            case "normal" -> {
                this.serverProcess.sendToConsole("difficulty " + Difficulty.NORMAL.getDifficultyName());
                this.bdsAutoEnable.getServerProperties().setDifficulty(Difficulty.NORMAL);
                event.getHook().editOriginalEmbeds(this.getDifficultyEmbed())
                        .setActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .queue();
            }
            case "hard" -> {
                this.serverProcess.sendToConsole("difficulty " + Difficulty.HARD.getDifficultyName());
                this.bdsAutoEnable.getServerProperties().setDifficulty(Difficulty.HARD);
                event.getHook().editOriginalEmbeds(this.getDifficultyEmbed())
                        .setActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .queue();
            }
        }
    }

    private void serveDeleteBackupButton(final ButtonInteractionEvent event) {
        for (final Path path : this.backupModule.getBackups()) {
            final String fileName = path.getFileName().toString();
            if (event.getComponentId().equals("delete_backup:" + fileName)) {
                try {
                    if (!Files.deleteIfExists(path)) {
                        event.getHook().editOriginal("Nie udało się usunąć backupa " + fileName).queue();
                        return;
                    }
                    this.backupModule.getBackups().remove(path);
                    event.getHook().editOriginalEmbeds(this.getBackupEmbed())
                            .setActionRow(ActionRow.of(this.backupButtons).getComponents())
                            .queue();
                    this.serverProcess.tellrawToAllAndLogger("&7[&bDiscord&7]",
                            "&aUżytkownik&b " + this.DiscordJDA.getUserName(event.getMember(), event.getUser()) +
                                    "&a usunął backup&b " + fileName + "&a za pomocą&e discord"
                            , LogState.INFO);

                    return;
                } catch (final Exception exception) {
                    event.getHook().editOriginal("Nie udało się usunąć backupa " + fileName + " " + exception.getMessage()).queue();
                }
            }
        }
    }

    private void serveBackupButton(final ButtonInteractionEvent event) {
        if (event.getComponentId().equals("backup")) {
            this.service.execute(() -> {
                if (this.backupModule.isBackuping()) {
                    event.getHook().editOriginal("Backup jest już robiony!").queue();
                    return;
                }
                if (!this.serverProcess.isEnabled()) {
                    event.getHook().editOriginal("Server jest wyłączony!").queue();
                    return;
                }

                this.backupModule.backup();
                ThreadUtil.sleep((int) this.appConfigManager.getWatchDogConfig().getBackupConfig().getLastBackupTime() + 3);
                event.getHook().editOriginalEmbeds(this.getBackupEmbed())
                        .setActionRow(this.backupButtons).queue();
            });
        }
    }

    private void serveUpdateButton(final ButtonInteractionEvent event) {
        this.service.execute(() -> {
            if (event.getComponentId().equals("update")) {
                event.getHook().editOriginal("Server jest już prawdopodobnie aktualizowany , jeśli nie zajrzyj w konsole")
                        .queue();
                this.bdsAutoEnable.getVersionManager().getVersionUpdater().updateToLatest();
            }
        });
    }

    private void serveStatsButtons(final ButtonInteractionEvent event) {
        if (!event.getComponentId().contains("stats_")) return;
        switch (event.getComponentId()) {
            case "stats_enable" -> {
                this.serverProcess.setCanRun(true);
                this.serverProcess.startProcess();
            }
            case "stats_disable" -> {
                this.serverProcess.setCanRun(false);
                this.serverProcess.kickAllPlayers("&aServer został wyłączony za pośrednictwem&b discord");
                this.serverProcess.sendToConsole("stop");
            }
        }
        ThreadUtil.sleep(3);
        event.getHook().editOriginalEmbeds(this.getStatsEmbed())
                .setActionRow(ActionRow.of(this.statsButtons).getComponents())
                .queue();
    }

    private MessageEmbed getStatsEmbed() {
        this.statsButtons.clear();

        final Button enable = Button.primary("stats_enable", "Włącz").withEmoji(Emoji.fromUnicode("✅"));
        final Button disable = Button.primary("stats_disable", "Wyłącz").withEmoji(Emoji.fromUnicode("🛑"));

        if (this.serverProcess.isEnabled()) {
            this.statsButtons.add(enable.asDisabled());
            this.statsButtons.add(disable);
        } else {
            this.statsButtons.add(enable);
            this.statsButtons.add(disable.asDisabled());
        }

        return new EmbedBuilder()
                .setTitle("Statystyki ")
                .setDescription(MessageUtil.listToSpacedString(StatusUtil.getStatus(true)))
                .setColor(Color.BLUE)
                .build();
    }

    private MessageEmbed getDifficultyEmbed() {
        final Difficulty currentDifficulty = this.bdsAutoEnable.getServerProperties().getDifficulty();
        final int currentDifficultyId = this.bdsAutoEnable.getServerProperties().getDifficulty().getDifficultyId();
        this.difficultyButtons.clear();

        final Button peaceful = Button.primary("peaceful", "Pokojowy").withEmoji(Emoji.fromUnicode("☮️"));
        final Button easy = Button.primary("easy", "Łatwy").withEmoji(Emoji.fromFormatted("<:NOOB:717474733167476766>"));
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

        final List<String> description = new ArrayList<>();
        this.backupButtons.clear();
        this.backupButtons.add(Button.primary("backup", "Backup")
                .withEmoji(Emoji.fromFormatted("<:bds:1138355151258783745>")));

        for (final Path path : this.backupModule.getBackups()) {
            final String fileName = path.getFileName().toString();
            if (!Files.exists(path)) continue;
            if (!(this.backupButtons.size() == 5)) {
                this.backupButtons.add(Button.danger("delete_backup:" + fileName, "Usuń " + fileName)
                        .withEmoji(Emoji.fromUnicode("🗑️")));
            }

            description.add("Nazwa: `" + fileName.replaceAll(".zip", "") + "` Rozmiar: `" + this.backupModule.getBackupSize(path.toFile(), true) + "`");
        }

        return new EmbedBuilder()
                .setTitle("Backup info")
                .setDescription("Status ostatniego backup: " + backupStatus +
                        "Następny backup za: `" + DateUtil.formatTime(this.backupModule.calculateMillisUntilNextBackup(), "days hours minutes seconds millis ") + "`\n" +
                        (description.isEmpty() ? "**Brak dostępnych backup**" : "**Dostępne backupy**:\n" + MessageUtil.listToSpacedString(description) + "\n") +
                        (gbSpace < 2 ? "**Zbyt mało pamięci aby wykonać backup!**" : ""))
                .setColor(Color.BLUE)
                .build();
    }
}
