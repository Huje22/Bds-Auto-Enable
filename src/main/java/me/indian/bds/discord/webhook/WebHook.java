package me.indian.bds.discord.webhook;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.ThreadUtil;

public class WebHook implements DiscordIntegration {

    private final Logger logger;
    private final Config config;
    private final String name, webhookURL , avatarUrl;
    private final ExecutorService service;
    private final Gson gson;

    public WebHook(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.config = bdsAutoEnable.getConfig();
        this.name =this.config.getWebHook().getName();
        this.webhookURL = this.config.getWebHook().getUrl();
        this.avatarUrl = this.config.getWebHook().getAvatarUrl();
        this.service = Executors.newSingleThreadExecutor(new ThreadUtil("Discord-WebHook"));
        this.gson = new Gson();
    }

    @Override
    public void init() {
    }

    private void sendMessage(final String message) {
        this.service.execute(() -> {
            try {
                final HttpURLConnection connection = (HttpURLConnection) new URL(this.webhookURL).openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                final JsonObject jsonPayload = new JsonObject();
                jsonPayload.addProperty("content", message);
                jsonPayload.addProperty("username", this.name);
                jsonPayload.addProperty("avatar_url", this.avatarUrl);

                final OutputStream os = connection.getOutputStream();
                os.write(this.gson.toJson(jsonPayload).getBytes());
                os.flush();
                os.close();

                final int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.logger.info("Kod odpowiedzi: " + responseCode);
                }
            } catch (final Exception exception) {
                this.logger.critical("Nie można wysłać wiadomości do Discord: " + exception);
            }
        });
    }


    @Override
    public void sendJoinMessage(final String playerName) {
        this.sendMessage(this.config.getMessages().getJoinMessage().replaceAll("<name>", playerName));
    }

    @Override
    public void sendLeaveMessage(final String playerName) {
        this.sendMessage(this.config.getMessages().getLeaveMessage().replaceAll("<name>", playerName));
    }

    @Override
    public void sendPlayerMessage(final String playerName, final String playerMessage) {
        this.sendMessage(this.config.getMessages().getMinecraftToDiscordMessage()
                .replaceAll("<name>", playerName)
                .replaceAll("<msg>", playerMessage)
        );
    }

    @Override
    public void sendDeathMessage(final String playerName, final String deathMessage) {
        this.sendMessage(this.config.getMessages().getDeathMessage()
                .replaceAll("<name>", playerName)
                .replaceAll("<casue>", deathMessage)
        );
    }

    @Override
    public void sendDisabledMessage() {
        this.sendMessage(this.config.getMessages().getDisabledMessage());
    }

    @Override
    public void sendDisablingMessage() {
        this.sendMessage(this.config.getMessages().getDisablingMessage());
    }

    @Override
    public void sendStopMessage() {
        this.sendMessage(this.config.getMessages().getDisablingMessage());
    }

    @Override
    public void sendEnabledMessage() {
        this.sendMessage(this.config.getMessages().getEnabledMessage());
    }

    @Override
    public void sendDestroyedMessage() {
        this.sendMessage(this.config.getMessages().getDestroyedMessage());
    }

    @Override
    public void disableBot() {

    }
}