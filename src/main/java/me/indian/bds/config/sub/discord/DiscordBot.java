package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import java.util.Arrays;
import java.util.List;

public class DiscordBot extends OkaeriConfig {

    private String token = "";
    private long channelID = 1L;
    private long consoleID = 1L;
    private long serverID = 1L;
    private List<String> ipMessage = Arrays.asList("Nasze IP: 127.0.0.1", "Nasz Port: 19132");
    @Comment({""})
    @Comment({"Pamiętaj że oznaczenie kogoś zawiera jego ID a ono jest długie!"})
    private int allowedLength = 200;
    private boolean deleteOnReachLimit = true;
    private String reachedMessage = "Osiągnięto dozwoloną ilosc znaków!";


    public String getToken() {
        return this.token;
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
