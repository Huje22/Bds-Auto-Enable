package me.indian.bds.command;


import java.util.ArrayList;
import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.CommandsConfig;
import me.indian.bds.server.ServerProcess;

public abstract class Command {

    private final String name, description;
    private final List<String> options;
    protected String playerName;
    protected CommandSender commandSender;
    protected CommandsConfig commandsConfig;
    private BDSAutoEnable bdsAutoEnable;

    public Command(final String name, final String description) {
        this.name = "!" + name;
        this.description = description;
        this.options = new ArrayList<>();
    }

    public abstract boolean onExecute(String[] args, boolean isOp);

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    protected void addOption(final String option) {
        this.options.add(option);
    }

    public String getUsage() {
        String usage = "";
        int counter = 0;
        for (final String option : this.options) {
            usage += option + (counter < this.options.size() - 1 ? ", " : "");
            counter++;
        }

        if (usage.isEmpty()) return "";

        return "&a" + this.name + "&4 -&b " + usage;
    }

    public void setPlayerName(final String playerName) {
        this.playerName = playerName;
    }

    public final void setCommandSender(final CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    protected final void sendMessage(final String message) {
        final ServerProcess serverProcess = this.bdsAutoEnable.getServerProcess();
        if(message.isEmpty()) return;
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