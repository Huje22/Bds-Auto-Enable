package me.indian.bds.discord.jda.listener;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.Config;
import me.indian.bds.discord.jda.DiscordJda;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.MinecraftUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageReceived extends ListenerAdapter {

    private final DiscordJda discordJda;
    private final JDA jda;
    private final BDSAutoEnable bdsAutoEnable;
    private final Config config;
    private final String prefix;
    private TextChannel textChannel;
    private TextChannel consoleChannel;
    private ServerProcess serverProcess;

    public MessageReceived(final DiscordJda discordJda, final BDSAutoEnable bdsAutoEnable) {
        this.discordJda = discordJda;
        this.jda = this.discordJda.getJda();
        this.bdsAutoEnable = bdsAutoEnable;
        this.config = this.bdsAutoEnable.getConfig();
        this.prefix = this.config.getDiscordBot().getPrefix();
    }

    public void initChannels() {
        this.textChannel = this.discordJda.getTextChannel();
        this.consoleChannel = this.discordJda.getConsoleChannel();
    }

    public void initServerProcess(final ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        final Member member = event.getMember();
        final User author = event.getAuthor();
        final Message message = event.getMessage();
        final String rawMessage = message.getContentRaw();
        final TextChannel channel = event.getChannel().asTextChannel();

        if (event.getAuthor().isBot()) return;

        if (rawMessage.contains(event.getJDA().getSelfUser().getAsMention())) {
            event.getChannel().sendMessage("Mój prefix to `" + this.prefix + "` \n").queue(msg -> {
                msg.delete().queueAfter(10, TimeUnit.SECONDS);
                message.delete().queueAfter(9, TimeUnit.SECONDS);
            });
            return;
        }

        if (channel == this.consoleChannel) {
            if (member == null) return;
            if (member.hasPermission(Permission.ADMINISTRATOR)) {
                this.serverProcess.sendToConsole(rawMessage);
            } else {
                event.getChannel().sendMessage("Nie masz uprawnień administratora aby wysłać tu wiadomość").queue(msg -> {
                    msg.delete().queueAfter(5, TimeUnit.SECONDS);
                    message.delete().queueAfter(4, TimeUnit.SECONDS);
                });
            }
            return;
        }
        if (channel == this.textChannel && !rawMessage.startsWith(this.prefix)) {
            this.bdsAutoEnable.getLogger().debug(this.serverProcess);
            this.serverProcess.sendToConsole(MinecraftUtil.tellrawToAllMessage(this.config.getMessages().getDiscordToMinecraftMessage()
                    .replaceAll("<name>", author.getName())
                    .replaceAll("<msg>", rawMessage)));
        }

        if (rawMessage.startsWith(this.prefix)) {
            if (channel != this.textChannel) {
                channel.sendMessage("Polecenia można wykonywać tylko na kanale czatu <#" + this.discordJda.getChannelID() + ">").queue(msg ->
                        msg.delete().queueAfter(5, TimeUnit.SECONDS));
                message.delete().queueAfter(4, TimeUnit.SECONDS);
                return;
            }

            final String command = rawMessage.substring(this.prefix.length());
            channel.sendMessage(command).queue(msg ->
                    msg.delete().queueAfter(5, TimeUnit.SECONDS));
            message.delete().queueAfter(4, TimeUnit.SECONDS);
            switch (command.toLowerCase()) {
                case "help", "pomoc" -> {
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Lista poleceń")
                            .setDescription("`" + this.prefix + "help` - lista poleceń\n" +
                                    "`" + this.prefix + "list` - lista graczy online\n" +
                                    "`" + this.prefix + "ping` - aktualny ping bota\n" +
                                    "`" + this.prefix + "uptime` - czas działania aplikacij i bota\n" +
                                    "`" + this.prefix + "ip` - ip ustawione w config\n"
                            )
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(10, TimeUnit.SECONDS);
                        event.getMessage().delete().queueAfter(9, TimeUnit.SECONDS);
                    });
                }
                case "ping" -> {
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Ping Bot <-> Discord")
                            .setDescription("Aktualny ping z serverami discord wynosi: " + this.jda.getGatewayPing() + " ms")
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(10, TimeUnit.SECONDS);
                        event.getMessage().delete().queueAfter(9, TimeUnit.SECONDS);
                    });
                }
                case "ip" -> {
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Nasze ip!")
                            .setDescription(MessageUtil.listToSpacedString(this.config.getDiscordBot().getIpMessage()))
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(10, TimeUnit.SECONDS);
                        event.getMessage().delete().queueAfter(9, TimeUnit.SECONDS);
                    });
                }
                case "uptime" -> {
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Czas działania")
                            .setDescription("Czas działania aplikacij `" + MathUtil.formatTime(System.currentTimeMillis() - this.bdsAutoEnable.getStartTime()) + "`\n" +
                                    "Czas działania servera `" + MathUtil.formatTime(System.currentTimeMillis() - this.serverProcess.getStartTime()) + "`\n")
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(10, TimeUnit.SECONDS);
                        event.getMessage().delete().queueAfter(9, TimeUnit.SECONDS);
                    });
                }

                case "list" -> {
                    final List<String> players = this.bdsAutoEnable.getPlayerManager().getOnlinePlayers();
                    final MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Lista Graczy")
                            .setDescription(players.size() + "/" + this.bdsAutoEnable.getServerProperties().getMaxPlayers() + "\n`" +
                                    players.toString().replaceAll("\\[\\]", "") + "`\n")
                            .setColor(Color.BLUE)
                            .setFooter("Wywołane przez " + author.getName(), author.getEffectiveAvatarUrl())
                            .build();

                    event.getChannel().sendMessageEmbeds(embed).queue(msg -> {
                        msg.delete().queueAfter(10, TimeUnit.SECONDS);
                        event.getMessage().delete().queueAfter(9, TimeUnit.SECONDS);
                    });
                }
                default -> event.getChannel().sendMessage("Nieznana komenda: " + command).queue(msg -> {
                    msg.delete().queueAfter(5, TimeUnit.SECONDS);
                    message.delete().queue();
                });
            }
        }
    }


}
