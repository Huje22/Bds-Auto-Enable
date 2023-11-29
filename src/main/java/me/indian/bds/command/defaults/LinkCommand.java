package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.discord.jda.manager.LinkingManager;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;

public class LinkCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProcess serverProcess;

    public LinkCommand(final BDSAutoEnable bdsAutoEnable) {
        super("link", "Połącz konta Discord i Minecraft");
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
    }

    @Override
    public boolean onExecute(final String player, final String[] args, final boolean isOp) {
        if (this.bdsAutoEnable.getDiscord() instanceof final DiscordJda jda) {
            final LinkingManager linkingManager = jda.getLinkingManager();
            if (linkingManager == null) {
                this.serverProcess.tellrawToPlayer(player, "&cCoś poszło nie tak , &bLinkingManager&c jest&4 nullem");
                return false;
            }
            if (linkingManager.isLinked(player)) {
                this.serverProcess.tellrawToPlayer(player,
                        "&aTwoje konto jest już połączone z ID:&b " + linkingManager.getIdByName(player));
                return false;
            }

            final String code = MessageUtil.generateCode(6);
            linkingManager.addAccountToLink(player, code);
            this.serverProcess.tellrawToPlayer(player, "&aTwój kod do połączenia konto to:&b " + code);
            this.serverProcess.tellrawToPlayer(player, "&aUżyj na naszym discord&b /link&a aby go użyć");

        } else {
            this.serverProcess.tellrawToPlayer(player, "&cTen server nie używa integracji z JDA");
        }
        return false;
    }
}