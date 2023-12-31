package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.command.CommandSender;
import me.indian.bds.discord.jda.manager.LinkingManager;
import me.indian.bds.util.MessageUtil;

public class LinkCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public LinkCommand(final BDSAutoEnable bdsAutoEnable) {
        super("link", "Połącz konta Discord i Minecraft");
        this.bdsAutoEnable = bdsAutoEnable;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (this.commandSender == CommandSender.CONSOLE) {
            this.sendMessage("&cPolecenie jest tylko dla graczy");
            return true;
        }

        final LinkingManager linkingManager = this.bdsAutoEnable.getDiscordHelper().getDiscordJDA().getLinkingManager();
        if (linkingManager == null) {
            this.sendMessage("&cCoś poszło nie tak , &bLinkingManager&c jest&4 nullem");
            return true;
        }

        if (linkingManager.isLinked(this.playerName)) {
            this.sendMessage(
                    "&aTwoje konto jest już połączone z ID:&b " + linkingManager.getIdByName(this.playerName));
            return true;
        }

        final String code = MessageUtil.generateCode(6);
        linkingManager.addAccountToLink(this.playerName, code);
        this.sendMessage("&aTwój kod do połączenia konto to:&b " + code);
        this.sendMessage("&aUżyj na naszym discord&b /link&a aby go użyć");


        return true;
    }
}