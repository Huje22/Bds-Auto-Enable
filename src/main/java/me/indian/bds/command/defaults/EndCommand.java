package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.logger.LogState;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.ThreadUtil;

public class EndCommand extends Command {

    private final ServerProcess serverProcess;
    private boolean canStop;

    public EndCommand(final BDSAutoEnable bdsAutoEnable) {
        super("end", "Kończy działanie servera i aplikacji");
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.canStop = true;

        this.addOption("[seconds]");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
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
        this.canStop = false;
        this.serverProcess.setCanRun(false);
        for (int i = seconds; i >= 0; i--) {
            this.serverProcess.tellrawToAllAndLogger("",
                    "&aZa&1 " + i + "&a sekund server zostanie zamknięty!", LogState.INFO);
            ThreadUtil.sleep(1);
        }
        this.serverProcess.sendToConsole("stop");
        ThreadUtil.sleep(10);
        System.exit(1);
    }
}