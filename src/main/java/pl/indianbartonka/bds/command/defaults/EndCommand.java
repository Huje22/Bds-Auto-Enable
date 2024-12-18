package pl.indianbartonka.bds.command.defaults;

import java.util.concurrent.TimeUnit;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.config.sub.transfer.LobbyConfig;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.bds.util.ServerUtil;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.logger.LogState;
import pl.indianbartonka.util.logger.Logger;

public class EndCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private boolean canStop;

    public EndCommand(final BDSAutoEnable bdsAutoEnable) {
        super("end", "Kończy działanie servera i aplikacji");
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = bdsAutoEnable.getLogger();
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
        }

        this.end(seconds);
        return true;
    }

    private void end(final int seconds) {
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

        try {
            this.serverProcess.sendToConsole("stop");
            this.logger.alert("Oczekiwanie na zamknięcie servera");
            this.sendMessage("&c&lJeśli proces servera nie zakończy się w czasie&e 2&bminut&c zostanie on zabity!");
            if (!this.serverProcess.waitFor(2, TimeUnit.MINUTES)) {
                ServerUtil.tellrawToAllAndLogger("", "&4Proces servera nie zakończył się w czasie&e 2&b minut&4 powoduje to zabicie procesu", LogState.ERROR);

                this.serverProcess.destroyProcess();

                if (!this.serverProcess.waitFor(30, TimeUnit.SECONDS)) {
                    this.serverProcess.destroyForciblyProcess();
                }
            }
        } catch (final InterruptedException exception) {
            this.logger.error("Wątek został przerwany", exception);
            ThreadUtil.sleep(10);
        }
        System.exit(0);
    }
}
