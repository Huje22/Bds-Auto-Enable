package me.indian.bds.discord.jda.manager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.config.sub.discord.DiscordConfig;
import me.indian.bds.discord.DiscordIntegration;
import me.indian.bds.discord.jda.listener.CommandListener;
import me.indian.bds.discord.jda.listener.JDAListener;
import me.indian.bds.discord.jda.listener.MessageListener;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MessageUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;

public class ChannelManager{

    private final BDSAutoEnable bdsAutoEnable;
  private final DiscordJda discordJda;
    private final Logger logger;
    private final Config config;
    private final DiscordConfig discordConfig;
    private final long onlinePlayersID;
    private final Timer timer;
    private final List<JDAListener> listeners;
    private final JDA jda;
  private final Guild guild;
    private VoiceChannel onlinePlayersChannel;
  
public ChannelManager(final BDSAutoEnable bdsAutoEnable, final DiscordJda discordJda) {
        this.bdsAutoEnable = bdsAutoEnable;
        this.discordJda = discordJda;
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.bdsAutoEnable.getConfig();
        this.discordConfig = this.config.getDiscordConfig();
        this.timer = new Timer("Discord Channel Manager Timer", true);
        this.onlinePlayersID = this.discordConfig.getDiscordBotConfig().getOnlinePlayersID();
        this.jda = this.discordJda.getJda();
        this.guild = this.discordJda.getGuidl();
  

}


  public void init(){
    this.onlinePlayersChannel = this.guild.getVoiceChannelById(this.onlinePlayersID);
      this.setOnlinePlayersCount();

        if (this.onlinePlayersChannel == null) {
            this.logger.debug("(Gracz online) Nie można odnaleźć kanału głosowego z ID &b " + this.onlinePlayersID);
        }
    
          }
  

  private void setOnlinePlayersCount() {
        if (this.onlinePlayersChannel != null) {
            final VoiceChannelManager manager = this.onlinePlayersChannel.getManager();

            final TimerTask onlinePlayersTask = new TimerTask() {

                int lastOnlinePlayers;

                @Override
                public void run() {
                    final int onlinePlayers = DiscordJda.this.bdsAutoEnable.getServerManager().getOnlinePlayers().size();
                    final int maxPlayers = DiscordJda.this.bdsAutoEnable.getServerProperties().getMaxPlayers();

                    //Sprawdzam tak aby na darmo nie wysyłać requesta do discord
                    if (onlinePlayers == 0 && this.lastOnlinePlayers == 0) return;

                    this.lastOnlinePlayers = onlinePlayers;

                    manager.setName(DiscordJda.this.discordConfig.getDiscordBotConfig().getOnlinePlayersMessage()
                            .replaceAll("<online>", String.valueOf(onlinePlayers))
                            .replaceAll("<max>", String.valueOf(maxPlayers))
                    ).queue();

                }
            };

            this.timer.scheduleAtFixedRate(onlinePlayersTask,
                    MathUtil.minutesTo(1, TimeUnit.MILLISECONDS),
                    MathUtil.minutesTo(1, TimeUnit.MILLISECONDS)
            );
        }
  }

}
