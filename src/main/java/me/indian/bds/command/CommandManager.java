package me.indian.bds.command;

import java.util.HashSet;
import java.util.Set;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.defaults.BackupCommand;
import me.indian.bds.command.defaults.ChatFormatCommand;
import me.indian.bds.command.defaults.EndCommand;
import me.indian.bds.command.defaults.ExtensionsCommand;
import me.indian.bds.command.defaults.HelpCommand;
import me.indian.bds.command.defaults.MuteCommand;
import me.indian.bds.command.defaults.RestartCommand;
import me.indian.bds.command.defaults.ServerPingCommand;
import me.indian.bds.command.defaults.SettingInfoCommand;
import me.indian.bds.command.defaults.StatsCommand;
import me.indian.bds.command.defaults.TPSCommand;
import me.indian.bds.command.defaults.TestCommand;
import me.indian.bds.command.defaults.TopCommand;
import me.indian.bds.command.defaults.VersionCommand;
import me.indian.bds.server.ServerProcess;

public class CommandManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProcess serverProcess;
    private final  Set<Command> commandSet ;

    public CommandManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.commandSet = new HashSet<>();

        this.registerCommand(new HelpCommand(this.commandSet));
        this.registerCommand(new TPSCommand(this.bdsAutoEnable));
        this.registerCommand(new ExtensionsCommand(this.bdsAutoEnable));
        this.registerCommand(new EndCommand(this.bdsAutoEnable));
        this.registerCommand(new RestartCommand(this.bdsAutoEnable));
        this.registerCommand(new BackupCommand(this.bdsAutoEnable));

        if(this.bdsAutoEnable.getWatchDog().getPackModule().isLoaded()) {
            this.registerCommand(new TopCommand(this.bdsAutoEnable));
        }

        this.registerCommand(new VersionCommand(this.bdsAutoEnable));
        this.registerCommand(new ChatFormatCommand(this.bdsAutoEnable));
        this.registerCommand(new MuteCommand(this.bdsAutoEnable));
        this.registerCommand(new SettingInfoCommand(this.bdsAutoEnable));
        this.registerCommand(new ServerPingCommand(this.bdsAutoEnable));
        this.registerCommand(new StatsCommand());

        if (this.bdsAutoEnable.getAppConfigManager().getAppConfig().isDebug()) {
            this.registerCommand(new TestCommand(this.bdsAutoEnable));
        }
    }

    public <T extends Command> void registerCommand(final T command) {
        this.commandSet.add(command);
        command.init(this.bdsAutoEnable);
    }

    public boolean runCommands(final CommandSender sender, final String playerName, final String commandName, final String[] args, final boolean isOp) {
        for (final Command command : this.commandSet) {
            if (command.getName().equalsIgnoreCase(commandName) || command.isAlias(commandName)) {
                command.setCommandSender(sender);
                command.setPlayerName(playerName);

                if (!command.onExecute(args, this.isOp(playerName)) && !command.getUsage().isEmpty()) {
                    switch (sender) {
                        case CONSOLE -> this.bdsAutoEnable.getLogger().print(command.getUsage());
                        case PLAYER -> this.serverProcess.tellrawToPlayer(playerName, command.getUsage());
                    }
                }
                return true;
            }
        }

        if (sender == CommandSender.PLAYER) {
            this.serverProcess.tellrawToPlayer(playerName, "&cNie znaleziono takiego polecenia");
        }

        return false;
    }

    private boolean isOp(final String playerName) {
        if (playerName.equalsIgnoreCase("CONSOLE")) return true;
        return this.bdsAutoEnable.getAppConfigManager().getAppConfig().getModerators().contains(playerName);
    }
}