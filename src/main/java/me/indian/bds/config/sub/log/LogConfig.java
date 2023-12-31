package me.indian.bds.config.sub.log;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Header("################################################################")
@Header("#           Ustawienia Logów                                   #")
@Header("################################################################")

public class LogConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Nie zapisuje tych informacji które zawierają:"})
    @Comment({"W pliku"})
    @CustomKey("NoFile")
    private List<String> noFile = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]",
            "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]",
            " ERROR]", " WARN]",
            "\"component_groups\"");
    @Comment({""})
    @Comment("W konsoli")
    @CustomKey("NoConsole")
    private List<String> noConsole = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]",
            "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]",
            "\"component_groups\"");

    @Comment({""})
    @Comment("W konsoli discord")
    @Comment("UWAGA!!! Jeśli używasz 1gb ram dla aplikacji (co jest optymalne) nie usuwaj nic z tąd albo daj więcej ram!!!!")
    @CustomKey("NoDiscordConsole")
    private List<String> noDiscordConsole = new ArrayList<>();

    public LogConfig() {
        this.noDiscordConsole.addAll(this.noConsole);
        this.noDiscordConsole.addAll(this.noFile);
    }

    public List<String> getNoFile() {
        return this.noFile;
    }

    public List<String> getNoConsole() {
        return this.noConsole;
    }

    public List<String> getNoDiscordConsole() {
        return this.noDiscordConsole;
    }
}