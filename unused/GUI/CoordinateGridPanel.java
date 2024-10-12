package pl.indianbartonka.bds.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

public class CoordinateGridPanel extends JPanel {

    private int gridSize;
    private boolean debug;

    public CoordinateGridPanel() {
        this.gridSize = 25;
        this.setName("CoordinateGridPanel");
        this.handlePopUp();
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

    private void handlePopUp() {
        final JPopupMenu popupMenu = new JPopupMenu();
        final JMenuItem menuItem1 = new JMenuItem("Może coś kiedyś ");
        final JMenuItem menuItem2 = new JMenuItem("tu będzie");
        popupMenu.add(menuItem1);
        popupMenu.add(menuItem2);

        menuItem1.addActionListener(actionEvent -> {
            System.out.println("Wybrano opcję 1");
        });


        menuItem2.addActionListener(actionEvent -> {
            System.out.println("Wybrano opcję 2");
        });

        this.setComponentPopupMenu(popupMenu);
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
}