package me.indian.bds.logger.impl;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.util.DefaultsVariables;
import me.indian.util.logger.Logger;
import me.indian.util.logger.LoggerConfiguration;

public class MainLogger extends Logger {

    public MainLogger(final BDSAutoEnable bdsAutoEnable) {
        super(new LoggerConfiguration(bdsAutoEnable.getAppConfigManager().getAppConfig().isDebug(),
                DefaultsVariables.getLogsDir(), "ServerLog-" + bdsAutoEnable.getRunDate()));
    }
}