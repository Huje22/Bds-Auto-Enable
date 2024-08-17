package me.indian.bds.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.sub.watchdog.WatchDogConfig;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.watchdog.WatchDog;

public class ServerCard extends CoordinateGridPanel {

    private final GuiManager guiManager;
    private final BDSAutoEnable bdsAutoEnable;
    private final WatchDog watchDog;
    private final WatchDogConfig watchDogConfig;
    private final ServerProcess serverProcess;
    private final JButton enableButton, disableButton;

    public ServerCard(final GuiManager guiManager, final BDSAutoEnable bdsAutoEnable) {
        this.guiManager = guiManager;
        this.bdsAutoEnable = bdsAutoEnable;
        this.watchDog = bdsAutoEnable.getWatchDog();
        this.watchDogConfig = bdsAutoEnable.getAppConfigManager().getWatchDogConfig();
        this.serverProcess = bdsAutoEnable.getServerProcess();
        this.enableButton = new JButton("Włącz server");
        this.disableButton = new JButton("Wyłącz server");

        this.setName("Server");
        this.init();
    }

    private void init() {
        final JPanel containerPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.CENTER;

        containerPanel.add(this.handleServerEnablePanel(), constraints);

        constraints.gridy = 1;
        constraints.weighty = 0.0;

        containerPanel.add(this.handleServerStatsPanel(), constraints);

        this.add(containerPanel);
    }


    private JPanel handleServerStatsPanel() {
        final JPanel serverStatsPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        serverStatsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                serverStatsPanel.removeAll();

                gbc.anchor = GridBagConstraints.CENTER;

                ServerCard.this.addLabel(serverStatsPanel, gbc, "--Statystyki servera--");

                gbc.anchor = GridBagConstraints.WEST;

                ServerCard.this.addLabel(serverStatsPanel, gbc, "Ostatnie TPS: " + ServerCard.this.bdsAutoEnable.getServerManager().getLastTPS());
                ServerCard.this.addLabel(serverStatsPanel, gbc, "Pamięć RAM: " + MathUtil.formatKiloBytesDynamic(StatusUtil.getServerRamUsage(), true));

                if (ServerCard.this.watchDogConfig.getAutoRestartConfig().isEnabled()) {
                    ServerCard.this.addLabel(serverStatsPanel, gbc, "Następny restart za: " + DateUtil.formatTimeDynamic(ServerCard.this.watchDog.getAutoRestartModule().calculateMillisUntilNextRestart()));
                }
                if (ServerCard.this.watchDogConfig.getBackupConfig().isEnabled()) {
                    ServerCard.this.addLabel(serverStatsPanel, gbc, "Następny backup za: " + DateUtil.formatTimeDynamic(ServerCard.this.watchDog.getBackupModule().calculateMillisUntilNextBackup()));
                }
                if (ServerCard.this.serverProcess.isEnabled()) {
                    ServerCard.this.addLabel(serverStatsPanel, gbc, "Czas działania: " + DateUtil.formatTimeDynamic(System.currentTimeMillis() - ServerCard.this.serverProcess.getStartTime()));
                }

                serverStatsPanel.revalidate();
                serverStatsPanel.repaint();
            }
        };

        new Timer().scheduleAtFixedRate(timerTask, 1000, 1000);

        return serverStatsPanel;
    }

    private void addLabel(final JPanel panel, final GridBagConstraints gbc, final JLabel label) {
        gbc.gridy++;
        panel.add(label, gbc);
    }

    private void addLabel(final JPanel panel, final GridBagConstraints gbc, final String text) {
        final JLabel label = new JLabel(text);
        gbc.gridy++;
        panel.add(label, gbc);
    }

    private JPanel handleServerEnablePanel() {
        final JPanel serverEnablePanel = new JPanel();
        serverEnablePanel.setLayout(new BoxLayout(serverEnablePanel, BoxLayout.X_AXIS));
        serverEnablePanel.add(Box.createVerticalStrut(10));

        serverEnablePanel.add(this.enableButton);
        serverEnablePanel.add(this.disableButton);

        this.handleServerEnableButton();
        this.handleServerDisableButton();

        return serverEnablePanel;
    }

    private void handleServerEnableButton() {
        this.enableButton.addActionListener(actionEvent -> {
            this.serverProcess.setCanRun(true);
            this.serverProcess.startProcess();
            this.disableButton.setEnabled(true);
            this.enableButton.setEnabled(false);

            JOptionPane.showMessageDialog(this.enableButton, "Włączono server!", "Włączono server!", JOptionPane.INFORMATION_MESSAGE);
        });

        this.enableButton.setEnabled(!this.serverProcess.isEnabled());
    }

    private void handleServerDisableButton() {
        this.disableButton.addActionListener(actionEvent -> {
            this.serverProcess.setCanRun(false);
            try {
                JOptionPane.showMessageDialog(this.disableButton, "Wyłączanie...", "Server jest w trakcie wyłączania....", JOptionPane.INFORMATION_MESSAGE);
                this.serverProcess.disableServer();
                this.disableButton.setEnabled(false);
                this.enableButton.setEnabled(true);
            } catch (final InterruptedException exception) {
                JOptionPane.showMessageDialog(this.disableButton, "Nie udało się wyłączyć servera!",
                        "Nie udało się wyłączyć servera!\n" + MessageUtil.getStackTraceAsString(exception), JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this.enableButton, "Wyłączono server!", "Wyłączono server!", JOptionPane.INFORMATION_MESSAGE);
        });

        this.disableButton.setEnabled(this.serverProcess.isEnabled());
    }
}