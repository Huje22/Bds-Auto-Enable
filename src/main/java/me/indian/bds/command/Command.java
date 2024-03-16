package me.indian.bds.command;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.CommandConfig;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;

public abstract class Command {

    private final String name, description;
    private final List<String> alliases;
    private final Map<String, String> commandOptions;
    protected String playerName;
    protected CommandSender commandSender;
    protected CommandConfig commandConfig;
    private BDSAutoEnable bdsAutoEnable;

    public Command(final String name, final String description) {
        this.name = "!" + name;
        this.description = description;
        this.alliases = new ArrayList<>();
        this.commandOptions = new LinkedHashMap<>();
    }

    public abstract boolean onExecute(String[] args, boolean isOp);

    public final String getName() {
        return this.name;
    }

    public final String getDescription() {
        return this.description;
    }

    public boolean isAlias(final String command) {
        return this.alliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(command));
    }

    public final void addAlliases(final List<String> alliases) {
        alliases.forEach(alias -> this.alliases.add("!" + alias));
    }

    protected final void addOption(final String option) {
        this.commandOptions.put(option, "");
    }

    protected final void addOption(final String option, final String description) {
        this.commandOptions.put(option, description);
    }

    protected final void buildHelp() {
        if (!this.alliases.isEmpty()) {
            this.sendMessage("&aAlliasy:&b " + MessageUtil.stringListToString(this.alliases, "&a,&b "));
        }
        for (final Map.Entry<String, String> option : this.commandOptions.entrySet()) {
            this.sendMessage("&a" + option.getKey() + "&4 - &b" + option.getValue());
        }
        this.sendMessage("&b[]&4 -&a Opcjonalne &e<>&4 -&a Wymagane");
    }

    public final String getUsage() {
        String usage = "";
        int counter = 0;
        for (final Map.Entry<String, String> option : this.commandOptions.entrySet()) {
            usage += option.getKey() + (counter < this.commandOptions.size() - 1 ? ", " : "");
            counter++;
        }

        if (usage.isEmpty()) return "";

        return "&a" + this.name + "&4 -&b " + usage;
    }

    public final void setPlayerName(final String playerName) {
        this.playerName = playerName;
    }

    public final void setCommandSender(final CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    protected final void sendMessage(final String message) {
        final ServerProcess serverProcess = this.bdsAutoEnable.getServerProcess();
        switch (this.commandSender) {
            case CONSOLE -> this.bdsAutoEnable.getLogger().print(message);
            case PLAYER -> serverProcess.tellrawToPlayer(this.playerName, message);
        }
    }

    public final void init(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.commandConfig = bdsAutoEnable.getAppConfigManager().getCommandsConfig();
    }

    @Override
    public final String toString() {
        return "Command(name=" + this.name +
                ", description=" + this.description +
                ", playerName= " + this.playerName +
                ", commandSender= " + this.commandSender +
                ", alliases= " + this.alliases +
                ", commandOptions= " + this.commandOptions +
                ")";
    }
}