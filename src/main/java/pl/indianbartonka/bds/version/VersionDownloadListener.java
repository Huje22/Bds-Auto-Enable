package pl.indianbartonka.bds.version;

import java.io.File;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.download.DownloadListener;
import pl.indianbartonka.util.logger.Logger;

public class VersionDownloadListener implements DownloadListener {

    private final BDSAutoEnable bdsAutoEnable;
    private Logger logger;
    private String version;

    public VersionDownloadListener(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
    }

    @Override
    public void onStart(final int definedBuffer, final File outputFile) {
        this.logger.info("Pobieranie wersji: &1" + this.version);
        this.logger.info("Ustalony buffer dla naszego pliku to:&a " + MathUtil.formatBytesDynamic(definedBuffer, false));
    }

    @Override
    public void onSecond(final int progress, final double formatedSpeed, final String remainingTimeString) {
        this.bdsAutoEnable.setAppWindowName(String.format("%-1s %-2s %-4s %-4s", "Pobrano w:", progress + "%", formatedSpeed + " MB/s", "Pozostało " + remainingTimeString));
    }

    @Override
    public void onProgress(final int progress, final double formatedSpeed, final String remainingTimeString) {
        this.logger.info(String.format("%-1s &b%-2s &a%-4s &d%-4s", "Pobrano w:", progress + "%", formatedSpeed + " MB/s", "Pozostało " + remainingTimeString));
    }

    @Override
    public void onTimeout(final int timeOutSeconds) {
        this.logger.info("TimeOut");
    }

    @Override
    public void onEnd(final File outputFile) {
        this.logger.info("Pobrano wersje: &1" + this.version);
        this.bdsAutoEnable.setAppWindowName("Pobrano wersje: " + this.version);
    }

    @Override
    public void onDownloadStop() {
        this.logger.alert("Zatrzymano pobieranie!");
    }

    public void setVersion(final String version) {
        this.version = version;
        this.logger = this.bdsAutoEnable.getLogger().tempLogger(version);
    }
}