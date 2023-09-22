package me.indian.bds.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.util.RateLimiter;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.rest.RestApiConfig;
import me.indian.bds.logger.Logger;
import me.indian.bds.manager.player.PlayerManager;
import me.indian.bds.util.GsonUtil;

import java.util.concurrent.TimeUnit;

public class RestWebsite {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final RestApiConfig restApiConfig;
    private final PlayerManager playerManager;

    public RestWebsite(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.restApiConfig = this.bdsAutoEnable.getConfig().getRestApiConfig();
        this.playerManager = this.bdsAutoEnable.getPlayerManager();
    }

    public void init() {
        if (!this.restApiConfig.isEnabled()) {
            this.logger.debug("&bRest API&r jest wyłączone");
            return;
        }
        try {
            final int port = this.restApiConfig.getPort();
            final int rateLimit = this.restApiConfig.getRateLimit();

            final Javalin app = Javalin.create().start(port);
            final RateLimiter limiter = new RateLimiter(TimeUnit.MINUTES);

            app.get("/api/stats/playtime", ctx -> {
                limiter.incrementCounter(ctx, rateLimit);
                ctx.contentType("application/json").result(GsonUtil.getGson().toJson(this.playerManager.getStatsManager().getPlayTime()));
            });

            app.get("/api/stats/deaths", ctx -> {
                limiter.incrementCounter(ctx, rateLimit);
                ctx.contentType("application/json").result(GsonUtil.getGson().toJson(this.playerManager.getStatsManager().getDeaths()));
            });

            app.get("/api/stats/players", ctx -> {
                limiter.incrementCounter(ctx, rateLimit);
                ctx.contentType("application/json").result(this.playersJson());
            });

            this.logger.info("Uruchomiono strone z rest api na porcie:&b " + port);
        } catch (final Exception exception) {
            this.logger.error("Nie udało się uruchomić storny z&b Rest API", exception);
        }
    }

    private String playersJson() {
        final JsonObject json = new JsonObject();
        final JsonArray onlinePlayers = new JsonArray();

        for (final String playerName : this.playerManager.getOnlinePlayers()) {
            onlinePlayers.add(playerName);
        }

        json.add("online", onlinePlayers);

        final JsonArray offlinePlayers = new JsonArray();

        for (final String playerName : this.playerManager.getOfflinePlayers()) {
            offlinePlayers.add(playerName);
        }

        json.add("offline", offlinePlayers);
        return GsonUtil.getGson().toJson(json);
    }
}