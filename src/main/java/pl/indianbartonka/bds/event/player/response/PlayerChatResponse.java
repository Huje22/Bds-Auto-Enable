package pl.indianbartonka.bds.event.player.response;

import pl.indianbartonka.bds.event.EventResponse;

public class PlayerChatResponse extends EventResponse {

    private final String format;
    private final boolean cancel;

    public PlayerChatResponse(final String format, final boolean cancel) {
        this.format = format;
        this.cancel = cancel;
    }

    public String getFormat() {
        return this.format;
    }

    public boolean isCanceled() {
        return this.cancel;
    }
}
