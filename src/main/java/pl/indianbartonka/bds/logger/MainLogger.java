package pl.indianbartonka.bds.logger;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.util.DefaultsVariables;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.logger.config.LoggerConfiguration;

public class MainLogger extends Logger {

    public MainLogger(final BDSAutoEnable bdsAutoEnable) {
        super(LoggerConfiguration.builder()
                .setLogName("ServerLog-" + bdsAutoEnable.getRunDate())
                .setLogsPath(DefaultsVariables.getLogsDir())
                .setDebug(bdsAutoEnable.getAppConfigManager().getAppConfig().isDebug())
                .setOneLog(false)
                .setLoggingToFile(true)
                .setLogJULtoFile(true)
                .build());
    }
}