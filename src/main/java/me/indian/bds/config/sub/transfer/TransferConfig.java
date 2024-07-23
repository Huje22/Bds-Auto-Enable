package me.indian.bds.config.sub.transfer;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;

@Header("################################################################")
@Header("#           Ustawienia Transferowania do lobby                 #")
@Header("################################################################")
public class TransferConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Konfiguracja servera lobby"})
    @CustomKey("LobbyServer")
    private LobbyConfig lobbyConfig = new LobbyConfig();

    @Comment({""})
    @Comment({"Konfiguracja servera głównego"})
    @CustomKey("MainServer")
    private MainServerConfig mainServerConfig = new MainServerConfig();

    public LobbyConfig getLobbyConfig() {
        return this.lobbyConfig;
    }

    public MainServerConfig getMainServerConfig() {
        return this.mainServerConfig;
    }
}