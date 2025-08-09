package pl.indianbartonka.bds.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.defaults.admin.AlertCommand;
import pl.indianbartonka.bds.command.defaults.admin.BackupCommand;
import pl.indianbartonka.bds.command.defaults.admin.ChatFormatCommand;
import pl.indianbartonka.bds.command.defaults.admin.EndCommand;
import pl.indianbartonka.bds.command.defaults.system.ExecuteCommand;
import pl.indianbartonka.bds.command.defaults.info.ExtensionsCommand;
import pl.indianbartonka.bds.command.defaults.system.HelpCommand;
import pl.indianbartonka.bds.command.defaults.admin.McLogCommand;
import pl.indianbartonka.bds.command.defaults.admin.MuteCommand;
import pl.indianbartonka.bds.command.defaults.info.PacksCommand;
import pl.indianbartonka.bds.command.defaults.info.PlayerInfoCommand;
import pl.indianbartonka.bds.command.defaults.info.PlayerListCommand;
import pl.indianbartonka.bds.command.defaults.admin.ReloadCommand;
import pl.indianbartonka.bds.command.defaults.admin.RestartCommand;
import pl.indianbartonka.bds.command.defaults.info.ServerPingCommand;
import pl.indianbartonka.bds.command.defaults.info.SettingInfoCommand;
import pl.indianbartonka.bds.command.defaults.info.StatsCommand;
import pl.indianbartonka.bds.command.defaults.info.TPSCommand;
import pl.indianbartonka.bds.command.defaults.TestCommand;
import pl.indianbartonka.bds.command.defaults.info.TopCommand;
import pl.indianbartonka.bds.command.defaults.info.VersionCommand;
import pl.indianbartonka.bds.extension.Extension;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.player.position.Position;
import pl.indianbartonka.bds.util.ServerUtil;

public class CommandManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Map<Command, Extension> commandMap;

    public CommandManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.commandMap = new LinkedHashMap<>();

        this.commandMap.put(new HelpCommand(this.commandMap), null);
        this.commandMap.put(new ExecuteCommand(), null);
        this.commandMap.put(new TPSCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new ExtensionsCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new EndCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new RestartCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new BackupCommand(this.bdsAutoEnable), null);
        this.commandMap.put(new ReloadCommand(this.bdsAutoEnable), null);

        if (this.bdsAutoEnable.getWatchDog().getPackModule().isLoaded()) {
            this.commandMap.put(new TopCommand(this.bdsAutoEnable), null);
            this.commandMap.put(new PlayerInfoCommand(this.bdsAutoEnable), null);
            this.commandMap.put(new PlayerListCommand(this.bdsAutoEnable), null);
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
            this.commandMap.put(new TestCommand(), null);
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
                        this.bdsAutoEnable.getLogger().println(command.getUsage());
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