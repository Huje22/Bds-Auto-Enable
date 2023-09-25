package me.indian.bds.config.sub;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

import java.util.Arrays;
import java.util.List;

public class AutoMessagesConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Włączenie jak i każda inna zmiana wymaga restartu aplikacji"})
    private boolean enabled = true;
    @Comment({""})
    @Comment({"Jeśli włączone lista nie leci po kolei"})
    private boolean random = false;
    @Comment({""})
    @Comment({"Odstęp czasowy w sekundach z jakim wysyłane są wiadomości"})
    private int time = 120;
    @Comment({""})
    @Comment({"Prefix wiadomości"})
    private String prefix = "&7[&aAuto&eMessages&7] ";
    @Comment({""})
    @Comment({"Lista wiadomości , możesz użyć & do kolorów , aktualnie jedna wiadomość to jedna linia!"})
    private List<String> messages = Arrays.asList("&bTen server używa &aBDS-Auto-Enable&3 https://github.com/Huje22/Bds-Auto-Enable",
            "&aWpadnij na nasz discord&e https://discord.com/invite/&b56h83WPKdK",
            "&bProjekt robiony dla servera&a Huje&e22&b !",
            "&bZasponsoruj &aHuje&e22&3 https://host2play.com/donation/UqJqPNW0"
    );

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isRandom() {
        return this.random;
    }

    public int getTime() {
        return this.time;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public List<String> getMessages() {
        return this.messages;
    }
}
