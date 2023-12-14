package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

public class MuteCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final List<String> muted;
  
    public MuteCommand(final BDSAutoEnable bdsAutoEnable, final List<String> muted) {
        super("mute", "Wycisz/odcisz kogoś na czacie i na discord","&a!mute &b <player>");
        this.bdsAutoEnable = bdsAutoEnable;
      this.muted = muted;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            return true;
        }
      if (args.length == 0) return false;
      final boolen appHandling = this.bdsAutoEnable.getWatchDog().getPackModule().isAppHandledMessages();
      if (!appHandling){
      this.sendMessage("&aAplikacja nie utrzymuję wiadomości czatu, gracz nadal będzie mógł pisać na czacie Minecraft");
      }
      
      final String player = args[0];
      if(!this.muted.contains(player)){
       this.muted.add(player);
        this.sendMessage("&aGracz&b " + player + "&a został dodany do listy wyciszonych");
      } else {
      this.muted.remove(player);
        this.sendMessage("&aGracz&b " + player + "&a został usunięty z listy wyciszonych");
      }
      
      return true;
    }
}
