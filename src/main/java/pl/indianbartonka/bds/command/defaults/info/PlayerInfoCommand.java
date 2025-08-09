package pl.indianbartonka.bds.command.defaults.info;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.util.MinecraftUtil;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.util.MessageUtil;

public class PlayerInfoCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public PlayerInfoCommand(final BDSAutoEnable bdsAutoEnable) {
        super("player", "Wszytkie dostępne informacje o graczu");
        this.addOption("<player>", "Gracz ktory ma zostac wyszukany");
        this.bdsAutoEnable = bdsAutoEnable;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.bdsAutoEnable.getWatchDog().getPackModule().isLoaded()) {
            this.sendMessage("&cPaczka &b" + this.bdsAutoEnable.getWatchDog().getPackModule().getPackName() + "&c nie jest załadowana!");
            return true;
        }

        final String playerName = MinecraftUtil.fixPlayerName(MessageUtil.buildMessageFromArgs(args));

        final PlayerStatistics player = this.bdsAutoEnable.getServerManager().getStatsManager().getPlayer(playerName);

        if (player == null) {
            this.sendMessage("&cNie znaleziono informacji na temat gracza:&b " + playerName);
            return true;
        }

        final long xuid = player.getXuid();

        this.sendMessage("&aNick:&b " + playerName);
        this.sendMessage("&aXUID:&b " + xuid);

        this.sendMessage("&aUrządzenie:&b " + player.getPlatformType().getPlatformName());
        this.sendMessage("&aKontroler:&b " + player.getLastKnownInputMode().getMode());
        this.sendMessage("&aMaksymalna Liczba Chunk:&b " + player.getMaxRenderDistance());

        final String memory = switch (player.getMemoryTier().getTier()) {
            case 0 -> "Max 1,5GB";
            case 1 -> "Max 2GB";
            case 2 -> "Max 4GB";
            case 3 -> "Max 8GB";
            case 4 -> "Więcej niż 8GB";
            default -> "Niewiadomo";
        };

        this.sendMessage("&aPoziom Pamięci:&b " + memory);

        final List<String> oldNames = player.getOldNames();
        if (oldNames != null && !oldNames.isEmpty()) {
            this.sendMessage("&aZnany również jako:&b " + MessageUtil.stringListToString(oldNames, " ,"));
        } else {
            this.sendMessage("&aZnany również jako:&b " + "__Brak danych o innych nick__");
        }

        final long firstJoin = player.getFirstJoin();
        final long lastJoin = player.getLastJoin();
        final long lastQuit = player.getLastQuit();

        if (firstJoin != 0 && firstJoin != -1) {
            this.sendMessage("&aPierwsze dołączenie:&b " + this.getTime(DateUtil.millisToLocalDateTime(firstJoin)));
        }
        if (lastJoin != 0 && lastJoin != -1) {
            this.sendMessage("&aOstatnie dołączenie:&b " + this.getTime(DateUtil.millisToLocalDateTime(lastJoin)));
        }

        if (lastQuit != 0 && lastQuit != -1) {
            this.sendMessage("&aOstatnie opuszczenie:&b " + this.getTime(DateUtil.millisToLocalDateTime(lastQuit)));
        }

        this.sendMessage("&aLogin Streak:&b " + player.getLoginStreak());
        this.sendMessage("&aLongest Login Streak:&b " + player.getLoginStreak());

        this.sendMessage("&aŚmierci:&b " + player.getDeaths());
        this.sendMessage("&aCzas gry:&b " + DateUtil.formatTimeDynamic(player.getPlaytime()));
        this.sendMessage("&aPostawione bloki:&b " + player.getBlockPlaced());
        this.sendMessage("&aZniszczone bloki:&b " + player.getBlockBroken());

        if (this.bdsAutoEnable.getServerProperties().isAllowList()) {
            if (this.bdsAutoEnable.getAllowlistManager().isOnAllowList(playerName)) {
                this.sendMessage("&aZnajduje się na białej liście");
            } else {
                this.sendMessage("&aNie znajduje się na białej liście");
            }
        }

        if (isOp) {
            final Map<String, Object> dynamicProperties = player.getDynamicProperties();
            this.sendMessage("&aDynamiczne wartości");
            for (final Map.Entry<String, Object> entry : dynamicProperties.entrySet()) {
                this.sendMessage("&a" + entry.getKey() + "&4 :&b " + entry.getValue());
            }
        }

        return true;
    }

    public String getTime(final LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy\nHH:mm:ss"));
    }
}
