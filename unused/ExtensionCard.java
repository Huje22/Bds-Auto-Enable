package me.indian.bds.gui;

import java.awt.Font;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.bds.extension.ExtensionManager;
import me.indian.bds.util.MessageUtil;

public class ExtensionCard extends CoordinateGridPanel {

    private final GuiManager guiManager;
    private final ExtensionManager extensionManager;
    private final JTabbedPane tabbedPane;

    public ExtensionCard(final GuiManager guiManager, final BDSAutoEnable bdsAutoEnable) {
        this.guiManager = guiManager;
        this.extensionManager = bdsAutoEnable.getExtensionManager();
        this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        this.tabbedPane.setPreferredSize(guiManager.getSize());

        this.setName("Extensions");
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        this.add(Box.createVerticalStrut(0));

        for (final Extension extension : this.extensionManager.getExtensions().values()) {
            this.addExtension(extension);
        }

        this.add(this.guiManager.scroll(this.tabbedPane));
    }

    private void addExtension(final Extension extension) {
        final JPanel extensionPanel = new JPanel();

        extensionPanel.setPreferredSize(this.guiManager.getSize());
        extensionPanel.setLayout(new BoxLayout(extensionPanel, BoxLayout.Y_AXIS));
        extensionPanel.add(Box.createVerticalStrut(10));

        final String name = extension.getName();
        final String version = extension.getVersion();
        final String prefix = extension.getExtensionDescription().prefix();
        final List<String> dependencies = extension.getExtensionDescription().dependencies();
        final List<String> softDependencies = extension.getExtensionDescription().softDependencies();

        final JLabel versionLabel = new JLabel("Wersja: " + version);
        versionLabel.setFont(new Font("Arial", Font.BOLD, 14));

        final JLabel prefixLabel = new JLabel("Prefix: " + extension.getExtensionDescription().prefix());
        prefixLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        final JLabel descriptionLabel = new JLabel("Opis: " + extension.getDescription());
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        final JLabel mainClassLabel = new JLabel("Główna klasa: " + extension.getExtensionDescription().mainClass());
        mainClassLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        final JLabel authorsLabel = new JLabel("Autorzy: " + MessageUtil.stringListToString(extension.getAuthors(), ", "));
        authorsLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        final JLabel dependenciesLabel = new JLabel("Zależności: " + MessageUtil.stringListToString(dependencies, ", "));
        dependenciesLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        final JLabel softDependenciesLabel = new JLabel("Miękkie zależności: " + MessageUtil.stringListToString(softDependencies, ", "));
        softDependenciesLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        final JCheckBox checkBox = new JCheckBox("Czy rozserzenie jest włączone?", extension.isEnabled());
        checkBox.addActionListener(actionEvent -> {
            if (checkBox.isSelected()) {
                if (this.extensionManager.enableExtension(extension)) {
                    JOptionPane.showMessageDialog(checkBox, "Włączono " + name + " (Wersja: " + version + " Autor: " + extension.getAuthor() + ")",
                            "Włączono rozserzenie", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(checkBox, "Nie udało się włączyć " + name + " (Wersja: " + version + " Autor: " + extension.getAuthor() + ")",
                            "Nieudało się włączyć rozserenia", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                if (this.extensionManager.disableExtension(extension)) {
                    JOptionPane.showMessageDialog(checkBox, "Wyłączono " + name + " (Wersja: " + version + " Autor: " + extension.getAuthor() + ")",
                            "Włączono rozserzenie", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(checkBox, "Nie udało się wyłączyć " + name + " (Wersja: " + version + " Autor: " + extension.getAuthor() + ")",
                            "Nieudało się wyłączyć rozserenia", JOptionPane.ERROR_MESSAGE);
                }
            }

            checkBox.setSelected(extension.isEnabled());
        });

        extensionPanel.add(versionLabel);
        if (!prefix.equals(name)) extensionPanel.add(prefixLabel);
        extensionPanel.add(descriptionLabel);
        extensionPanel.add(mainClassLabel);
        extensionPanel.add(authorsLabel);
        if (!dependencies.isEmpty()) extensionPanel.add(dependenciesLabel);
        if (!softDependencies.isEmpty()) extensionPanel.add(softDependenciesLabel);
        extensionPanel.add(checkBox);

        this.tabbedPane.addTab(name + " " + version, this.guiManager.scroll(extensionPanel));
    }
}