package me.indian.bds.rest.request.key;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.discord.jda.DiscordJDA;
import me.indian.bds.logger.Logger;
import me.indian.bds.rest.Request;
import me.indian.bds.rest.RestWebsite;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.watchdog.module.BackupModule;

public class BackupRequest implements Request {

    private final RestWebsite restWebsite;
    private final Logger logger;
    private final Javalin app;
    private final DiscordJDA discordJDA;
    private final BackupModule backupModule;

    public BackupRequest(final RestWebsite restWebsite, final BDSAutoEnable bdsAutoEnable) {
        this.restWebsite = restWebsite;
        this.logger = bdsAutoEnable.getLogger();
        this.app = this.restWebsite.getApp();
        this.discordJDA = bdsAutoEnable.getDiscordHelper().getDiscordJDA();
        this.backupModule = bdsAutoEnable.getWatchDog().getBackupModule();
    }

    @Override
    public void init() {
        this.app.get("/api/{api-key}/backup/{filename}", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            if (!this.restWebsite.checkApiKey(ctx)) return;

            final String filename = ctx.pathParam("filename");
            final String ip = ctx.ip();

            for (final Path path : this.backupModule.getBackups()) {
                final String fileName = path.getFileName().toString().replaceAll(".zip", "");
                if (filename.equalsIgnoreCase(fileName)) {

                    this.logger.info("&b" + ip + "&r pobiera&3 " + filename);
                    this.discordJDA.writeConsole("`" + ip + "` pobiera `" + filename + "`");

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
                        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType("application/json")
                                .result(exception.toString());
                    }
                    return;
                }
            }

            final List<String> backupsNames = this.backupModule.getBackupsNames();
            backupsNames.replaceAll(name -> name.replaceAll(".zip", ""));

            final String currentUrl = ctx.req().getRequestURL().toString().replaceAll(filename, "");
            ctx.status(HttpStatus.NOT_FOUND).contentType("application/json").result("Dostępne Backupy to: \n"
                    + MessageUtil.listToSpacedString(backupsNames) + "\n \n" +
                    "Użyj np: " + currentUrl + backupsNames.get(0)
            );
        });
    }
}