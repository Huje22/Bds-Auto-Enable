package pl.indianbartonka.bds.util;

import java.util.ArrayList;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.server.ServerManager;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.util.logger.LogState;
import pl.indianbartonka.util.logger.Logger;

public final class ServerUtil {

    private static Logger LOGGER;
    private static ServerProcess SERVER_PROCESS;
    private static ServerManager SERVER_MANAGER;

    private ServerUtil() {

    }

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        LOGGER = bdsAutoEnable.getLogger();
        SERVER_PROCESS = bdsAutoEnable.getServerProcess();
        SERVER_MANAGER = bdsAutoEnable.getServerManager();
    }

    public static String sendCommandAsPlayer(final String playerName, final String command) {
        return SERVER_PROCESS.commandAndResponse("execute as " + playerName + " run " + command);
    }

    public static void kickAllPlayers(final String msg) {
        if (SERVER_MANAGER.getOnlinePlayers().isEmpty()) {
            LOGGER.debug("Lista graczy jest pusta");
            return;
        }
        new ArrayList<>(SERVER_MANAGER.getOnlinePlayers()).forEach(name -> kick(name, msg));
    }

    public static void kick(final String who, final String reason) {
        if (SERVER_MANAGER.getOnlinePlayers().isEmpty()) {
            LOGGER.debug("Lista graczy jest pusta");
            return;
        }
        SERVER_PROCESS.sendToConsole("kick " + who + " " + MinecraftUtil.colorize(reason));
    }

    public static void transferPlayer(final String playerName, final String address, final int port) {
        SERVER_PROCESS.sendToConsole("transfer " + playerName + " " + address + " " + port);
    }

    public static void transferPlayer(final String playerName, final String address) {
        transferPlayer(playerName, address, 19132);
    }

    public static void setPlayerPrefix(String playerName, String prefix) {
        SERVER_PROCESS.sendToConsole("scriptevent bds:tag_prefix " + playerName.replaceAll("\"", "") + "=" + MinecraftUtil.colorize(prefix) + " ");
    }

    public static void setPlayerBelowName(String playerName, String belowName) {
        SERVER_PROCESS.sendToConsole("scriptevent bds:tag_belowName " + playerName.replaceAll("\"", "") + "=" + MinecraftUtil.colorize(belowName));
    }

    public static void tellrawToAll(final String msg) {
        if (SERVER_MANAGER.getOnlinePlayers().isEmpty()) {
            LOGGER.debug("Lista graczy jest pusta");
            return;
        }

        tellrawToPlayer("@a", msg);
    }

    public static void tellrawToPlayer(final String playerName, final String msg) {
        if (SERVER_MANAGER.getOnlinePlayers().isEmpty()) {
            LOGGER.debug("Lista graczy jest pusta");
            return;
        }

        final String msg2 = MinecraftUtil.fixMessage(msg, true).replace("\"", "\\\"");

        SERVER_PROCESS.sendToConsole(MinecraftUtil.colorize("tellraw " + playerName + " {\"rawtext\":[{\"text\":\"" + msg2 + "\"}]}"));
    }

    public static void tellrawToAllAndLogger(final String prefix, final String msg, final LogState logState) {
        tellrawToAllAndLogger(prefix, msg, null, logState);
    }

    public static void tellrawToAllAndLogger(final String prefix, final String msg, final Throwable throwable, final LogState logState) {
        LOGGER.logByState("[To Minecraft] " + msg, throwable, logState);
        if (!SERVER_MANAGER.getOnlinePlayers().isEmpty()) tellrawToAll(prefix + " " + msg);
    }

    public static void titleToPlayer(final String playerName, final String message) {
        if (SERVER_MANAGER.getOnlinePlayers().isEmpty()) return;
        SERVER_PROCESS.sendToConsole("title " + playerName + " title " + MinecraftUtil.colorize(message));
    }

    public static void titleToPlayer(final String playerName, final String message, final String subTitle) {
        if (SERVER_MANAGER.getOnlinePlayers().isEmpty()) return;
        SERVER_PROCESS.sendToConsole("title " + playerName + " subtitle " + MinecraftUtil.colorize(subTitle));
        titleToPlayer(playerName, message);
    }

    public static void titleToAll(final String message) {
        titleToPlayer("@a", message);
    }

    public static void titleToAll(final String message, final String subTitle) {
        titleToPlayer("@a", message, subTitle);
    }

    public static void actionBarToPlayer(final String playerName, final String message) {
        if (SERVER_MANAGER.getOnlinePlayers().isEmpty()) return;
        SERVER_PROCESS.sendToConsole("title " + playerName + " actionbar " + MinecraftUtil.colorize(message));
    }

    public static void actionBarToAll(final String message) {
        actionBarToPlayer("@a", message);
    }

    public static void playSoundToPlayer(final String playerName, final String soundName) {
        SERVER_PROCESS.sendToConsole("playsound " + soundName + " " + playerName);
    }

    public static void playSoundToAll(final String soundName) {
        playSoundToPlayer("@a", soundName);
    }

}
