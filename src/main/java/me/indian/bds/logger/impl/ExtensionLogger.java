package me.indian.bds.logger.impl;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.DateUtil;

public class ExtensionLogger extends Logger {

    private final String extensionName;

    public ExtensionLogger(final BDSAutoEnable bdsAutoEnable, final String extensionName) {
        super(bdsAutoEnable);
        this.extensionName = extensionName;
    }

    @Override
    public void updatePrefix() {
        final String logStateColor = this.logState.getColorCode();
        this.prefix = "&a[" + DateUtil.getTimeHMSMS() + "] &e[&7" +
                Thread.currentThread().getName() + "&r&e] (&f" + this.extensionName + "&e) "
                + logStateColor + this.logState.name().toUpperCase() + " &r";
    }
}