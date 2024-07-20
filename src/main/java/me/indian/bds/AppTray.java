package me.indian.bds;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;

public class AppTray {

    private final SystemTray systemTray;
    private final Image huje22Logo;
    private final PopupMenu popupMenu;
    private final TrayIcon trayIcon;

    public AppTray() {
        try {
            this.systemTray = SystemTray.getSystemTray();
            this.huje22Logo = ImageIO.read(Objects.requireNonNull(AppTray.class.getClassLoader().getResourceAsStream("Huje22.jpg")));
            this.popupMenu = new PopupMenu();
            this.trayIcon = new TrayIcon(this.huje22Logo, "BDS-Auto-Enable", this.popupMenu);

            this.addMenuItems();

            this.trayIcon.setImageAutoSize(true);
            this.systemTray.add(this.trayIcon);
        } catch (final IOException | AWTException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void sendMessage(final String caption, final String text, final TrayIcon.MessageType messageType) {
        if (!SystemTray.isSupported()) return;
        this.trayIcon.displayMessage(caption, text, messageType);
    }

    private void addMenuItems() {
        final MenuItem exitItem = new MenuItem("Zamknij");
        exitItem.addActionListener(e -> {
            this.systemTray.remove(this.trayIcon);
            System.exit(0);
        });

        this.popupMenu.add(exitItem);
    }
}
