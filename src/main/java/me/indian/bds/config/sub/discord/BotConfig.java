package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import java.util.Arrays;
import java.util.List;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class BotConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy włączyć bota"})
    private boolean enable = true;
    @Comment({""})
    @Comment({"Zostaw puste aby nie uruchamiać "})
    private String token = "";

    @Comment({""})
    @Comment({"Poczytaj o tych flagach tutaj"})
    @Comment({"https://github.com/discord-jda/JDA/wiki/Gateway-Intents-and-Member-Cache-Policy/#cacheflags"})
    private List<CacheFlag> enableCacheFlag = Arrays.asList(
            CacheFlag.EMOJI,
            CacheFlag.CLIENT_STATUS,
            CacheFlag.ACTIVITY,
            CacheFlag.CLIENT_STATUS,
            CacheFlag.ONLINE_STATUS
    );

    private List<CacheFlag> disableCacheFlag = Arrays.asList(
            CacheFlag.VOICE_STATE,
            // CacheFlag.ACTIVITY,
            // CacheFlag.CLIENT_STATUS,
            // CacheFlag.ONLINE_STATUS,
            CacheFlag.SCHEDULED_EVENTS
    );

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
    @Comment({"Dostępne aktywności:  PLAYING, STREAMING, LISTENING, WATCHING, CUSTOM_STATUS, COMPETING"})
    private Activity.ActivityType activity = Activity.ActivityType.PLAYING;

    @Comment({""})
    @Comment({"Wiadomość w statusie bota"})
    @Comment({"<time> - czas w minutach przez jaki server jest online"})
    private String activityMessage = "Minecraft <time>";

    @Comment({""})
    @Comment({"URL do stream "})
    private String streamUrl = "https://www.youtube.com/@IndianBartonka?sub_confirmation=1";


    public boolean isEnable() {
        return this.enable;
    }

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

    public List<CacheFlag> getEnableCacheFlag() {
        return this.enableCacheFlag;
    }

    public List<CacheFlag> getDisableCacheFlag() {
        return this.disableCacheFlag;
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

    public Activity.ActivityType getActivity() {
        return this.activity;
    }

    public void setActivity(final Activity.ActivityType activity) {
        this.activity = activity;
    }

    public String getActivityMessage() {
        return this.activityMessage;
    }

    public void setActivityMessage(final String activityMessage) {
        this.activityMessage = activityMessage;
    }

    public String getStreamUrl() {
        return this.streamUrl;
    }
}