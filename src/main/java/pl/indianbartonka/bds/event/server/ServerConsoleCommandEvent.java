package pl.indianbartonka.bds.event.server;

import org.jetbrains.annotations.Nullable;
import pl.indianbartonka.bds.event.ResponsibleEvent;

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
