package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class StatsChannelsConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Kanał (voice) gdzie bedzie pokazane ile graczy jest online na serwerze, Zostaw puste aby nie uruchamiać"})
    private long onlinePlayersID = 1L;
    @Comment({""})
    @Comment({"Nazwa kanału głosowego z liczbą graczy online , zmienia się ona co 30s "})
    private String onlinePlayersMessage = "Gracze online <online> / <max>";

    public long getOnlinePlayersID() {
        return this.onlinePlayersID;
    }

    public String getOnlinePlayersMessage() {
        return this.onlinePlayersMessage;
    }
}