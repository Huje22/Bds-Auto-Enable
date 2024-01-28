package me.indian.bds.rest.post.key.discord;

import com.google.gson.JsonSyntaxException;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.discord.DiscordHelper;
import me.indian.bds.discord.jda.DiscordJDA;
import me.indian.bds.logger.Logger;
import me.indian.bds.rest.Request;
import me.indian.bds.rest.RestWebsite;
import me.indian.bds.rest.component.discord.DiscordMessagePostData;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.GsonUtil;

import java.net.HttpURLConnection;

public class DiscordMessagePostRequest implements Request {

    private final RestWebsite restWebsite;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private final Javalin app;
    private final DiscordHelper discordHelper;
    private final DiscordJDA discordJDA;

    public DiscordMessagePostRequest(final RestWebsite restWebsite, final BDSAutoEnable bdsAutoEnable) {
        this.restWebsite = restWebsite;
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.app = this.restWebsite.getApp();
        this.discordHelper = this.bdsAutoEnable.getDiscordHelper();
        this.discordJDA = this.discordHelper.getDiscordJDA();
    }

    @Override
    public void init() {
        this.app.post("/discord/message/{api-key}", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            if (!this.restWebsite.isCorrectApiKey(ctx)) return;

            final String ip = ctx.ip();
            final String requestBody = ctx.body();
            final DiscordMessagePostData discordMessagePostData;

            try {
                discordMessagePostData = GsonUtil.getGson().fromJson(requestBody, DiscordMessagePostData.class);
            } catch (final JsonSyntaxException jsonSyntaxException) {
                this.restWebsite.incorrectJsonMessage(ctx, jsonSyntaxException);
                return;
            }

            if (discordMessagePostData.name() == null && discordMessagePostData.message() == null) {
                this.restWebsite.incorrectJsonMessage(ctx);
                return;
            }

            if (!this.discordHelper.isBotEnabled()) {
                ctx.status(HttpStatus.SERVICE_UNAVAILABLE).result("Bot jest wyłączony");
                return;
            }

            this.discordJDA.sendPlayerMessage(discordMessagePostData.name(), discordMessagePostData.message());
            this.logger.debug("&b" + ip + "&r używa poprawnie endpointu&1 DISCORD/MESSAGE");

            ctx.status(HttpURLConnection.HTTP_NO_CONTENT);
        });
    }
}