package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.util.BedrockQuery;

public class ServerPingCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public ServerPingCommand(final BDSAutoEnable bdsAutoEnable) {
        super("server", "Pinguje server w celu uzyskania z niego informacji");
        this.bdsAutoEnable = bdsAutoEnable;

        this.addOption("<ip> [port]");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (args.length == 0) return false;

        final String adres = args[0];
        int port = 19132;

        if (args[0].equalsIgnoreCase("127.0.0.1") ||
                args[0].equalsIgnoreCase("localhost")) {
            port = this.bdsAutoEnable.getServerProperties().getServerPort();
        }

        if (args.length == 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (final NumberFormatException ignored) {
            }
        }

        this.sendMessage("&aPróba pingowania &1" + adres + "&e:&1" + port);
        final BedrockQuery query = BedrockQuery.create(adres, port);

        if (query.online()) {
            this.sendMessage("&aEdycja:&b " + query.edition());
            this.sendMessage("&aMOTD:&b " + query.motd());
            this.sendMessage("&aProtocol Version:&b " + query.protocol());
            this.sendMessage("&aMinecraft Version:&b " + query.minecraftVersion());
            this.sendMessage("&aPlayer Count:&b " + query.playerCount());
            this.sendMessage("&aMax Players:&b " + query.maxPlayers());
            this.sendMessage("&aMap Name:&b " + query.mapName());
            this.sendMessage("&aGamemode:&b " + query.gamemode());
        } else {
            this.sendMessage("&cSerwer jest offline lub podane informacje są nieprawidłowe");
        }

        return true;
    }
}