package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import java.util.Arrays;
import java.util.List;

public class DiscordBot extends OkaeriConfig {

    @Comment({""})
    @Comment({"Zostaw puste aby nie uruchamiać "})
    private String token = "";
    @Comment({""})
    private long channelID = 1L;
    @Comment({""})
    @Comment({"Zostaw puste aby nie uruchamiać "})
    private long consoleID = 1L;
    @Comment({""})
    private long serverID = 1L;
    @Comment({""})
    private List<String> ipMessage = Arrays.asList("Nasze IP: 127.0.0.1", "Nasz Port: 19132");
    @Comment({""})
    @Comment({"Pamiętaj że oznaczenie kogoś zawiera jego ID a ono jest długie!"})
    private int allowedLength = 200;
    private boolean deleteOnReachLimit = true;
    private String reachedMessage = "Osiągnięto dozwoloną ilosc znaków!";
    @Comment({""})
    @Comment({"Aktywność"})
    @Comment({"Dostępne aktywności:  PLAYING, STREAMING, LISTENING, WATCHING, COMPETING"})
    private String activity = "PLAYING";
    private String activityMessage = "Minecraft";
    private String streamUrl = "hhttps://www.youtube.com/@IndianBartonka?sub_confirmation=1";


    public String getToken() {
        return this.token;
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

    public long getChannelID() {
        return this.channelID;
    }

    public long getConsoleID() {
        return this.consoleID;
    }

    public long getServerID() {
        return this.serverID;
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
}
