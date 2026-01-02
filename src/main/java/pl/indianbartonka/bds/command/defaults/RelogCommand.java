package pl.indianbartonka.bds.command.defaults;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.config.sub.transfer.MainServerConfig;
import pl.indianbartonka.bds.util.ServerUtil;
import pl.indianbartonka.util.minecraft.BedrockQuery;

public class RelogCommand extends Command {

    public final BDSAutoEnable bdsAutoEnable;
    public boolean isCorrect;

    public RelogCommand(final BDSAutoEnable bdsAutoEnablel) {
        super("relog", "Reloguje ciebie albo wybranego gracza");
        this.bdsAutoEnable = bdsAutoEnablel;
        this.addOption("[player]", "Nick gracza którego przenieść");
    }


    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        final String playerName;

        if (args.length == 1) {
            if (!isOp) {
                this.sendMessage("&cNie masz pozwoleń aby relogować innego gracza!!");
                return true;
            }
                playerName = args[0];

                if (!ServerUtil.isOnline(playerName)) {
                    this.sendMessage("&cTen gracz jest offline!!");
                    return true;
                }

                ServerUtil.tellrawToPlayer(playerName, "&cAdmin wykonuje relog dla ciebie!");
        } else {
            if (this.player == null) {
                this.sendMessage("&cNie możesz relogować jako konsola!!!");
                return true;
            } else {
                playerName = this.player.getPlayerName();
            }
        }

        final MainServerConfig mainServerConfig = this.bdsAutoEnable.getAppConfigManager().getTransferConfig().getMainServerConfig();

        final String ip = mainServerConfig.getIp();
        final int port = this.bdsAutoEnable.getServerProperties().getServerPort();

        if (!this.isCorrect) {
            this.sendMessage("&aUzyskiwanie serveru....");

            this.isCorrect = BedrockQuery.create(ip, port).online();

            if (!this.isCorrect) {
                this.sendMessage("&cNie można uzyskać serveru!!");
                this.sendMessage("&aPrawdopodobnie Admin w&e Transfer.yml&a ustawił&c nieprawidłowy&a adres IP!!!");
                return true;
            }
        }

        ServerUtil.transferPlayer(playerName, ip, port);

        return true;
    }
}
