package me.indian.bds.event.server;

import me.indian.bds.event.ResponsibleEvent;
import org.jetbrains.annotations.Nullable;

public class ServerConsoleCommandEvent extends ResponsibleEvent {

    private final String command;
    private final String[] args;

    public ServerConsoleCommandEvent(final String command, final String[] args) {
        this.command = command;
        this.args = args;
    }

    public ServerConsoleCommandEvent(final String command) {
        this.command = command;
        this.args = null;
    }

    public String getCommand() {
        return this.command;
    }

    @Nullable
    public String[] getArgs() {
        return this.args;
    }
}
