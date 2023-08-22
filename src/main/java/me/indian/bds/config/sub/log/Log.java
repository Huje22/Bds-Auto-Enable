package me.indian.bds.config.sub.log;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import java.util.Arrays;
import java.util.List;

public class Log extends OkaeriConfig {

    @Comment({""})
    @Comment({"Nie zapisuje tych informacj które zawierają:"})
    @Comment({"W pliku"})
    private List<String> file = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]",
            "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]",
            " ERROR]", " WARN]",
            "\"component_groups\"");
    @Comment({""})
    @Comment("W konsoli")
    private List<String> console = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]",
            "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]",
            "\"component_groups\"");

    public List<String> getFile() {
        return this.file;
    }

    public List<String> getConsole() {
        return this.console;
    }
}
