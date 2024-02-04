package me.indian.bds.event.server.response;

import me.indian.bds.event.EventResponse;

public class ConsoleCommandResponse extends EventResponse {

    private final Runnable actionToDo;

    public ConsoleCommandResponse(final Runnable actionToDo) {
        this.actionToDo = actionToDo;
    }

    public Runnable getActionToDo() {
        return this.actionToDo;
    }
}