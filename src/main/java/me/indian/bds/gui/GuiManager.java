package me.indian.bds.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;

public class GuiManager extends JFrame {

    private final List<CoordinateGridPanel> cards;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Dimension size;
    private final boolean debug;

    public GuiManager(final BDSAutoEnable bdsAutoEnable) {
        this.cards = new LinkedList<>();
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.size = new Dimension(737, 463);
        this.debug = this.bdsAutoEnable.getAppConfigManager().getAppConfig().isDebug();
    }

    public void init() {
        this.addCard(new AppCard());
        this.addCard(new ExtensionCard(this, this.bdsAutoEnable));
        this.createWindow();
    }

    private void createWindow() {
        this.setTitle("BDS-Auto-Enable");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(this.size);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);

        final JTabbedPane tabbedPane = new JTabbedPane();

        for (final CoordinateGridPanel card : this.cards) {
            card.setDebug(this.debug);
            tabbedPane.addTab(card.getName(), card);
        }

        this.getContentPane().add(tabbedPane);

        this.debug();
    }

    private void addCard(final CoordinateGridPanel coordinateGridPanel) {
        this.cards.add(coordinateGridPanel);
    }

    public JScrollPane scroll(final Component component) {
        final JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    @Override
    public Dimension getSize() {
        return this.size;
    }

    private void debug() {
        if (!this.debug) return;
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                final Dimension size = e.getComponent().getSize();
                GuiManager.this.logger.debug("x:" + size.width + " y:" + size.height);
            }
        });

        this.setResizable(true);
    }
}