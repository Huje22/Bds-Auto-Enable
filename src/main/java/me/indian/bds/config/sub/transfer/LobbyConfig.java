package me.indian.bds.config.sub.transfer;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class LobbyConfig  extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy przenosić graczy na dany server podczas restartu albo wyłączenia servera?"})
    private boolean enable = true;

    @Comment({""})
    @Comment({"Adres IP servera na jaki mają zostać przeniesieni gracze"})
    private String address = "play.skyblockpe.com";

    @Comment({""})
    @Comment({"Port servera na jaki mają zostać przeniesieni gracze"})
    private int port = 19132;

    @Comment({""})
    @Comment({"Wiadomość przed przeniesieniem gracza"})
    private String transferringMessage = "&aZaraz zostaniesz przeniesiony na server&b lobby";

    @Comment({""})
    @Comment({"Wiadomość o tym gdy server lobby jest niedostępny"})
    private String serverOffline = "&cServer&b lobby&c jest aktualnie offline!";

    public boolean isEnable() {
        return this.enable;
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public String getTransferringMessage() {
        return this.transferringMessage;
    }

    public String getServerOffline() {
        return this.serverOffline;
    }
}