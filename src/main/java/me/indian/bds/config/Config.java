package me.indian.bds.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import me.indian.bds.config.sub.AutoMessagesConfig;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.config.sub.log.LogConfig;
import me.indian.bds.config.sub.rest.RestApiConfig;
import me.indian.bds.config.sub.version.VersionManagerConfig;
import me.indian.bds.config.sub.watchdog.WatchDogConfig;


@Header("################################################################")
@Header("#                                                              #")
@Header("#    Huje22 Bds-Auto-Enable                                    #")
@Header("#  https://github.com/Huje22/Bds-Auto-Enable                   #")
@Header("#  https://bstats.org/plugin/bukkit/BDS-Auto-Enable/19727      #")
@Header("#                                                              #")
@Header("################################################################")

public class Config extends OkaeriConfig {
    @Comment({""})
    @Comment({"Pierwsze uruchomienie"})
    @CustomKey("FirstRun")
    private boolean firstRun = true;

    @Comment({""})
    @Comment({"Jeśli ustawisz na false następnym razem aplikacja uruchomi się bez zadawania żadnych pytań"})
    @CustomKey("Questions")
    private boolean questions = true;

    @Comment({""})
    @Comment({"Czy użyć wine?"})
    @Comment({"Aby użyć wine trzeba mieć je pobrane!"})
    @CustomKey("Wine")
    private boolean wine = false;

    @Comment({""})
    @Comment({"Ścieżka do plików z serverem"})
    @CustomKey("FilesPath")
    private String filesPath = "./";

    @Comment({""})
    @Comment({"UUID aplikacji"})
    @CustomKey("UUID")
    private String uuid = "";

    @Comment({""})
    @Comment({"Debug, dodatkowe wiadomości w konsoli dla developerów"})
    @CustomKey("Debug")
    private boolean debug = false;

   //TODO: Oddać gettery i settery
}
