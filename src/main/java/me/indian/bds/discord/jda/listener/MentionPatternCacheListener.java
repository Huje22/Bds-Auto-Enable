package me.indian.bds.discord.jda.listener;

import me.indian.bds.server.ServerProcess;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Map;
import java.util.regex.Pattern;

public class MentionPatternCacheListener extends ListenerAdapter implements JDAListener {

    private final Map<String, Pattern> mentionPatternCache;

    public MentionPatternCacheListener(final Map<String, Pattern> mentionPatternCache) {
        this.mentionPatternCache = mentionPatternCache;
    }

    @Override
    public void onUserUpdateName(final UserUpdateNameEvent event) {
        this.mentionPatternCache.remove(event.getUser().getId());
    }

    @Override
    public void onGuildMemberUpdateNickname(final GuildMemberUpdateNicknameEvent event) {
        this.mentionPatternCache.remove(event.getMember().getId());
    }

    @Override
    public void onRoleUpdateName(final RoleUpdateNameEvent event) {
        this.mentionPatternCache.remove(event.getRole().getId());
    }

    @Override
    public void init() {

    }

    @Override
    public void initServerProcess(final ServerProcess serverProcess) {

    }
}
