package me.indian.bds.rest.post.key;

import com.google.gson.JsonSyntaxException;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.rest.Request;
import me.indian.bds.rest.RestWebsite;
import me.indian.bds.rest.component.PlayerPostData;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.GsonUtil;

import java.net.HttpURLConnection;

public class PlayerInfoPostRequest implements Request {

    private final RestWebsite restWebsite;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private final Javalin app;

    public PlayerInfoPostRequest(final RestWebsite restWebsite, final BDSAutoEnable bdsAutoEnable) {
        this.restWebsite = restWebsite;
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.app = this.restWebsite.getApp();
    }

    @Override
    public void init() {
        this.app.post("/playerInfo/{api-key}", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            if (!this.restWebsite.checkApiKey(ctx)) return;

            final String ip = ctx.ip();
            final String requestBody = ctx.body();

            System.out.println(requestBody);

            final PlayerPostData playerPostData;

            try {
                playerPostData = GsonUtil.getGson().fromJson(requestBody, PlayerPostData.class);
            } catch (final JsonSyntaxException jsonSyntaxException) {
                this.restWebsite.incorrectJsonMessage(ctx, jsonSyntaxException);
                return;
            }

            //TODO:Dokończyć to 

            if (!this.serverProcess.isEnabled()) {
                ctx.status(HttpStatus.SERVICE_UNAVAILABLE).result("Server jest wyłączony");
                return;
            }

            this.logger.debug("&b" + ip + "&r używa poprawnie endpointu&1 PLAYERINFO");
            ctx.status(HttpURLConnection.HTTP_NO_CONTENT);
        });
    }
}