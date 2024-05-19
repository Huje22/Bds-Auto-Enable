package me.indian.bds.gui;

import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

public class AppCard extends CoordinateGridPanel {

    public AppCard() {
        this.setName("Główna karta");
        this.init();
    }

    private void init(){
        final JLabel jLabel = new JLabel("BDS-Auto-Enable");
        final JLabel jLabel1 = new JLabel("To UI jest w trakcie tworzenia....");

        jLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        jLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.add(jLabel);
        this.add(jLabel1);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }
}