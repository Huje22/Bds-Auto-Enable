package me.indian.bds.config.sub.discord;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;


public class LinkingConfig extends OkaeriConfig {

   @Comment({""})
    @Comment({"ID roli którą bedzie otrzymywał użytkownik po połączeniu kont, dostaje się ją jeśli ma sie 5h czasu gry na serwerze"})
    private long linkedRoleID = 1L;
@Comment({""})
  @Comment({"Czy użytkownik może pisać na kanale bez połączonych kont?"})
  private boolean canType = false;
@Comment({""})
  @Comment({""})
  private String cantTypeMessage = "Aby pisać na tym kanale musisz mieć połączone konta discord i Minecraft ";


  public long getLinkedRoleID() {
        return this.linkedRoleID;
  }
 
}
