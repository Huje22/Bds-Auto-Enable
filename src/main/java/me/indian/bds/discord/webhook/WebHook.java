package me.indian.bds.discord.webhook;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.discord.DiscordHelper;
import me.indian.bds.discord.embed.component.Field;
import me.indian.bds.discord.embed.component.Footer;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ThreadUtil;

public class WebHook {

    private final Logger logger;
    private final DiscordHelper discordHelper;
    private final String name, webhookURL, avatarUrl;
    private final ExecutorService service;
    private final ReentrantLock lock;
    private final Gson gson;
    private int requests;
    private boolean block;

    public WebHook(final BDSAutoEnable bdsAutoEnable, final DiscordHelper discordHelper) {
        this.logger = bdsAutoEnable.getLogger();
        final DiscordConfig discordConfig = bdsAutoEnable.getAppConfigManager().getDiscordConfig();
        this.discordHelper = discordHelper;
        this.name = discordConfig.getWebHookConfig().getName();
        this.webhookURL = discordConfig.getWebHookConfig().getChatUrl();
        this.avatarUrl = discordConfig.getWebHookConfig().getAvatarUrl();
        this.service = Executors.newScheduledThreadPool(2, new ThreadUtil("Discord-WebHook"));
        this.lock = new ReentrantLock();
        this.gson = GsonUtil.GSON;
        this.requests = 0;
        this.block = false;

        if (this.discordHelper.isWebhookEnabled()) {
            this.resetRequestsOnMinute();
        } else {
            this.service.shutdown();
        }
    }

    private void rateLimit() {
        this.logger.debug("Aktualna liczba wysłanych requestów do discord: " + this.requests + 1);
        if (this.requests == 37) {
            this.block = true;
            this.logger.debug("Czekanie ");
            ThreadUtil.sleep(60);
            this.requests = 0;
            this.block = false;
            this.logger.debug("Przeczekano ");
        }
    }

    private void resetRequestsOnMinute() {
        //Requesty nie muszą być zawsze wysyłane co sekunde
        // Więc po 3min resetuje je bo gdy wysyłamy jeden co np 10s to limit nie powinien zostać przekroczony

        final TimerTask task = new TimerTask() {
            public void run() {
                WebHook.this.requests = 0;
            }
        };
        new Timer("Webhook request cleaner", true).scheduleAtFixedRate(task, 0, MathUtil.minutesTo(3, TimeUnit.MILLISECONDS));
    }

    public void sendMessage(final String message) {
        if (!this.discordHelper.isWebhookEnabled()) return;

        this.service.execute(() -> {
            try {
                this.lock.lock();
                this.rateLimit();

                final HttpURLConnection connection = (HttpURLConnection) new URL(this.webhookURL).openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                final String finalMessage = message
                        .replaceAll("<owner>", this.discordHelper.getDiscordJDA().getOwnerMention());


                final JsonObject jsonPayload = new JsonObject();
                jsonPayload.addProperty("content", finalMessage);
                jsonPayload.addProperty("username", this.name);
                jsonPayload.addProperty("avatar_url", this.avatarUrl);
                jsonPayload.addProperty("tts", false);

                if (finalMessage.isEmpty()) {
                    this.logger.error("Nie można wysłać pustej wiadomości webhookiem!");
                    return;
                }

                try (final OutputStream os = connection.getOutputStream()) {
                    os.write(this.gson.toJson(jsonPayload).getBytes());
                    os.flush();
                }

                final int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    this.requests++;
                } else {
                    this.logger.debug("Kod odpowiedzi: " + responseCode);
                }
            } catch (final Exception exception) {
                this.logger.critical("Nie można wysłać wiadomości do discord ", exception);
            } finally {
                this.lock.unlock();
            }
        });
    }

    public void sendMessage(final String message, final Throwable throwable) {
        this.sendMessage(message +
                (throwable == null ? "" : "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```"));
    }

    public void sendEmbedMessage(final String title, final String message, final List<Field> fields, final Footer footer) {
        if (!this.discordHelper.isWebhookEnabled()) return;
        this.service.execute(() -> {
            try {
                this.lock.lock();
                this.rateLimit();

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
                embed.addProperty("description", message
                        .replaceAll("<owner>", this.discordHelper.getDiscordJDA().getOwnerMention())
                );

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
                    os.write(this.gson.toJson(jsonPayload).getBytes());
                    os.flush();
                }

                final int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    this.requests++;
                } else {
                    this.logger.debug("Kod odpowiedzi: " + responseCode);
                }
            } catch (final Exception exception) {
                this.logger.critical("Nie można wysłać wiadomości do discord ", exception);
            } finally {
                this.lock.unlock();
            }
        });
    }

    public void sendEmbedMessage(final String title, final String message, final List<Field> fields, final Throwable throwable, final Footer footer) {
        this.sendEmbedMessage(title, message +
                (throwable == null ? "" : "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```"), fields, footer);
    }

    public void sendEmbedMessage(final String title, final String message, final Footer footer) {
        this.sendEmbedMessage(title, message, (List<Field>) null, footer);
    }

    public void sendEmbedMessage(final String title, final String message, final Throwable throwable, final Footer footer) {
        this.sendEmbedMessage(title, message +
                (throwable == null ? "" : "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```"), footer);
    }

    public void shutdown() {
        if (this.discordHelper.isWebhookEnabled()) {
            try {
                while (this.block) {
                    this.logger.alert("Czekanie na możliwość wysłania requestów do discord");
                    ThreadUtil.sleep(10);
                }
//            while (this.requests == 9) {
//                this.logger.alert("Czekanie na możliwość wysłania requestów do discord");
//                ThreadUtil.sleep(21);
//            }

                this.logger.info("Zamykanie wątków Webhooku");
                this.service.shutdown();
                if (!this.service.awaitTermination(20, TimeUnit.SECONDS)) {
                    this.logger.error("Wątki nie zostały zamknięte na czas! Wymuszanie zamknięcia");
                    this.service.shutdownNow();
                }
                this.logger.info("Zamknięto wątki Webhooku");
            } catch (final Exception exception) {
                this.logger.critical("Wstąpił błąd przy próbie zamknięcia webhooku ", exception);
            }
        }
    }
}