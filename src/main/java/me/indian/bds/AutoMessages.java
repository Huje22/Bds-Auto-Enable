package me.indian.bds;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import me.indian.bds.config.sub.AutoMessagesConfig;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;

public class AutoMessages {

    private final BDSAutoEnable bdsAutoEnable;
    private final AutoMessagesConfig autoMessagesConfig;
    private final ServerProcess serverProcess;
    private final Timer timer;
    private final Random random;
    private final List<String> messages;

    public AutoMessages(final BDSAutoEnable bdsAutoEnable) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.autoMessagesConfig = this.bdsAutoEnable.getAppConfigManager().getAutoMessagesConfig();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.timer = new Timer("AutoMessages", true);
        this.random = new Random();
        this.messages = this.autoMessagesConfig.getMessages();
    }

    public void start() {
        final TimerTask autoMessages = new TimerTask() {
            Iterator<String> iterator = AutoMessages.this.messages.iterator();

            @Override
            public void run() {
                if (AutoMessages.this.serverProcess.isEnabled() && !AutoMessages.this.bdsAutoEnable.getServerManager().getOnlinePlayers().isEmpty()) {
                    if (!this.iterator.hasNext()) this.iterator = AutoMessages.this.messages.iterator();
                    final String prefix = AutoMessages.this.autoMessagesConfig.getPrefix();

                    if (AutoMessages.this.autoMessagesConfig.isRandom()) {
                        final int message = AutoMessages.this.random.nextInt(AutoMessages.this.messages.size());

                        AutoMessages.this.serverProcess.tellrawToAll(prefix + AutoMessages.this.messages.get(message));
                    } else {
                        AutoMessages.this.serverProcess.tellrawToAll(prefix + this.iterator.next());
                    }
                }
            }
        };
        if (this.autoMessagesConfig.isEnabled()) {
            this.timer.scheduleAtFixedRate(autoMessages, 0, MathUtil.secondToMillis(this.autoMessagesConfig.getTime()));
        } else {
            this.bdsAutoEnable.getLogger().debug("&aAutomessages jest&c wyłączone");
        }
    }
}