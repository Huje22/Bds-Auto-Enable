package me.indian.bds;

import me.indian.bds.config.Config;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class AutoMessages {

    private final BDSAutoEnable bdsAutoEnable;
    private final ServerProcess serverProcess;
    private final Timer timer;
    private final Config config;
    private final Random random;
    private final List<String> messages;

    public AutoMessages(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.timer = new Timer("AutoMessages", true);
        this.config = this.bdsAutoEnable.getConfig();
        this.random = new Random();
        this.messages = this.config.getAutoMessagesConfig().getMessages();
    }

    public void start() {
        final TimerTask autoMessages = new TimerTask() {
            Iterator<String> iterator = AutoMessages.this.messages.iterator();

            @Override
            public void run() {
                if (AutoMessages.this.serverProcess.isEnabled() && !AutoMessages.this.bdsAutoEnable.getServerManager().getOnlinePlayers().isEmpty()) {
                    if (!this.iterator.hasNext()) this.iterator = AutoMessages.this.messages.iterator();
                    final String prefix = AutoMessages.this.config.getAutoMessagesConfig().getPrefix();
                    
                    if (AutoMessages.this.config.getAutoMessagesConfig().isRandom()) {
                        final int message = AutoMessages.this.random.nextInt(AutoMessages.this.messages.size());
                        
                        AutoMessages.this.serverProcess.tellrawToAll(prefix + AutoMessages.this.messages.get(message));
                    } else {
                        AutoMessages.this.serverProcess.tellrawToAll(prefix + this.iterator.next());
                    }
                }
            }
        };
        if (this.config.getAutoMessagesConfig().isEnabled()) {
            this.timer.scheduleAtFixedRate(autoMessages, 0, MathUtil.secondToMillis(this.config.getAutoMessagesConfig().getTime()));
        } else {
            this.bdsAutoEnable.getLogger().debug("&aAutomessages jest&c wyłączone");
        }
    }
}
