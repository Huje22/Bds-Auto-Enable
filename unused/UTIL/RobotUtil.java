package me.indian.bds.util;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import org.jetbrains.annotations.Nullable;

public final class RobotUtil {

    private static Logger LOGGER;
    private static Robot ROBOT;

    public static void init(final BDSAutoEnable bdsAutoEnable) {
        LOGGER = bdsAutoEnable.getLogger();

        try {
            if (GraphicsEnvironment.isHeadless()) {
                LOGGER.error("System działa w trybie headless. Operacje graficzne nie są możliwe.");
                return;
            }

            ROBOT = new Robot();
        } catch (final AWTException | HeadlessException exception) {
            LOGGER.error("Nie udało się zainciować&b RobotUtil", exception);
        }
    }

    @Nullable
    public static File screenShot() throws IOException {
        if (GraphicsEnvironment.isHeadless()) {
            LOGGER.error("System działa w trybie headless. Operacje graficzne nie są możliwe.");
            return null;
        }

        if (ROBOT == null) {
            LOGGER.error("Robot nie został zainicjalizowany.");
            return null;
        }

        final Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        final BufferedImage screenCapture = ROBOT.createScreenCapture(screenRect);

        final File file = new File(DateUtil.getFixedDate() + ".png");

        ImageIO.write(screenCapture, "PNG", file);

        LOGGER.info("Zrzut ekranu został zapisany jako&b " + file.getName());

        return file;
    }
}