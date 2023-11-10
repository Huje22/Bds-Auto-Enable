package me.indian.bds.discord.jda.manager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.discord.StatsChannelsConfig;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.ThreadUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;

public class StatsChannelsManager {

    private final BDSAutoEnable bdsAutoEnable;
    private final DiscordJda discordJda;
    private final Logger logger;
    private final StatsChannelsConfig statsChannelsConfig;
    private final long onlinePlayersID;
    private final Timer timer;
    private final Guild guild;
    private VoiceChannel onlinePlayersChannel;

    public StatsChannelsManager(final BDSAutoEnable bdsAutoEnable, final DiscordJda discordJda) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.discordJda = discordJda;
        this.logger = this.bdsAutoEnable.getLogger();
        this.statsChannelsConfig = this.bdsAutoEnable.getConfig().getDiscordConfig()
                .getDiscordBotConfig().getStatsChannelsConfig();
        this.timer = new Timer("Discord Channel Manager Timer", true);
        this.onlinePlayersID = this.statsChannelsConfig.getOnlinePlayersID();
        this.guild = this.discordJda.getGuild();

    }

    public void init() {
        this.onlinePlayersChannel = this.guild.getVoiceChannelById(this.onlinePlayersID);
        this.setOnlinePlayersCount();

        if (this.onlinePlayersChannel == null) {
            this.logger.debug("(Gracz online) Nie można odnaleźć kanału głosowego z ID &b " + this.onlinePlayersID);
        }

    }

    //TODO: Dodać kanał dla TPS (pomyśle jeszcze nad tym)

    private void setOnlinePlayersCount() {
        if (this.onlinePlayersChannel != null) {
            final VoiceChannelManager manager = this.onlinePlayersChannel.getManager();

            final TimerTask onlinePlayersTask = new TimerTask() {

                int lastOnlinePlayers;

                @Override
                public void run() {
                    final int onlinePlayers = StatsChannelsManager.this.bdsAutoEnable.getServerManager().getOnlinePlayers().size();
                    final int maxPlayers = StatsChannelsManager.this.bdsAutoEnable.getServerProperties().getMaxPlayers();
          
                    //Sprawdzam tak aby na darmo nie wysyłać requesta do discord
                    if (onlinePlayers == 0 && this.lastOnlinePlayers == 0) return;

                     // Ustawiam kanał aby pozyskać go znów bo jak jest edytowany ręcznie to chyba dostaję nową instancję 
                      onlinePlayersChannel = this.guild.getVoiceChannelById(this.onlinePlayersID);
                     if (onlinePlayersChannel == null) return;

                    this.lastOnlinePlayers = onlinePlayers;

                    manager.setName(StatsChannelsManager.this.statsChannelsConfig.getOnlinePlayersMessage()
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
            ThreadUtil.sleep(2);
        }


    }
}
