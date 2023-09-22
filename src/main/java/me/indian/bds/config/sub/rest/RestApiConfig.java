package me.indian.bds.config.sub.rest;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class RestApiConfig extends OkaeriConfig {


    @Comment({""})
    @Comment({"Dostępne endpointy to \"/api/stats/deaths\" , \"/api/stats/playtime\" , \"/api/stats/players\" "})
    @Comment({""})
    @Comment({"Czy włączyć strone z Rest API?"})
    private boolean enabled = true;
    @Comment({""})
    @Comment({"Port strony "})
    private int port = 8080;
    @Comment({""})
    @Comment({"Rate limit na dany endpoint"})
    private int rateLimit = 100;

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getPort() {
        return this.port;
    }

    public int getRateLimit() {
        return this.rateLimit;
    }
}