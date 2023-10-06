package me.indian.bds.config.sub.rest;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

import java.util.Arrays;
import java.util.List;

public class RestApiConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Dostępne endpointy to:"})
    @Comment({"/api/stats/deaths - śmierci graczy   "})
    @Comment({"/api/stats/playtime - czas gry w ms graczy "})
    @Comment({"/api/stats/players - gracze online i offline"})
    @Comment({"/api/{api-key}/backup/{filename} - pobierz któryś z dostępnych backup (wymagany klucz autoryzacji)"})
    @Comment({""})
    @Comment({"Czy włączyć strone z Rest API?"})
    private boolean enabled = false;
    @Comment({""})
    @Comment({"Port strony "})
    private int port = 8080;
    @Comment({""})
    @Comment({"Rate limit na dany endpoint"})
    private int rateLimit = 20;
    @Comment({""})
    @Comment({"Klucze api które możesz rozdać użytkownikom rest api aby mogli: pobierać backupy , więcej wkrótce "})
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