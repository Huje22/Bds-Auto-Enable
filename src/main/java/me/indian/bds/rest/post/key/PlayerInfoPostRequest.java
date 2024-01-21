package me.indian.bds.rest.post.key;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.rest.Request;
import me.indian.bds.rest.RestWebsite;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.GsonUtil;

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

            final PlayerInfoPostRequest playerInfoPostRequest = GsonUtil.getGson().fromJson(requestBody, PlayerInfoPostRequest.class);

            //TODO:Dokończyć to 

            if (!this.serverProcess.isEnabled()) {
                ctx.status(HttpStatus.SERVICE_UNAVAILABLE).result("Server jest wyłączony");
                return;
            }

            this.logger.debug("&b" + ip + "&r używa poprawnie endpointu&1 COMMAND");


            ctx.result("");
        });
    }
}