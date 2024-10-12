package pl.indianbartonka.bds.logger;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.util.DefaultsVariables;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.logger.LoggerConfiguration;

public class MainLogger extends Logger {

    public MainLogger(final BDSAutoEnable bdsAutoEnable) {
        super(new LoggerConfiguration(bdsAutoEnable.getAppConfigManager().getAppConfig().isDebug(),
                DefaultsVariables.getLogsDir(), "ServerLog-" + bdsAutoEnable.getRunDate()));
    }
}