package me.indian.bds.rest.request;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.rest.Request;
import me.indian.bds.rest.RestWebsite;
import me.indian.bds.server.manager.ServerManager;
import me.indian.bds.util.GsonUtil;

public class StatsRequest implements Request {

    private final RestWebsite restWebsite;
    private final ServerManager serverManager;
    private final Javalin app;
    private final Gson gson;

    public StatsRequest(final RestWebsite restWebsite, final BDSAutoEnable bdsAutoEnable) {
        this.restWebsite = restWebsite;
        this.serverManager = bdsAutoEnable.getServerManager();
        this.app = this.restWebsite.getApp();
        this.gson = GsonUtil.GSON;
    }

    @Override
    public void init() {
        this.app.get("/api/stats/playtime", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType("application/json").result(this.gson.toJson(this.serverManager.getStatsManager().getPlayTime()));
        });

        this.app.get("/api/stats/deaths", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType("application/json").result(this.gson.toJson(this.serverManager.getStatsManager().getDeaths()));
        });

        this.app.get("/api/stats/players", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType("application/json").result(this.playersJson());
        });
    }

    private String playersJson() {
        final JsonObject json = new JsonObject();
        final JsonArray onlinePlayers = new JsonArray();

        for (final String playerName : this.serverManager.getOnlinePlayers()) {
            onlinePlayers.add(playerName);
        }

        json.add("online", onlinePlayers);

        final JsonArray offlinePlayers = new JsonArray();

        for (final String playerName : this.serverManager.getOfflinePlayers()) {
            offlinePlayers.add(playerName);
        }

        json.add("offline", offlinePlayers);
        return this.gson.toJson(json);
    }
}