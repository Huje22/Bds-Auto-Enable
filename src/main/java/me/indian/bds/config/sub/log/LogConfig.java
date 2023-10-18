package me.indian.bds.config.sub.log;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Nie zapisuje tych informacji które zawierają:"})
    @Comment({"W pliku"})
    private List<String> noFile = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]",
            "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]",
            " ERROR]", " WARN]",
            "\"component_groups\"");
    @Comment({""})
    @Comment("W konsoli")
    private List<String> noConsole = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]",
            "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]",
            "\"component_groups\"");

    @Comment({""})
    @Comment("W konsoli discord")
    @Comment("ULEPSZYĆ TO WSZYTKO Z TYM")
    @Comment("Jeśli log zawiera to bedzie to pokazywane w konsoli na discord")
    private List<String> allowedInDiscordConsole = Arrays.asList("INFO]" , "");

    public List<String> getNoFile() {
        return this.noFile;
    }

    public List<String> getNoConsole() {
        return this.noConsole;
    }

    public List<String> getAllowedInDiscordConsole() {
        return this.allowedInDiscordConsole;
    }
}
