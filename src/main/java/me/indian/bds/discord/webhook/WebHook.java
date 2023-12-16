package me.indian.bds.discord.webhook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.component.Field;
import me.indian.bds.discord.component.Footer;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;

public class WebHook implements DiscordIntegration {

    private final Logger logger;
    private final AppConfigManager appConfigManager;
    private final DiscordConfig discordConfig;
    private final String name, webhookURL, avatarUrl;
    private final ExecutorService service;

    public WebHook(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.appConfigManager = bdsAutoEnable.getAppConfigManager();
        this.discordConfig = this.appConfigManager.getDiscordConfig();
        this.name = this.discordConfig.getWebHookConfig().getName();
        this.webhookURL = this.discordConfig.getWebHookConfig().getUrl();
        this.avatarUrl = this.discordConfig.getWebHookConfig().getAvatarUrl();
        this.service = Executors.newScheduledThreadPool(2, new ThreadUtil("Discord-WebHook"));
    }

    @Override
    public void init() {
    }

    @Override
    public void sendMessage(final String message) {
        // Nadal potrzeba by to ulepszyć
        this.service.execute(() -> {
            try {
                final HttpURLConnection connection = (HttpURLConnection) new URL(this.webhookURL).openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                final String finalMessage = message
                        .replaceAll("<owner>", "");


                final JsonObject jsonPayload = new JsonObject();
                jsonPayload.addProperty("content", finalMessage);
                jsonPayload.addProperty("username", this.name);
                jsonPayload.addProperty("avatar_url", this.avatarUrl);
                jsonPayload.addProperty("tts", false);

                if (finalMessage.isEmpty()) {
                    this.logger.error("Nie można wysłać pustej wiadomości!");
                    return;
                }

                try (final OutputStream os = connection.getOutputStream()) {
                    os.write(GsonUtil.getGson().toJson(jsonPayload).getBytes());
                    os.flush();
                }

                final int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.logger.debug("Kod odpowiedzi: " + responseCode);
                }
            } catch (final Exception exception) {
                this.logger.critical("Nie można wysłać wiadomości do Discord: " + exception);
            }
        });
    }

    @Override
    public void sendMessage(final String message, final Throwable throwable) {
        this.sendMessage(message +
                (throwable == null ? "" : "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```"));
    }

    @Override
    public void sendEmbedMessage(final String title, final String message, final List<Field> fields, final Footer footer) {
        this.service.execute(() -> {
            try {
                final HttpURLConnection connection = (HttpURLConnection) new URL(this.webhookURL).openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                final JsonObject jsonPayload = new JsonObject();
                jsonPayload.addProperty("username", this.name);
                jsonPayload.addProperty("avatar_url", this.avatarUrl);
                jsonPayload.addProperty("tts", false);
                
                final JsonObject embed = new JsonObject();
                embed.addProperty("title", title);
                embed.addProperty("description", message.replaceAll("<owner>", ""));

                if (fields != null && !fields.isEmpty()) {
                    final JsonArray fieldsArray = new JsonArray();
                    for (final Field field : fields) {
                        final JsonObject fieldObject = new JsonObject();
                        fieldObject.addProperty("name", field.name());
                        fieldObject.addProperty("value", field.value());
                        fieldObject.addProperty("inline", field.inline());
                        fieldsArray.add(fieldObject);
                    }

                    embed.add("fields", fieldsArray);
                }

                final JsonObject footerObject = new JsonObject();
                footerObject.addProperty("text", footer.text());
                footerObject.addProperty("icon_url", footer.imageURL());

                embed.add("footer", footerObject);
                embed.addProperty("color", 3838);

                final JsonArray embeds = new JsonArray();
                embeds.add(embed);
                jsonPayload.add("embeds", embeds);

                try (final OutputStream os = connection.getOutputStream()) {
                    os.write(GsonUtil.getGson().toJson(jsonPayload).getBytes());
                    os.flush();
                }

                final int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.logger.debug("Kod odpowiedzi: " + responseCode);
                }
            } catch (final Exception exception) {
                this.logger.critical("Nie można wysłać wiadomości do Discord: " + exception);
            }
        });
    }

    @Override
    public void sendEmbedMessage(final String title, final String message, final List<Field> fields, final Throwable throwable, final Footer footer) {
        this.sendEmbedMessage(title, message +
                "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```", fields, footer);
    }

    @Override
    public void sendEmbedMessage(final String title, final String message, final Footer footer) {
        this.sendEmbedMessage(title, message, (List<Field>) null, footer);
    }

    @Override
    public void sendEmbedMessage(final String title, final String message, final Throwable throwable, final Footer footer) {
        this.sendEmbedMessage(title, message +
                "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```", footer);
    }

    @Override
    public void sendJoinMessage(final String playerName) {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendJoinMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getJoinMessage().replaceAll("<name>", playerName));
        }
    }

    @Override
    public void sendLeaveMessage(final String playerName) {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendLeaveMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getLeaveMessage().replaceAll("<name>", playerName));
        }
    }


    @Override
    public void sendPlayerMessage(final String playerName, final String playerMessage) {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendPlayerMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getMinecraftToDiscordMessage()
                    .replaceAll("<name>", playerName)
                    .replaceAll("<msg>", playerMessage)
                    .replaceAll("@everyone", "/everyone/")
                    .replaceAll("@here", "/here/")
            );
        }
    }

    @Override
    public void sendDeathMessage(final String playerName, final String deathMessage) {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendDeathMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getDeathMessage()
                    .replaceAll("<name>", playerName)
                    .replaceAll("<casue>", deathMessage)
            );
        }
    }

    @Override
    public void sendDisabledMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendDisabledMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getDisabledMessage());
        }
    }

    @Override
    public void sendDisablingMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendDisablingMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getDisablingMessage());
        }
    }

    @Override
    public void sendProcessEnabledMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendProcessEnabledMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getProcessEnabledMessage());
        }
    }

    @Override
    public void sendEnabledMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendEnabledMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getEnabledMessage());
        }
    }

    @Override
    public void sendDestroyedMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendDestroyedMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getDestroyedMessage());
        }
    }

    @Override
    public void sendBackupDoneMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendBackupMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getBackupDoneMessage());
        }
    }

    @Override
    public void sendAppRamAlert() {
        if (this.appConfigManager.getWatchDogConfig().getRamMonitorConfig().isDiscordAlters()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getAppRamAlter());
        }
    }

    @Override
    public void sendMachineRamAlert() {
        if (this.appConfigManager.getWatchDogConfig().getRamMonitorConfig().isDiscordAlters()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getMachineRamAlter());
        }
    }

    @Override
    public void sendServerUpdateMessage(final String version) {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendServerUpdateMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getServerUpdate()
                    .replaceAll("<version>", version)
                    .replaceAll("<current>", this.appConfigManager.getVersionManagerConfig().getVersion())

            );
        }
    }

    @Override
    public void sendRestartMessage() {
        if (this.discordConfig.getDiscordMessagesOptionsConfig().isSendRestartMessage()) {
            this.sendMessage(this.discordConfig.getDiscordMessagesConfig().getRestartMessage());
        }
    }

    @Override
    public void writeConsole(final String message) {
        //.replaceAll("<owner>" , this.getOwnerMention())
    }

    @Override
    public void writeConsole(final String message, final Throwable throwable) {

    }

    @Override
    public void startShutdown() {

    }

    @Override
    public void shutdown() {

    }
}
