package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.command.CommandSender;
import me.indian.bds.discord.jda.manager.LinkingManager;

public class UnlinkCommand extends Command {

    private final LinkingManager linkingManager;

    public UnlinkCommand(final BDSAutoEnable bdsAutoEnable) {
        super("unlink", "Rozłącza konto Discord z kontem nickiem Minecraft");

        this.addOption("[player]");
        this.linkingManager = bdsAutoEnable.getDiscordHelper().getDiscordJDA().getLinkingManager();
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if(args.length == 0){
            if (this.commandSender == CommandSender.CONSOLE) {
                this.sendMessage("&cPolecenie jest tylko dla graczy");
                return true;
            }
            if (this.linkingManager.isLinked(this.playerName)) {
                if(!this.linkingManager.isCanUnlink() && !isOp){
                    this.sendMessage("&cNie możesz sobie sam rozłączyć kont");
                    return true;
                }
                this.linkingManager.unLinkAccount(this.playerName);
                this.sendMessage("Rozłączono konto " + this.playerName);
            } else {
                this.sendMessage("&cNie posiadasz połączonych kont!");
            }
        } else {
            if(!isOp){
                this.sendMessage("&cNie masz permisji!");
                return true;
            }
            final String target = args[0];
            if (this.linkingManager.isLinked(target)) {
                this.linkingManager.unLinkAccount(target);
                this.sendMessage("Rozłączono konto " + target);
            } else {
                this.sendMessage("&cGracz &b" + target + "&c nie posiada połączonych kont!");
            }
        }
        return true;
    }
}