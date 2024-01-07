package me.indian.bds.rest.post;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.discord.DiscordLogChannelType;
import me.indian.bds.logger.Logger;
import me.indian.bds.rest.Request;
import me.indian.bds.rest.RestWebsite;
import me.indian.bds.rest.component.CommandRequestData;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.GsonUtil;

public class CommandPostRequest implements Request {

    private final RestWebsite restWebsite;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private final Javalin app;

    public CommandPostRequest(final RestWebsite restWebsite, final BDSAutoEnable bdsAutoEnable) {
        this.restWebsite = restWebsite;
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.app = this.restWebsite.getApp();
    }

    @Override
    public void init() {
        this.app.post("/command/{api-key}", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            if (!this.restWebsite.checkApiKey(ctx)) return;

            final String ip = ctx.ip();
            final String requestBody = ctx.body();
            final CommandRequestData commandRequestData = GsonUtil.GSON.fromJson(requestBody, CommandRequestData.class);
            final String command = commandRequestData.command();

            if (command == null) {
                this.logger.debug("&b" + ip + "&r wysła niepoprawny json&1 " + requestBody.replaceAll("\n", ""));
                ctx.status(HttpStatus.BAD_REQUEST).result("Niepoprawny Json! " + requestBody.replaceAll("\n", ""));
                return;
            }

            if (!this.serverProcess.isEnabled()) {
                ctx.status(HttpStatus.SERVICE_UNAVAILABLE).result("Server jest wyłączony");
                return;
            }

            this.logger.debug("&b" + ip + "&r używa poprawnie endpointu&1 COMMAND");
            this.logger.print(command, this.bdsAutoEnable.getDiscordHelper().getDiscordJDA(), DiscordLogChannelType.CONSOLE);
            ctx.result("Ostatnia linia z konsoli: " + this.serverProcess.commandAndResponse(command));
        });
    }
}