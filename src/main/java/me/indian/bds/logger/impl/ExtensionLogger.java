package me.indian.bds.logger.impl;

import me.indian.bds.BDSAutoEnable;
import me.indian.util.DateUtil;
import me.indian.util.logger.Logger;

public class ExtensionLogger extends Logger {

    private final String extensionPrefix;

    public ExtensionLogger(final BDSAutoEnable bdsAutoEnable, final String extensionPrefix) {
        super(bdsAutoEnable.getLogger());
        this.extensionPrefix = extensionPrefix;
    }

    @Override
    protected void updatePrefix() {
        final String logStateColor = this.logState.getColorCode();
        this.prefix = "&a[" + DateUtil.getTimeHMSMS() + "] &e[&7" +
                Thread.currentThread().getName() + "&r&e] (&f" + this.extensionPrefix + "&e) "
                + logStateColor + this.logState.name().toUpperCase() + " &r";
    }
}