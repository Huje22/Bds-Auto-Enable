package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.config.sub.transfer.LobbyConfig;
import me.indian.bds.logger.LogState;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.ServerUtil;
import me.indian.bds.util.ThreadUtil;

public class EndCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProcess serverProcess;
    private boolean canStop;

    public EndCommand(final BDSAutoEnable bdsAutoEnable) {
        super("end", "Kończy działanie servera i aplikacji");
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.canStop = true;

        this.addOption("[seconds]");

    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
         if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            this.deniedSound();
            return true;
        }

        int seconds = 5;

        if (args.length == 1) {
            try {
                seconds = Integer.parseInt(args[0]);
            } catch (final NumberFormatException ignored) {
            }
            this.stop(seconds);
        } else {
            this.stop(seconds);
        }
        return true;
    }

    private void stop(final int seconds) {
        if (!this.canStop) {
            this.sendMessage("&cServer jest już w trakcie zatrzymywania");
            return;
        }

        final LobbyConfig lobbyConfig = this.bdsAutoEnable.getAppConfigManager().getTransferConfig().getLobbyConfig();

        ServerUtil.titleToAll("&cServer zostanie zamknięty", "&bZa&a " + seconds + "&e sekund");
        ServerUtil.playSoundToAll("mob.wither.break_block");

        if (lobbyConfig.isEnable()) {
            ServerUtil.tellrawToAll("&2Zaraz zostaniecie przeniesieni na server&b lobby");
        }

        this.canStop = false;
        this.serverProcess.setCanRun(false);

        int nextSeconds = seconds;
        for (int i = seconds; i >= 1; i--) {
            if (i < 10 || i == nextSeconds) {
                nextSeconds = MathUtil.getCorrectNumber((i - 5), 0, seconds);
                ServerUtil.actionBarToAll("&aZa&b " + i + "&a sekund server zostanie zamknięty!");
                ServerUtil.tellrawToAllAndLogger("", "&aZa&b " + i + "&a sekund server zostanie zamknięty!", LogState.INFO);
            }
            ThreadUtil.sleep(1);
        }

        ServerUtil.tellrawToAllAndLogger("", "&aPierw zapiszemy świat!", LogState.INFO);
        this.bdsAutoEnable.getWatchDog().saveAndResume();
        
        ServerUtil.playSoundToAll("mob.wither.death");

        if (!lobbyConfig.isEnable()) {
            ServerUtil.kickAllPlayers("&cServer jest wyłączany");
        }

        this.serverProcess.sendToConsole("stop");

        try {
            this.serverProcess.waitFor();
        } catch (final InterruptedException exception) {
            ThreadUtil.sleep(10);
        }
        System.exit(0);
    }
}
