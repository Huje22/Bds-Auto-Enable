package me.indian.bds.event.server;

import me.indian.bds.event.ResponsibleEvent;

public class ConsoleCommandEvent extends ResponsibleEvent {

    private final String command;

    public ConsoleCommandEvent(final String command) {
        this.command = command;
    }

    public String getCommand() {
        return this.command;
    }
}
