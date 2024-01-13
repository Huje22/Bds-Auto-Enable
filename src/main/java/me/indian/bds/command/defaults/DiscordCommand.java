package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.discord.DiscordHelper;
import me.indian.bds.discord.jda.DiscordJDA;
import me.indian.bds.util.MessageUtil;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class DiscordCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final AppConfigManager appConfigManager;
    private final DiscordHelper discordHelper;
    private final DiscordJDA discordJDA;
    private TextChannel textChannel;

    public DiscordCommand(final BDSAutoEnable bdsAutoEnable) {
        super("discord", "Komenda do zarządzania Discord ");
        this.bdsAutoEnable = bdsAutoEnable;
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
        this.discordHelper = this.bdsAutoEnable.getDiscordHelper();
        this.discordJDA = this.discordHelper.getDiscordJDA();

        this.addOption("help");
        this.addOption("online [true]");
        this.addOption("message <wiadomość>");
        this.addOption("status <nowy status bota>");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (args.length == 0) return false;

        this.textChannel = this.discordJDA.getTextChannel();

        if (args[0].equalsIgnoreCase("help")) {
            this.sendMessage("&aonline&4 -&b Osoby online mające dostęp do&1 #" + this.textChannel.getName());
            this.sendMessage("&amessage&4 -&b Wysyła wiadomość do Discord na kanał&1 #" + this.textChannel.getName());
            this.sendMessage("&astatus&4 -&b Zmienia status bota");
            return true;
        }

        if (args[0].equalsIgnoreCase("online")) {
            if(!this.discordJDA.isCacheFlagEnabled(CacheFlag.ONLINE_STATUS)){
                this.sendMessage("&cFlaga&b " +  CacheFlag.ONLINE_STATUS +"&c jest wyłączona , bot nie wie kto ma jaki status aktywności");
                return true;
            }

            this.sendMessage("&aOsoby online mające dostęp do&1 #" + this.textChannel.getName());
            for (final Member member : this.discordJDA.getAllChannelOnlineMembers(this.textChannel)) {
                final OnlineStatus onlineStatus = member.getOnlineStatus();
                this.sendMessage(this.discordJDA.getColoredRole(this.discordJDA.getHighestRole(member.getIdLong())) +
                        " &b" + this.discordJDA.getUserName(member, member.getUser()) +
                        "&d - " + this.discordJDA.getStatusColor(member.getOnlineStatus()) + onlineStatus +
                        " &6[&9" + MessageUtil.enumSetToString(member.getActiveClients(), " &a,&9 ") + "&6] "
                );
            }
            return true;
        }

        if (!isOp) {
            this.sendMessage("Nie masz odpowiednich uprawnień do wykonania tego polecenia");
            return true;
        }

        if (args[0].equalsIgnoreCase("message")) {
            final String message = MessageUtil.buildMessageFromArgs(MessageUtil.removeFirstArgs(args));
            if (message.isEmpty()) {
                this.sendMessage("&cWiadomość nie może być pusta!");
                return true;
            }

            this.discordJDA.sendMessage(message);
            this.sendMessage("&aWysłano wiadomość:&b " + message);
            return true;
        }
        if (args[0].equalsIgnoreCase("status")) {
            final String message = MessageUtil.buildMessageFromArgs(MessageUtil.removeFirstArgs(args));
            if (message.isEmpty()) {
                this.sendMessage("&cStatus aktywności nie może być pusty!");
                return true;
            }

            this.discordJDA.setBotActivityStatus(message);
            this.sendMessage("&aUstawiono status aktywności bota na:&b " + message);
            return true;
        }

        return false;
    }
}