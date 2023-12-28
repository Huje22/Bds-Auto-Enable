package me.indian.bds.discord.jda.manager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.discord.StatsChannelsConfig;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MathUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class StatsChannelsManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final StatsChannelsConfig statsChannelsConfig;
    private final Timer timer;
    private final Guild guild;
    private final long onlinePlayersID, tpsID;
    private int latsTPS;
    private VoiceChannel onlinePlayersChannel, tpsChannel;

    public StatsChannelsManager(final BDSAutoEnable bdsAutoEnable, final DiscordJda discordJda) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.statsChannelsConfig = this.bdsAutoEnable.getAppConfigManager().getDiscordConfig()
                .getBotConfig().getStatsChannelsConfig();
        this.timer = new Timer("Discord Channel Manager Timer", true);
        this.onlinePlayersID = this.statsChannelsConfig.getOnlinePlayersID();
        this.tpsID = this.statsChannelsConfig.getTpsID();
        this.guild = discordJda.getGuild();

        this.latsTPS = 0;
    }

    public void init() {
        this.onlinePlayersChannel = this.guild.getVoiceChannelById(this.onlinePlayersID);
        this.tpsChannel = this.guild.getVoiceChannelById(this.tpsID);
        this.setOnlinePlayersCount();

        if (this.onlinePlayersChannel == null)
            this.logger.debug("(Gracz online) Nie można odnaleźć kanału głosowego z ID &b " + this.onlinePlayersID);
        if (this.tpsChannel == null)
            this.logger.debug("(TPS) Nie można odnaleźć kanału głosowego z ID &b " + this.onlinePlayersID);
    }

    public void setTpsCount(final int tps) {
        if (this.tpsChannel != null) {
            if (tps == this.latsTPS) return;
            this.latsTPS = tps;

            this.tpsChannel.getManager().setName(this.statsChannelsConfig.getTpsName()
                            .replaceAll("<tps>", String.valueOf(tps)))
                    .queue();
        }
    }

    private void setOnlinePlayersCount() {
        if (this.onlinePlayersChannel != null) {

            final TimerTask onlinePlayersTask = new TimerTask() {

                int lastOnlinePlayers;

                @Override
                public void run() {
                    if (!StatsChannelsManager.this.bdsAutoEnable.getServerProcess().isEnabled()) return;
                    final int onlinePlayers = StatsChannelsManager.this.bdsAutoEnable.getServerManager().getOnlinePlayers().size();
                    final int maxPlayers = StatsChannelsManager.this.bdsAutoEnable.getServerProperties().getMaxPlayers();

                    //Sprawdzam tak aby na darmo nie wysyłać requesta do discord
                    if (onlinePlayers == 0 && this.lastOnlinePlayers == 0) return;

                    this.lastOnlinePlayers = onlinePlayers;

                    StatsChannelsManager.this.onlinePlayersChannel.getManager().setName(StatsChannelsManager.this.statsChannelsConfig.getOnlinePlayersName()
                            .replaceAll("<online>", String.valueOf(onlinePlayers))
                            .replaceAll("<max>", String.valueOf(maxPlayers))
                    ).queue();
                }
            };

            this.timer.scheduleAtFixedRate(onlinePlayersTask,
                    MathUtil.minutesTo(1, TimeUnit.MILLISECONDS),
                    MathUtil.secondToMillis(30)
            );
        }
    }

    public void onShutdown() {
        if (this.onlinePlayersChannel != null) {
            this.onlinePlayersChannel.getManager().setName("Offline").queue();
        }
        if (this.tpsChannel != null) {
            this.tpsChannel.getManager().setName("Offline").queue();
        }
    }
}