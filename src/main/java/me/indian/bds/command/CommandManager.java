package me.indian.bds.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.defaults.AlertCommand;
import me.indian.bds.command.defaults.BackupCommand;
import me.indian.bds.command.defaults.ChatFormatCommand;
import me.indian.bds.command.defaults.EndCommand;
import me.indian.bds.command.defaults.ExtensionsCommand;
import me.indian.bds.command.defaults.HelpCommand;
import me.indian.bds.command.defaults.McLogCommand;
import me.indian.bds.command.defaults.MuteCommand;
import me.indian.bds.command.defaults.PacksCommand;
import me.indian.bds.command.defaults.ReloadCommand;
import me.indian.bds.command.defaults.RestartCommand;
import me.indian.bds.command.defaults.ServerPingCommand;
import me.indian.bds.command.defaults.SettingInfoCommand;
import me.indian.bds.command.defaults.StatsCommand;
import me.indian.bds.command.defaults.TPSCommand;
import me.indian.bds.command.defaults.TestCommand;
import me.indian.bds.command.defaults.TopCommand;
import me.indian.bds.command.defaults.VersionCommand;
import me.indian.bds.extension.Extension;
import me.indian.bds.player.PlayerStatistics;
import me.indian.bds.player.position.Position;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.ServerUtil;

public class CommandManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProcess serverProcess;
    private final Map<Command, Extension> commandMap;

    public CommandManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.commandMap = new LinkedHashMap<>();

        this.commandMap.put(new HelpCommand(this.commandMap), null);
        this.commandMap.put(new TPSCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new ExtensionsCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new EndCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new RestartCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new BackupCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new ReloadCommand(this.bdsAutoEnable), null);

        if (this.bdsAutoEnable.getWatchDog().getPackModule().isLoaded()) {
            this.commandMap.put(new TopCommand(this.bdsAutoEnable), null);
        }

        this.commandMap.put(new PacksCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new McLogCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new VersionCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new ChatFormatCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new MuteCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new AlertCommand(), null);
        this.commandMap.put(new SettingInfoCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new ServerPingCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new StatsCommand(), null);

        if (this.bdsAutoEnable.getAppConfigManager().getAppConfig().isDebug()) {
            this.commandMap.put(new TestCommand(this.bdsAutoEnable), null);
        }

        for (final Map.Entry<Command, Extension> entry : this.commandMap.entrySet()) {
            final Command command = entry.getKey();
            command.init(this.bdsAutoEnable);
        }
    }

    public <T extends Command> void registerCommand(final T command, final Extension extension) {
        if (extension == null) throw new NullPointerException("Rozszerzenie nie może być null");
        this.commandMap.put(command, extension);
        command.init(this.bdsAutoEnable);
    }

    public void unRegister(final Extension extension) {
        final List<Command> commandsToRemove = new ArrayList<>();

        this.commandMap.forEach((command, ex) -> {
            if (ex == extension) {
                commandsToRemove.add(command);
            }
        });

        commandsToRemove.forEach(this.commandMap::remove);
    }

    public boolean runCommands(final PlayerStatistics player, final String commandName, final String[] args, final Position position, final boolean isOp) {
        for (final Map.Entry<Command, Extension> entry : this.commandMap.entrySet()) {
            final Command command = entry.getKey();
            if (command.getName().equalsIgnoreCase(commandName) || command.isAlias(commandName)) {
                command.setPlayer(player);
                command.setPosition(position);

                if (!command.onExecute(args, this.isOp(player, isOp)) && !command.getUsage().isEmpty()) {
                    if (player != null) {
                        ServerUtil.tellrawToPlayer(player.getPlayerName(), command.getUsage());
                    } else {
                        this.bdsAutoEnable.getLogger().print(command.getUsage());
                    }
                }
                return true;
            }
        }

        if (player != null) {
            ServerUtil.tellrawToPlayer(player.getPlayerName(), "&cNie znaleziono takiego polecenia");
            ServerUtil.playSoundToPlayer(player.getPlayerName(), "random.break");
        }

        return false;
    }

    private boolean isOp(final PlayerStatistics playerStatistics, final boolean isOp) {
        if (isOp || playerStatistics.getPlayerName().equalsIgnoreCase("CONSOLE")) return true;
        return this.bdsAutoEnable.getAppConfigManager().getAppConfig().getAdmins().contains(playerStatistics.getPlayerName());
    }
}