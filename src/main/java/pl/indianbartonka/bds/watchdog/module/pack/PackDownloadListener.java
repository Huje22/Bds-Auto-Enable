package pl.indianbartonka.bds.watchdog.module.pack;

import java.io.File;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.download.DownloadListener;
import pl.indianbartonka.util.logger.Logger;

public class PackDownloadListener implements DownloadListener {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;

    public PackDownloadListener(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = bdsAutoEnable.getLogger().tempLogger("PackDownloader");
    }

    @Override
    public void onStart(final int definedBuffer, final long fileSize, final File outputFile) {
        this.logger.info("Pobieranie paczki");
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
        this.logger.info("&aPobrano paczke");
        this.bdsAutoEnable.setAppWindowName("Pobrano wersje paczke");
    }

    @Override
    public void onDownloadStop() {
        this.logger.alert("Zatrzymano pobieranie!");
    }
}
