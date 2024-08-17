package me.indian.bds.logger.impl;

import java.nio.file.Path;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.util.DefaultsVariables;
import me.indian.util.logger.Logger;
import me.indian.util.logger.LoggerConfiguration;

public class MainLogger extends Logger {

    public MainLogger(final BDSAutoEnable bdsAutoEnable) {
        super(new LoggerConfiguration(bdsAutoEnable.getAppConfigManager().getAppConfig().isDebug(),
                Path.of(DefaultsVariables.getLogsDir()), bdsAutoEnable.getRunDate()));
    }
}