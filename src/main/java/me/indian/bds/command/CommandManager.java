package me.indian.bds.command;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.defaults.BackupCommand;
import me.indian.bds.command.defaults.ChatFormatCommand;
import me.indian.bds.command.defaults.DeathsCommand;
import me.indian.bds.command.defaults.DiscordCommand;
import me.indian.bds.command.defaults.EndCommand;
import me.indian.bds.command.defaults.ExtensionCommand;
import me.indian.bds.command.defaults.HelpCommand;
import me.indian.bds.command.defaults.LinkCommand;
import me.indian.bds.command.defaults.MuteCommand;
import me.indian.bds.command.defaults.PlaytimeCommand;
import me.indian.bds.command.defaults.RestartCommand;
import me.indian.bds.command.defaults.ServerPingCommand;
import me.indian.bds.command.defaults.SettingInfoCommand;
import me.indian.bds.command.defaults.StatsCommand;
import me.indian.bds.command.defaults.TPSCommand;
import me.indian.bds.command.defaults.TestCommand;
import me.indian.bds.command.defaults.UnlinkCommand;
import me.indian.bds.command.defaults.VersionCommand;
import me.indian.bds.server.ServerProcess;
import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProcess serverProcess;
    private final List<Command> commandList;
    private final boolean botEnabled;

    public CommandManager(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.commandList = new ArrayList<>();
        this.botEnabled = this.bdsAutoEnable.getDiscordHelper().isBotEnabled();

        this.registerCommand(new HelpCommand(this.commandList));
        this.registerCommand(new TPSCommand(this.bdsAutoEnable));
        this.registerCommand(new ExtensionCommand(this.bdsAutoEnable));
        this.registerCommand(new EndCommand(this.bdsAutoEnable));
        this.registerCommand(new RestartCommand(this.bdsAutoEnable));
        this.registerCommand(new BackupCommand(this.bdsAutoEnable));
        this.registerCommand(new PlaytimeCommand(this.bdsAutoEnable));
        this.registerCommand(new DeathsCommand(this.bdsAutoEnable));
        this.registerCommand(new VersionCommand(this.bdsAutoEnable));
        this.registerCommand(new ChatFormatCommand(this.bdsAutoEnable));
        this.registerCommand(new MuteCommand(this.bdsAutoEnable));
        this.registerCommand(new SettingInfoCommand(this.bdsAutoEnable));
        this.registerCommand(new ServerPingCommand(this.bdsAutoEnable));
        this.registerCommand(new StatsCommand());

        if (this.bdsAutoEnable.getAppConfigManager().getAppConfig().isDebug()) {
            this.registerCommand(new TestCommand(this.bdsAutoEnable));
        }

        if (this.botEnabled) {
            this.registerCommand(new DiscordCommand(this.bdsAutoEnable));
            this.registerCommand(new LinkCommand(this.bdsAutoEnable));
            this.registerCommand(new UnlinkCommand(this.bdsAutoEnable));
        }
    }

    public <T extends Command> void registerCommand(final T command) {
        if (this.commandList.stream().anyMatch(command1 -> command1.getName().equals(command.getName()))) {
            throw new RuntimeException("Komenda o nazwie " + command.getName() + " już istnieje!");
        }

        this.commandList.add(command);
        command.init(this.bdsAutoEnable);
    }

    public boolean runCommands(final CommandSender sender, final String playerName, final String commandName, final String[] args, final boolean isOp) {
        for (final Command command : this.commandList) {
            if (command.getName().equalsIgnoreCase(commandName)) {
                command.setCommandSender(sender);
                command.setPlayerName(playerName);

                if (!command.onExecute(args, this.isOp(sender, playerName)) && !command.getUsage().isEmpty()) {
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

    private boolean isOp(final CommandSender sender, final String name) {
        //Ta metoda jest do czasu aż mojang nie naprawi w scriptAPI metody `isOp()`
        if (sender == CommandSender.CONSOLE) return true;
        return (this.botEnabled && this.bdsAutoEnable.getDiscordHelper().getDiscordJDA()
                .getLinkingManager().hasPermissions(name, Permission.ADMINISTRATOR));
    }
}