package me.indian.bds.gui;

import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;

public class GuiManager extends JFrame {

    private final List<JPanel> cards;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;

    public GuiManager(final BDSAutoEnable bdsAutoEnable) {
        this.cards = new LinkedList<>();
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();

    }

    public void init() {
        this.createWindow();
    }

    private void createWindow() {
        this.setTitle("BDS-Auto-Enable");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(610, 450);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);

        final JTabbedPane tabbedPane = new JTabbedPane();

        for (final JPanel card : this.cards) {
            tabbedPane.addTab(card.getName(), card);
        }


        this.getContentPane().add(tabbedPane);
    }

    private void addCard(final JPanel jPanel) {
        this.cards.add(jPanel);
    }

    public void sendNotification(final String message) {
        this.logger.info(message);
    }
}