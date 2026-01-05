package pl.indianbartonka.bds.command.defaults.admin;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.util.logger.Logger;

public class StartCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ServerProcess serverProcess;

    public StartCommand(final BDSAutoEnable bdsAutoEnable) {
        super("start", "Rozpoczyna prace serveru");
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = bdsAutoEnable.getLogger();
        this.serverProcess = bdsAutoEnable.getServerProcess();

        this.addOption("[seconds]");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            this.deniedSound();
            return true;
        }

        if (this.serverProcess.checkProcesRunning()){
            this.sendMessage("&cSERVER już działa!!");
            return true;
        }

        this.serverProcess.setCanRun(true);
        this.serverProcess.startProcess();
        return true;
    }
}
