package me.indian.bds;

import gs.mclo.api.MclogsClient;
import gs.mclo.api.response.UploadLogResponse;
import java.util.concurrent.CompletableFuture;
import me.indian.bds.config.sub.log.LogConfig;
import me.indian.bds.logger.Logger;

public class McLog {

    private final Logger logger;
    private final LogConfig logConfig;
    private final MclogsClient mclogsClient;

    public McLog(final BDSAutoEnable bdsAutoEnable) {
        this.logger = bdsAutoEnable.getLogger();
        this.logConfig = bdsAutoEnable.getAppConfigManager().getLogConfig();
        this.mclogsClient = new MclogsClient("BDS-Auto-Enable", bdsAutoEnable.getProjectVersion(), bdsAutoEnable.getVersionManager().getLoadedVersion());
    }

    public void sendCurrentLog() {
        if (this.logConfig.isSendLogs()) {
            try {
                // share the log file
                final CompletableFuture<UploadLogResponse> future = this.mclogsClient.uploadLog(this.logger.getLogFile().toPath());

                this.logger.info("&aWysłano logi servera do&b mclo.gs&a link:&1 " + future.get().getUrl());
            } catch (final Exception exception) {
                this.logger.error("&cNie udało się przekazać logów servera do&b mclo.gs", exception);
            }
        } else {
            this.logger.info("&cWysyłąnie logów na&b https://mclo.gs/&c jest wyłączone");
        }
    }
}