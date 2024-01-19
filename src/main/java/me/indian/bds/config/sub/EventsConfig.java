package me.indian.bds.config.sub;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import java.util.List;

@Header("################################################################")
@Header("#           Ustawienia Eventów                                 #")
@Header("################################################################")

public class EventsConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Komendy które zostaną wykonane gdy gracz odrodzi sie  "})
    @CustomKey("OnSpawn")
    private List<String> onSpawn = List.of("");

    @Comment({""})
    @Comment({"Komendy które zostaną wykonane gdy gracz dołączy na server"})
    @CustomKey("OnJoin")
    private List<String> onJoin = List.of("effect <player> resistance 10 100");

    @Comment({""})
    @Comment({"Komendy które zostaną wykonane gdy gracz dołączy na server"})
    @Comment({"Można użyć <form> i <to> aby uzyskać wymiary"})
    @CustomKey("OnDimensionChange")
    private List<String> onDimensionChange = List.of("effect <player> resistance 20 100");

    public List<String> getOnSpawn() {
        return this.onSpawn;
    }

    public List<String> getOnJoin() {
        return this.onJoin;
    }

    public List<String> getOnDimensionChange() {
        return this.onDimensionChange;
    }
}