package me.indian.bds.discord;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.logger.Logger;
import me.indian.bds.manager.PlayerManager;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.ThreadUtil;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebHook {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Config config;
    private final String webhookURL;
    private final ExecutorService service;
    private final boolean enabled;
    private final Timer timer;

    public WebHook(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.webhookURL = this.config.getWebHookChatUrl();
        this.service = Executors.newSingleThreadExecutor(new ThreadUtil("Discord-WebHook"));
        this.enabled = this.config.isEnableChat();
        this.timer = new Timer();
    }

    public void sendDiscordMessage(final String message) {
        if (!this.enabled) return;
        this.service.execute(() -> {
            try {
                final HttpURLConnection conn = (HttpURLConnection) new URL(webhookURL).openConnection();

                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                final String jsonPayload = "{\"content\":\"" + message + "\"}";

                final OutputStream os = conn.getOutputStream();
                os.write(jsonPayload.getBytes());
                os.flush();
                os.close();

                final int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                    this.logger.info("Kod odpowiedzi: " + responseCode);
                }
            } catch (final Exception exception) {
                this.logger.critical("Nie można wyslać wiadomości do discord: " + exception);
            }
        });
    }
}
