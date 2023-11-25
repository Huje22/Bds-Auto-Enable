package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class LinkingConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"ID roli którą bedzie otrzymywał użytkownik po połączeniu kont, dostaje się ją jeśli ma sie 5h czasu gry na serwerze"})
    private long linkedRoleID = 1L;
    @Comment({""})
    @Comment({"Czy użytkownik może pisać na kanale bez połączonych kont?"})
    private boolean canType = false;
    @Comment({""})
    @Comment({""})
    private String cantTypeMessage = "Aby wysyłać wiadomości na tym kanale musisz mieć połączone konta discord i Minecraft ";

    public boolean isCanType() {
        return this.canType;
    }

    public String getCantTypeMessage() {
        return this.cantTypeMessage;
    }

    public long getLinkedRoleID() {
        return this.linkedRoleID;
    }

}