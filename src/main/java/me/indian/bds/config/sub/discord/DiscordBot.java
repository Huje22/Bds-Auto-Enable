package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import java.util.Arrays;
import java.util.List;

public class DiscordBot extends OkaeriConfig {

    private String token = "";
    private String prefix = "$";
    private long channelID = 1L;
    private long consoleID = 1L;
    private long serverID = 1L;
    private List<String> ipMessage = Arrays.asList("Nasze IP: 127.0.0.1", "Nasz Port: 19132");

    public String getToken() {
        return this.token;
    }

    public String getPrefix() {
        return this.prefix;
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
}
