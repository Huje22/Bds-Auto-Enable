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

public class GuiManager {

    private final JFrame frame;
    private final List<CoordinateGridPanel> cards;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final Dimension size;
    private final JTabbedPane tabbedPane;
    private final boolean debug;

    public GuiManager(final BDSAutoEnable bdsAutoEnable) {
        this.frame = new JFrame("BDS-Auto-Enable");
        this.cards = new LinkedList<>();
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.size = new Dimension(737, 463);
        this.tabbedPane = new JTabbedPane();
        this.debug = this.bdsAutoEnable.getAppConfigManager().getAppConfig().isDebug();
        this.frame.getContentPane().add(this.tabbedPane);
    }

    public void init() {
        this.createWindow();
        this.addCard(new AppCard());
        this.addCard(new ServerCard(this, this.bdsAutoEnable));
        this.addCard(new ExtensionCard(this, this.bdsAutoEnable));
        if (this.debug) this.addCard(new CoordinateGridPanel());
    }

    private void createWindow() {
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(this.size);
        this.frame.setLocationRelativeTo(null);
        this.frame.setResizable(false);
        this.frame.setVisible(true);


        this.debug();
    }

    public void addCard(final CoordinateGridPanel coordinateGridPanel) {
        this.cards.add(coordinateGridPanel);
        coordinateGridPanel.setDebug(this.debug);
        this.tabbedPane.addTab(coordinateGridPanel.getName(), coordinateGridPanel);
    }

    public JScrollPane scroll(final Component component) {
        final JScrollPane scrollPane = new JScrollPane(component);

        if (this.debug) scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);


        return scrollPane;
    }

    public Dimension getSize() {
        return this.size;
    }

    private void debug() {
        if (!this.debug) return;
        this.frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                final Dimension size = e.getComponent().getSize();
                GuiManager.this.logger.debug("x:" + size.width + " y:" + size.height);
            }
        });

        this.frame.setResizable(true);
    }
}