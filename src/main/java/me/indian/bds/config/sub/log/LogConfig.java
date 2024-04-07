package me.indian.bds.config.sub.log;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import java.util.Arrays;
import java.util.List;

@Header("################################################################")
@Header("#           Ustawienia Logów                                   #")
@Header("################################################################")

public class LogConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy po zakończeniu procesu aplikacji wysłać logi servera na https://mclo.gs/"})
    @CustomKey("SendLogs")
    private boolean sendLogs = true;

    @Comment({""})
    @Comment({"Nie zapisuje tych informacji które zawierają:"})
    @Comment({"W pliku"})
    @CustomKey("NoFile")
    private List<String> noFile = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]",
            "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]",
            " ERROR]", " WARN]", "[Scripting] Player",
            "\"component_groups\"");
    @Comment({""})
    @Comment("W konsoli")
    @CustomKey("NoConsole")
    private List<String> noConsole = Arrays.asList("[Json]", "[Blocks]", "[Components]", "[Molang]",
            "[Item]", "[Recipes]", "[FeatureRegistry]", "[Actor]", "[Scripting] Player",
            "\"component_groups\"");

    @Comment({""})
    @Comment({"Wysyła ServerAlertEvent z wiadomością która zawiera coś z poniższych informacji"})
    @CustomKey("AlertOn")
    private List<String> alertOn = Arrays.asList("[Scythe]");

    public boolean isSendLogs() {
        return this.sendLogs;
    }

    public List<String> getNoFile() {
        return this.noFile;
    }

    public List<String> getNoConsole() {
        return this.noConsole;
    }

    public List<String> getAlertOn() {
        return this.alertOn;
    }
}