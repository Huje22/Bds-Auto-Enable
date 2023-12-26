package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import java.util.Arrays;
import java.util.List;

public class BotConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Zostaw puste aby nie uruchamiać "})
    private String token = "";

    @Comment({""})
    @Comment({"ID servera discord"})
    private long serverID = 1L;

    @Comment({""})
    @Comment({"ID kanału czatu"})
    private long channelID = 1L;

    @Comment({""})
    @Comment({"Kanał na który zostaną wysyłane wiadomości z konsoli minecraft , Zostaw puste aby nie uruchamiać "})
    private long consoleID = 1L;

    @Comment({""})
    @Comment({""})
    private LinkingConfig linkingConfig = new LinkingConfig();

    @Comment({""})
    @Comment({"Ustawienia kanałów statystyk"})
    @CustomKey("statsChannels")
    private StatsChannelsConfig statsChannelsConfig = new StatsChannelsConfig();

    @Comment({""})
    @Comment({"Czy pokazać więcej informacji o graczu online w /list ? "})
    private boolean advancedPlayerList = true;

    @Comment({""})
    @Comment({"Opuść wszystkie inne servery przy starcie bota "})
    private boolean leaveServers = false;

    @Comment({""})
    @Comment({"Info po wpisaniu /ip"})
    private List<String> ipMessage = Arrays.asList("Nasze IP: 127.0.0.1", "Nasz Port: 19132");

    @Comment({""})
    @Comment({"Pamiętaj że oznaczenie kogoś zawiera jego ID a ono jest długie!"})
    private int allowedLength = 500;

    @Comment({""})
    @Comment({"Czy usunąć wiadomość po przekroczeniu liczby znaków?"})
    private boolean deleteOnReachLimit = false;

    @Comment({""})
    @Comment({"Informacja o przekroczeniu liczby znaków (na pv)"})
    private String reachedMessage = "Osiągnięto dozwoloną ilość znaków!";

    @Comment({""})
    @Comment({"Aktywność , aktualizowana co 10min"})
    @Comment({"Dostępne aktywności:  PLAYING, STREAMING, LISTENING, WATCHING, COMPETING"})
    private String activity = "PLAYING";

    @Comment({""})
    @Comment({"Wiadomość w statusie bota"})
    @Comment({"<time> - czas w minutach przez jaki server jest online"})
    private String activityMessage = "Minecraft <time>";

    @Comment({""})
    @Comment({"URL do stream "})
    private String streamUrl = "https://www.youtube.com/@IndianBartonka?sub_confirmation=1";

    public String getToken() {
        return this.token;
    }

    public long getServerID() {
        return this.serverID;
    }

    public long getChannelID() {
        return this.channelID;
    }

    public long getConsoleID() {
        return this.consoleID;
    }

    public LinkingConfig getLinkingConfig() {
        return this.linkingConfig;
    }

    public StatsChannelsConfig getStatsChannelsConfig() {
        return this.statsChannelsConfig;
    }

    public boolean isAdvancedPlayerList() {
        return this.advancedPlayerList;
    }

    public boolean isLeaveServers() {
        return this.leaveServers;
    }

    public List<String> getIpMessage() {
        return this.ipMessage;
    }

    public int getAllowedLength() {
        return this.allowedLength;
    }

    public boolean isDeleteOnReachLimit() {
        return this.deleteOnReachLimit;
    }

    public String getReachedMessage() {
        return this.reachedMessage;
    }

    public String getActivity() {
        return this.activity;
    }

    public String getActivityMessage() {
        return this.activityMessage;
    }

    public String getStreamUrl() {
        return this.streamUrl;
    }
}