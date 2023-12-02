package me.indian.bds.command.defaults;

import me.indian.bds.command.Command;
import me.indian.bds.command.CommandSender;
import me.indian.bds.util.StatusUtil;

public class DeathsCommand extends Command {


    public DeathsCommand() {
        super("deaths", "Top 10 graczy z największą ilością śmierci");
    }

    @Override
    public boolean onExecute(final CommandSender sender, final String[] args, final boolean isOp) {
        this.sendMessage("&a---------------------");
        for (final String s : StatusUtil.getTopDeaths(false, 10)) {
            this.sendMessage(s);
        }
        this.sendMessage("&a---------------------");
        return true;
    }
}
