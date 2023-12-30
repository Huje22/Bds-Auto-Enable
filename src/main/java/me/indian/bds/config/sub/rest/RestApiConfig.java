package me.indian.bds.config.sub.rest;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import java.util.Arrays;
import java.util.List;

@Header("################################################################")
@Header("#           Ustawienia Strony z RestAPI                        #")
@Header("################################################################")

public class RestApiConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Apy zobaczyć dostępne endpointy zobacz:"})
    @Comment({"https://github.com/Huje22/Bds-Auto-Enable/blob/master/RestAPI.MD"})
    @Comment({"Czy włączyć strone z Rest API?"})
    @CustomKey("Enable")
    private boolean enabled = false;

    @Comment({""})
    @Comment({"Port strony "})
    @CustomKey("Port")
    private int port = 8080;

    @Comment({""})
    @Comment({"Rate limit na dany endpoint"})
    @CustomKey("RateLimit")
    private int rateLimit = 20;

    @Comment({""})
    @Comment({"Klucze api które możesz rozdać użytkownikom rest api aby mogli: pobierać backupy , więcej wkrótce "})
    @CustomKey("ApiKeys")
    private List<String> apiKeys = Arrays.asList("kOpsAjdfads", "KYFgvHVY");

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getPort() {
        return this.port;
    }

    public int getRateLimit() {
        return this.rateLimit;
    }

    public List<String> getApiKeys() {
        return this.apiKeys;
    }
}