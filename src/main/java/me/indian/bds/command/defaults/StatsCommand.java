package me.indian.bds.command.defaults;

import me.indian.bds.command.Command;
import me.indian.bds.util.StatusUtil;

public class StatsCommand extends Command {

    public StatsCommand() {
        super("stats", "Aktualne statystyki servera minecraft i maszyny");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (this.player != null) {
            this.sendMessage("&cPolecenie można wykonać tylko z poziomu konsoli!");
            return true;
        }

        //TODO: Dodaj wersję dla gracz gdzie będzie pokazane użycie ramu i servera
        //np RAM UŻYWANY BDS/WOLNY RAM
        //np RAM UŻYWANY MASZYNA/WOLNY RAM/CALY RSM
        for (final String stats : StatusUtil.getMainStats(false)) {
            this.sendMessage(stats);
        }
        return true;
    }
}
