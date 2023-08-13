package me.indian.bds.util;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.manager.PlayerManager;

public class MinecraftUtil {

    private static Logger logger;
    private static ServerProcess serverProcess;
    private static PlayerManager playerManager;

    public MinecraftUtil() {
    }

    public static void initMinecraftUtil(final BDSAutoEnable bdsAutoEnable) {
        logger = bdsAutoEnable.getLogger();
        serverProcess = bdsAutoEnable.getServerProcess();
        playerManager = bdsAutoEnable.getPlayerManager();
    }


    public static String colorize(final String msg) {
        return msg.replaceAll("&", "§");
    }

    public static String tellrawToAllMessage(final String msg) {
        return colorize("tellraw @a {\"rawtext\":[{\"text\":\"" + msg + "\"}]}");
    }

    public static void tellrawToAllAndLogger(final String prefix, final String msg, final LogState logState) {
        logger.logByState("[To Minecraft] " + ConsoleColors.convertMinecraftColors(msg) + ConsoleColors.RESET, logState);
        if (!playerManager.getOnlinePlayers().isEmpty()) serverProcess.sendToConsole(tellrawToAllMessage(prefix + " " + msg));
    }

    public static String kickCommand(final String who, final String reason) {
        return "kick " + who + " " + colorize(reason);
    }
}
