package me.indian.bds.rest;

import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.util.RateLimiter;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.rest.RestApiConfig;
import me.indian.bds.logger.Logger;
import me.indian.bds.rest.post.key.CommandPostRequest;
import me.indian.bds.rest.post.key.PlayerInfoPostRequest;
import me.indian.bds.rest.post.key.discord.DiscordMessagePostRequest;
import me.indian.bds.rest.request.StatsRequest;
import me.indian.bds.rest.request.key.BackupRequest;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class RestWebsite {

    private final BDSAutoEnable bdsAutoEnable;
    private final Javalin app;
    private final RateLimiter limiter;
    private final Logger logger;
    private final RestApiConfig restApiConfig;
    private final List<Request> requests;
    private final File htmlFile;
    private String htmlFileContent;

    public RestWebsite(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.app = Javalin.create();
        this.limiter = new RateLimiter(TimeUnit.MINUTES);
        this.logger = bdsAutoEnable.getLogger();
        this.restApiConfig = bdsAutoEnable.getAppConfigManager().getRestApiConfig();
        this.requests = new ArrayList<>();
        this.htmlFile = new File(DefaultsVariables.getAppDir() + File.separator + "config" + File.separator + "Website.html");

        this.createHTMLFile();
        this.refreshFileContent();
        this.requests.add(new StatsRequest(this, bdsAutoEnable));
        this.requests.add(new BackupRequest(this, bdsAutoEnable));
        this.requests.add(new CommandPostRequest(this, bdsAutoEnable));
        this.requests.add(new PlayerInfoPostRequest(this, bdsAutoEnable));
        this.requests.add(new DiscordMessagePostRequest(this, bdsAutoEnable));
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
                if (!this.htmlFile.exists()) this.createHTMLFile();

                ctx.contentType(ContentType.TEXT_HTML)
                        .result(this.htmlFileContent
                                .replaceAll("<online>", String.valueOf(this.bdsAutoEnable.getServerManager().getOnlinePlayers().size()))
                                .replaceAll("<max>", String.valueOf(this.bdsAutoEnable.getServerProperties().getMaxPlayers()))
                                .replaceAll("<online-with-stats>", HTMLUtil.getOnlineWithStats(this.bdsAutoEnable))
                        );
            });

            for (final Request request : this.requests) {
                request.init();
            }

            this.logger.info("Uruchomiono strone z rest api na porcie:&b " + this.restApiConfig.getPort());
        } catch (final Exception exception) {
            this.logger.error("Nie udało się uruchomić strony z&b Rest API", exception);
        }
    }

    public boolean isCorrectApiKey(final Context ctx) {
        final String apiKey = ctx.pathParam("api-key");
        final String ip = ctx.ip();

        if (!this.restApiConfig.getApiKeys().contains(apiKey)) {
            ctx.status(HttpStatus.UNAUTHORIZED).contentType(ContentType.APPLICATION_JSON)
                    .result(GsonUtil.getGson().toJson("Klucz API \"" + apiKey + "\" nie jest obsługiwany"));

            this.logger.debug("&b" + ip + "&r używa niepoprawnego klucza autoryzacji&c " + apiKey);
            return false;
        }
        return true;
    }

    public void addRateLimit(final Context ctx) {
        this.limiter.incrementCounter(ctx, this.restApiConfig.getRateLimit());
    }

    public void incorrectJsonMessage(final Context ctx) {
        this.incorrectJsonMessage(ctx, null);
    }

    public void incorrectJsonMessage(final Context ctx, final @Nullable Exception exception) {
        final String ip = ctx.ip();
        final String requestBody = ctx.body();

        this.logger.debug("&b" + ip + "&r wysła niepoprawny json&1 " + requestBody.replaceAll("\n", ""), exception);
        ctx.status(HttpStatus.BAD_REQUEST).result("Niepoprawny Json! " + requestBody.replaceAll("\n", ""));
    }

    private void createHTMLFile() {
        try {
            if (!this.htmlFile.exists()) {
                if (!this.htmlFile.createNewFile()) {
                    this.logger.error("Nie można utworzyć&b Website.html");
                }
            }

            if (Files.size(this.htmlFile.toPath()) == 0) {
                try (final FileWriter writer = new FileWriter(this.htmlFile)) {
                    writer.write(HTMLUtil.getExampleBody());
                }
            }
        } catch (final Exception exception) {
            this.logger.error("Nie udało się utworzyć&b Website.html", exception);
        }
    }

    private void refreshFileContent() {
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!RestWebsite.this.htmlFile.exists()) RestWebsite.this.createHTMLFile();

                try {
                    RestWebsite.this.htmlFileContent = MessageUtil.listToSpacedString(Files.readAllLines(RestWebsite.this.htmlFile.toPath()));
                } catch (final Exception exception) {
                    RestWebsite.this.htmlFileContent = "Strona odświeża<br><br> " + MessageUtil.getStackTraceAsString(exception);
                }
            }
        };

        final long minute = MathUtil.minutesTo(1, TimeUnit.MILLISECONDS);
        new Timer("Refresh File Content", true).scheduleAtFixedRate(task, 0, minute);
    }

    public Javalin getApp() {
        return this.app;
    }
}