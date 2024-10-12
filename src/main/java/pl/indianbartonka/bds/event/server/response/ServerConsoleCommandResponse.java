package pl.indianbartonka.bds.event.server.response;

import pl.indianbartonka.bds.event.EventResponse;

public class ServerConsoleCommandResponse extends EventResponse {

    private final Runnable actionToDo;

    public ServerConsoleCommandResponse(final Runnable actionToDo) {
        this.actionToDo = actionToDo;
    }

    public Runnable getActionToDo() {
        return this.actionToDo;
    }
}