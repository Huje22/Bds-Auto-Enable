package me.indian.bds.logger.impl;

import java.io.File;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;

public class MainLogger extends Logger {

    public MainLogger(final BDSAutoEnable bdsAutoEnable) {
        super(bdsAutoEnable);
    }

    @Override
    public void print() {
        super.print();
    }

    @Override
    public void print(final Object log) {
        super.print(log);
    }

    @Override
    public void print(final Object log, final Throwable throwable) {
        super.print(log, throwable);
    }

    @Override
    public void info(final Object log) {
        super.info(log);
    }

    @Override
    public void info(final Object log, final Throwable throwable) {
        super.info(log, throwable);
    }

    @Override
    public void warning(final Object log) {
        super.warning(log);
    }

    @Override
    public void warning(final Object log, final Throwable throwable) {
        super.warning(log, throwable);
    }

    @Override
    public void alert(final Object log) {
        super.alert(log);
    }

    @Override
    public void alert(final Object log, final Throwable throwable) {
        super.alert(log, throwable);
    }

    @Override
    public void critical(final Object log) {
        super.critical(log);
    }

    @Override
    public void critical(final Object log, final Throwable throwable) {
        super.critical(log, throwable);
    }

    @Override
    public void error(final Object log) {
        super.error(log);
    }

    @Override
    public void error(final Object log, final Throwable throwable) {
        super.error(log, throwable);
    }

    @Override
    public void debug(final Object log) {
        super.debug(log);
    }

    @Override
    public void debug(final Object log, final Throwable throwable) {
        super.debug(log, throwable);
    }

    @Override
    public void logByState(final Object log, final LogState logState) {
        super.logByState(log, logState);
    }

    @Override
    public void logByState(final Object log, final Throwable throwable, final LogState logState) {
        super.logByState(log, throwable, logState);
    }

    @Override
    public void instantLogToFile(final Object log) {
        super.instantLogToFile(log);
    }

    @Override
    public void logThrowable(final Throwable throwable) {
        super.logThrowable(throwable);
    }

    @Override
    public File getLogFile() {
        return super.getLogFile();
    }
}
