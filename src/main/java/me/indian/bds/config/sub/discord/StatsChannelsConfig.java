package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class StatsChannelsConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Kanał (voice) gdzie bedzie pokazane ile graczy jest online na serwerze, Zostaw puste aby nie uruchamiać"})
    private long onlinePlayersID = 1L;
    @Comment({""})
    @Comment({"Kanał (voice) gdzie bedzie pokazywana ostatnia liczbą TPS, Zostaw puste aby nie uruchamiać"})
    private long tpsID = 1L;
    @Comment({""})
    @Comment({"Nazwa kanału głosowego z liczbą TPS"})
    private String tpsName = "TPS: <tps>";

    @Comment({""})
    @Comment({"Nazwa kanału głosowego z liczbą graczy online , zmienia się ona co 30s "})
    private String onlinePlayersName = "Gracze online <online> / <max>";

    public long getOnlinePlayersID() {
        return this.onlinePlayersID;
    }

    public long getTpsID() {
        return this.tpsID;
    }

    public String getTpsName() {
        return this.tpsName;
    }

    public String getOnlinePlayersName() {
        return this.onlinePlayersName;
    }
}