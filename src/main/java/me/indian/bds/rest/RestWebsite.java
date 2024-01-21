package me.indian.bds.rest;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.util.RateLimiter;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.rest.RestApiConfig;
import me.indian.bds.logger.Logger;
import me.indian.bds.rest.post.key.CommandPostRequest;
import me.indian.bds.rest.post.key.PlayerInfoPostRequest;
import me.indian.bds.rest.request.StatsRequest;
import me.indian.bds.rest.request.key.BackupRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RestWebsite {

    private final Javalin app;
    private final RateLimiter limiter;
    private final Logger logger;
    private final RestApiConfig restApiConfig;
    private final List<Request> requests;

    public RestWebsite(final BDSAutoEnable bdsAutoEnable) {
        this.app = Javalin.create();
        this.limiter = new RateLimiter(TimeUnit.MINUTES);
        this.logger = bdsAutoEnable.getLogger();
        this.restApiConfig = bdsAutoEnable.getAppConfigManager().getRestApiConfig();
        this.requests = new ArrayList<>();

        this.requests.add(new StatsRequest(this, bdsAutoEnable));
        this.requests.add(new BackupRequest(this, bdsAutoEnable));
        this.requests.add(new CommandPostRequest(this, bdsAutoEnable));
        this.requests.add(new PlayerInfoPostRequest(this, bdsAutoEnable));
    }

    public void init() {
        if (!this.restApiConfig.isEnabled()) {
            this.logger.debug("&bRest API&r jest wyłączone");
            return;
        }
        try {
            this.app.start(this.restApiConfig.getPort());
            this.app.after(ctx -> ctx.res().setCharacterEncoding("UTF-8"));

            this.app.get("/", ctx -> {
                final String info = """
                        Dostępne endointy to:
                                                
                        /api/stats/deaths - śmierci graczy\s
                        /api/stats/playtime - czas gry w ms graczy\s
                        /api/stats/players - gracze online i offline\s
                        /api/{api-key}/backup/{filename} - pobierz któryś z dostępnych backup (wymagany klucz autoryzacji)\s
                        """;

                ctx.contentType("text/json").result(info);
            });

            for (final Request request : this.requests) {
                request.init();
            }

            this.logger.info("Uruchomiono strone z rest api na porcie:&b " + this.restApiConfig.getPort());
        } catch (final Exception exception) {
            this.logger.error("Nie udało się uruchomić strony z&b Rest API", exception);
        }
    }

    public boolean checkApiKey(final Context ctx) {
        final String apiKey = ctx.pathParam("api-key");

        if (!this.restApiConfig.getApiKeys().contains(apiKey)) {
            ctx.status(HttpStatus.UNAUTHORIZED).contentType("application/json")
                    .result("Klucz API \"" + apiKey + "\" nie jest obsługiwany");
            return false;
        }
        return true;
    }

    public void addRateLimit(final Context ctx) {
        this.limiter.incrementCounter(ctx, this.restApiConfig.getRateLimit());
    }

    public Javalin getApp() {
        return this.app;
    }
}