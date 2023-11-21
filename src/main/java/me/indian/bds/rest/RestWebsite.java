package me.indian.bds.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.util.RateLimiter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.rest.RestApiConfig;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.logger.Logger;
import me.indian.bds.manager.server.ServerManager;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.watchdog.module.BackupModule;

public class RestWebsite {

    private final Logger logger;
    private final DiscordIntegration discordIntegration;
    private final RestApiConfig restApiConfig;
    private final ServerManager serverManager;
    private final BackupModule backupModule;

    public RestWebsite(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.discordIntegration = bdsAutoEnable.getDiscord();
        this.restApiConfig = bdsAutoEnable.getAppConfigManager().getRestApiConfig();
        this.serverManager = bdsAutoEnable.getServerManager();
        this.backupModule = bdsAutoEnable.getWatchDog().getBackupModule();
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

            app.after(ctx -> ctx.res().setCharacterEncoding("UTF-8"));

            app.get("/", ctx -> {
                final String info = """
                        Dostępne endointy to:
                                                
                        /api/stats/deaths - śmierci graczy\s
                        /api/stats/playtime - czas gry w ms graczy\s
                        /api/stats/players - gracze online i offline\s
                        /api/{api-key}/backup/{filename} - pobierz któryś z dostępnych backup (wymagany klucz autoryzacji)\s
                        """;

                ctx.contentType("text/json").result(info);
            });

            app.get("/api/stats/playtime", ctx -> {
                limiter.incrementCounter(ctx, rateLimit);
                ctx.contentType("application/json").result(GsonUtil.getGson().toJson(this.serverManager.getStatsManager().getPlayTime()));
            });

            app.get("/api/stats/deaths", ctx -> {
                limiter.incrementCounter(ctx, rateLimit);
                ctx.contentType("application/json").result(GsonUtil.getGson().toJson(this.serverManager.getStatsManager().getDeaths()));
            });

            app.get("/api/stats/players", ctx -> {
                limiter.incrementCounter(ctx, rateLimit);
                ctx.contentType("application/json").result(this.playersJson());
            });

            app.get("/api/{api-key}/backup/{filename}", ctx -> {
                limiter.incrementCounter(ctx, rateLimit);

                if (!this.checkApiKey(ctx)) return;

                final String filename = ctx.pathParam("filename");

                for (final Path path : this.backupModule.getBackups()) {
                    final String fileName = path.getFileName().toString().replaceAll(".zip", "");
                    if (filename.equalsIgnoreCase(fileName)) {

                        this.logger.info("&b" + ctx.ip() + "&r pobiera&3 " + filename);
                        this.discordIntegration.writeConsole("`" + ctx.ip() + "` pobiera `" + filename + "`");

                        final File file = new File(path.toString());
                        ctx.res().setHeader("Content-Disposition", "attachment; filename=" + filename + ".zip");
                        ctx.res().setHeader("Content-Type", "application/zip");

                        try (final OutputStream os = ctx.res().getOutputStream(); final FileInputStream fis = new FileInputStream(file)) {
                            final byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                        } catch (final IOException exception) {
                            this.logger.error("", exception);
                            ctx.status(500).contentType("application/json")
                                    .result(exception.toString());
                        }
                        return;
                    }
                }

                final List<String> backupsNames = this.backupModule.getBackupsNames();
                backupsNames.replaceAll(name -> name.replaceAll(".zip", ""));

                final String currentUrl = ctx.req().getRequestURL().toString().replaceAll(filename, "");
                ctx.status(404).contentType("application/json").result("Dostępne Backupy to: \n"
                        + MessageUtil.listToSpacedString(backupsNames) + "\n \n" +
                        "Użyj np: " + currentUrl + backupsNames.get(0)
                );
            });

            this.logger.info("Uruchomiono strone z rest api na porcie:&b " + port);
        } catch (final Exception exception) {
            this.logger.error("Nie udało się uruchomić storny z&b Rest API", exception);
        }
    }

    private boolean checkApiKey(final Context ctx) {
        final String apiKey = ctx.pathParam("api-key");

        if (!this.restApiConfig.getApiKeys().contains(apiKey)) {
            ctx.status(HttpStatus.UNAUTHORIZED).contentType("application/json")
                    .result("Ten klucz api nie istnieje");
            return false;
        }
        return true;
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
        return GsonUtil.getGson().toJson(json);
    }
}