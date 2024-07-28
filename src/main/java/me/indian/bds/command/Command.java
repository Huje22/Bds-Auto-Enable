package me.indian.bds.command;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.CommandConfig;
import me.indian.bds.player.PlayerStatistics;
import me.indian.bds.player.position.Position;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ServerUtil;
import org.jetbrains.annotations.Nullable;

public abstract class Command {

    private final String name, description;
    private final List<String> alliases;
    private final Map<String, String> commandOptions;
    protected PlayerStatistics player;
    protected CommandConfig commandConfig;
    private Position position;
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

    @Nullable
    public Position getPosition() {
        return this.position;
    }

    public final void setPosition(final Position position) {
        this.position = position;
    }

    protected void deniedSound() {
        ServerUtil.playSoundToPlayer(this.player.getPlayerName(), this.commandConfig.getDeniedSound());
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

    public final void setPlayer(final PlayerStatistics player) {
        this.player = player;
    }

    protected final void sendMessage(final String message) {
        final ServerProcess serverProcess = this.bdsAutoEnable.getServerProcess();
        if (this.player != null) {
            ServerUtil.tellrawToPlayer(this.player.getPlayerName(), message);
        } else {
            this.bdsAutoEnable.getLogger().print(message);
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
                ", player= " + this.player +
                ", alliases= " + this.alliases +
                ", commandOptions= " + this.commandOptions +
                ")";
    }
}