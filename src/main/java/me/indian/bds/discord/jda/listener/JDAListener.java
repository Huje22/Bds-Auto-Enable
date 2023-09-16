package me.indian.bds.discord.jda.listener;

import me.indian.bds.server.ServerProcess;

public interface JDAListener {

    void init();

    void initServerProcess(final ServerProcess serverProcess);

}
