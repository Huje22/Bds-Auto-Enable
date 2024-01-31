package me.indian.bds.event.player.response;

import me.indian.bds.event.EventResponse;

public class PlayerChatResponse extends EventResponse {

    private final String format;

    public PlayerChatResponse(final String format){
        this.format = format;
    }

    public String getFormat() {
        return this.format;
    }
}