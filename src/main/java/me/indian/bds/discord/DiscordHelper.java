package me.indian.bds.discord;

import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.discord.jda.DiscordJDA;
import me.indian.bds.discord.jda.manager.LinkingManager;
import me.indian.bds.discord.jda.manager.StatsChannelsManager;
import me.indian.bds.discord.webhook.WebHook;
import me.indian.bds.logger.Logger;
import net.dv8tion.jda.api.JDA;

public class DiscordHelper {

    private final Logger logger;
    private final DiscordJDA discordJDA;
    private final WebHook webHook;
    private boolean botEnabled, webhookEnabled;

    public DiscordHelper(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();

        this.botEnabled = bdsAutoEnable.getAppConfigManager().getDiscordConfig().getBotConfig().isEnable();
        this.webhookEnabled = bdsAutoEnable.getAppConfigManager().getDiscordConfig().getWebHookConfig().isEnable();

        this.discordJDA = new DiscordJDA(bdsAutoEnable, this);
        this.webHook = new WebHook(bdsAutoEnable, this);
    }

    public void init() {
        this.discordJDA.init();
    }

    public void startShutdown() {
        final LinkingManager linkingManager = this.discordJDA.getLinkingManager();
        final StatsChannelsManager statsChannelsManager = this.discordJDA.getStatsChannelsManager();

        if (linkingManager != null) linkingManager.saveLinkedAccounts();
        if (statsChannelsManager != null) statsChannelsManager.onShutdown();

    }

    public void shutdown() {
        final JDA jda = this.discordJDA.getJda();
        if (jda != null) {
            if (jda.getStatus() == JDA.Status.CONNECTED) {
                try {
                    jda.shutdown();
                    if (!jda.awaitShutdown(10L, TimeUnit.SECONDS)) {
                        this.logger.info("Wyłączono bota");
                    }
                } catch (final Exception exception) {
                    this.logger.critical("Nie można wyłączyć bota", exception);
                }
            }
        }

        this.webHook.shutdown();
    }

    public DiscordJDA getDiscordJDA() {
        return this.discordJDA;
    }

    public WebHook getWebHook() {
        return this.webHook;
    }

    public boolean isBotEnabled() {
        return this.botEnabled;
    }

    public void setBotEnabled(final boolean botEnabled) {
        this.botEnabled = botEnabled;
    }

    public boolean isWebhookEnabled() {
        return this.webhookEnabled;
    }

    public void setWebhookEnabled(final boolean webhookEnabled) {
        this.webhookEnabled = webhookEnabled;
    }
}