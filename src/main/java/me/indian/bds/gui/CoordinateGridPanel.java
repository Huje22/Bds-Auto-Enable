package me.indian.bds.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class CoordinateGridPanel extends JPanel {

    private int gridSize;
    private boolean debug;

    public CoordinateGridPanel() {
        this.gridSize = 25;
    }

    public void setGridSize(final int gridSize) {
        this.gridSize = gridSize;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        if (this.debug) {
            super.paintComponent(g);

            g.setColor(Color.LIGHT_GRAY);

            for (int x = 0; x <= this.getWidth(); x += this.gridSize) {
                for (int y = 0; y <= this.getHeight(); y += this.gridSize) {
                    g.drawRect(x, y, this.gridSize, this.gridSize);
                    if (x % (this.gridSize * 2) == 0 && y % (this.gridSize * 2) == 0) {
                        g.drawString("(" + x + ", " + y + ")", x + 2, y + this.gridSize - 2);
                    }
                }
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(737, 463);
    }
}