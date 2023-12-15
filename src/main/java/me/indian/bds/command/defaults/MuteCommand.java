package me.indian.bds.command.defaults;

import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.discord.jda.manager.LinkingManager;
import me.indian.bds.server.manager.ServerManager;
import net.dv8tion.jda.api.entities.Member;

public class MuteCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerManager serverManager;

    public MuteCommand(final BDSAutoEnable bdsAutoEnable) {
        super("mute", "Wycisz/odcisz kogoś na czacie i na discord", "&a!mute &b <player>");
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverManager = this.bdsAutoEnable.getServerManager();
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            return true;
        }
        if (args.length == 0) return false;

        final boolean appHandling = this.bdsAutoEnable.getWatchDog().getPackModule().isAppHandledMessages();
        final String player = args[0];

        if (!appHandling) {
            this.sendMessage("&aAplikacja nie utrzymuję wiadomości czatu, gracz nadal będzie mógł pisać na czacie Minecraft");
        }

        if (this.serverManager.isMuted(player)) {
            this.unMute(player);
        } else {
            if (!this.serverManager.isOnline(player) && !this.isLinked(player)) {
                this.sendMessage("&aGracz&b " + player + "&a nie jest online ani nie ma połączonego konta z discord");
                return true;
            }
            this.mute(player);
        }
        return true;
    }

    private boolean isLinked(final String name) {
        if (this.bdsAutoEnable.getDiscord() instanceof final DiscordJda jda) {
            if (jda.getLinkingManager() != null) return jda.getLinkingManager().isLinked(name);
        }
        return false;
    }

    private void mute(final String name) {
        this.serverManager.mute(name);
        this.sendMessage("&aGracz&b " + name + "&a został dodany do listy wyciszonych");
        if (this.bdsAutoEnable.getDiscord() instanceof final DiscordJda jda) {
            final LinkingManager linkingManager = jda.getLinkingManager();
            if (linkingManager == null) return;
            final Member member = linkingManager.getMember(name);
            if (member == null) return;

            jda.mute(member, 1, TimeUnit.MINUTES);
        }
    }

    private void unMute(final String name) {
        this.serverManager.unMute(name);
        this.sendMessage("&aGracz&b " + name + "&a został usunięty z listy wyciszonych");
        if (this.bdsAutoEnable.getDiscord() instanceof final DiscordJda jda) {
            final LinkingManager linkingManager = jda.getLinkingManager();
            if (linkingManager == null) return;
            final Member member = linkingManager.getMember(name);
            if (member == null) return;

            jda.unMute(member);
        }
    }
}