package me.indian.bds.command;


import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.CommandsConfig;
import me.indian.bds.server.ServerProcess;

public abstract class Command {

    private final String name, description, usage;
    private String playerName;
    private BDSAutoEnable bdsAutoEnable;
    public CommandSender commandSender;
    public CommandsConfig commandsConfig;

    public Command(final String name, final String description) {
        this(name, description, "");
    }

    public Command(final String name, final String description, final String usage) {
        this.name = "!" + name;
        this.description = description;
        this.usage = usage;
    }

    public abstract boolean onExecute(String[] args, boolean isOp);

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getUsage() {
        return this.usage;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(final String playerName) {
        this.playerName = playerName;
    }

    public final void setCommandSender(final CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    public final void sendMessage(final String message) {
        final ServerProcess serverProcess = this.bdsAutoEnable.getServerProcess();
        switch (this.commandSender) {
            case CONSOLE -> this.bdsAutoEnable.getLogger().print(message);
            case PLAYER -> serverProcess.tellrawToPlayer(this.playerName, message);
        }
    }

    public final void init(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.commandsConfig = bdsAutoEnable.getAppConfigManager().getCommandsConfig();
    }

    @Override
    public String toString() {
        return "Command(name=" + this.name +
                ", description=" + this.description +
                ", playerName= " + this.playerName +
                ", commandSender= " + this.commandSender +
                ")";
    }
}