package me.indian.bds.command;


import java.util.HashMap;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.CommandsConfig;
import me.indian.bds.server.ServerProcess;

public abstract class Command {

    private final String name, description;
    private final Map<String, String> commandOptions;
    protected String playerName;
    protected CommandSender commandSender;
    protected CommandsConfig commandsConfig;
    private BDSAutoEnable bdsAutoEnable;

    public Command(final String name, final String description) {
        this.name = "!" + name;
        this.description = description;
        this.commandOptions = new HashMap<>();
    }

    public abstract boolean onExecute(String[] args, boolean isOp);

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    protected void addOption(final String option) {
        this.commandOptions.put(option, "");
    }

    protected void addOption(final String option, final String description) {
        this.commandOptions.put(option, description);
    }

    protected void buildHelp() {
        for (final Map.Entry<String, String> option : this.commandOptions.entrySet()) {
            this.sendMessage("&a" + option.getKey() + "&4 - &b" + option.getValue());
        }
        this.sendMessage("&b[]&4 -&a Opcjonalne &e<>&4 -&a Wymagane");
    }

    public String getUsage() {
        String usage = "";
        int counter = 0;
        for (final Map.Entry<String, String> option : this.commandOptions.entrySet()) {
            usage += option.getKey() + (counter < this.commandOptions.size() - 1 ? ", " : "");
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
                ", commandOptions= "+ this.commandOptions +
                ")";
    }
}