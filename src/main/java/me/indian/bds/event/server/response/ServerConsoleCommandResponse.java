package me.indian.bds.event.server.response;

import me.indian.bds.event.EventResponse;

public class ServerConsoleCommandResponse extends EventResponse {

    private final Runnable actionToDo;

    public ServerConsoleCommandResponse(final Runnable actionToDo) {
        this.actionToDo = actionToDo;
    }

    public Runnable getActionToDo() {
        return this.actionToDo;
    }
}