package me.indian.bds.config.sub.transfer;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class MainServerConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy przenieść gracza znów na server gdy jest to potrzebne?"})
    private boolean transfer = true;

    @Comment({""})
    @Comment({"IP twojego servera minecraft"})
    private String ip = "21.37.05";

    public boolean isTransfer() {
        return this.transfer;
    }

    public String getIp() {
        if (this.ip.equals("localhost") || this.ip.equals("127.0.0.1")) {
            throw new RuntimeException("Ip servera nie może być jako localhost");
        }

        return this.ip;
    }
}
