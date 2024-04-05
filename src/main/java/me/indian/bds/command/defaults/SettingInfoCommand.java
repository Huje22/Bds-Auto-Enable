package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.server.properties.ServerProperties;
import me.indian.bds.util.system.SystemUtil;

public class SettingInfoCommand extends Command {

    private final ServerProperties properties;

    public SettingInfoCommand(final BDSAutoEnable bdsAutoEnable) {
        super("setting", "Info o aktualnych ustawieniach servera");
        this.properties = bdsAutoEnable.getServerProperties();
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.commandConfig.isSettingsForAll() && !isOp) {
            this.sendMessage("&aTylko operatorzy mogą zobaczyć aktualne ustawienia servera");
            return true;
        }

        this.properties.loadProperties();

        this.sendMessage("&aSystem:&b " + SystemUtil.getSystem() + " &d(&1" + SystemUtil.getFullyOsName() + "&d)");
        this.sendMessage("&aSystem arch:&b " + SystemUtil.getFullyArchCode() + " &d(&1" + SystemUtil.getCurrentArch() + "&d)");

        this.sendMessage("&eNie wszystkie wartości muszą być załadowane przez server");

        this.sendMessage("&aMaksymalny zasięg widoku:&b " + this.properties.getViewDistance() + "&e chunk");
        this.sendMessage("&aMaksymalny zasięg ticków:&b " + this.properties.getTickDistance() + "&e chunk");
        this.sendMessage("&aMaksymalna ilość wątków których może użyć server:&b " + this.properties.getMaxThreads());
        this.sendMessage("&aMaksymalny czas bycia AFK:&b " + this.properties.getPlayerIdleTimeout() + "&e minut");

        this.sendMessage("&aAutoryzacja ruchu gracza:&b " + this.properties.getServerMovementAuth().getAuthName());

        this.sendMessage("&aCzy paczki są wymagane do pobrania:&b " + this.properties.isTexturePackRequired());
        this.sendMessage("&aCzy server emituje telemetrie:&b " + this.properties.isServerTelemetry());

        this.sendMessage("&aIle procent chunk może wygenerować ci server:&b " + this.calculateBuildRadiusRatio());
        this.sendMessage("&aAlgorytm kompresji pakietów:&b " + this.properties.getCompressionAlgorithm().getAlgorithmName());

        this.sendMessage("&eNie wszystkie wartości muszą być załadowane przez server");

        return true;
    }

    private String calculateBuildRadiusRatio() {
        final double ratio = this.properties.getServerBuildRadiusRatio();
        if (ratio == -1.0 || ratio == 0.0) return "Klient sam generuje chunk";
        return (int) (ratio * 100) + "%";
    }
}
