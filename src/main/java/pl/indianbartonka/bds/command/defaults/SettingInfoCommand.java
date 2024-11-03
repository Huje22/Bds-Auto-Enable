package pl.indianbartonka.bds.command.defaults;

import java.util.List;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.server.properties.ServerProperties;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.system.SystemUtil;


public class SettingInfoCommand extends Command {

    private final ServerProperties properties;

    public SettingInfoCommand(final BDSAutoEnable bdsAutoEnable) {
        super("setting", "Info o aktualnych ustawieniach servera");
        this.properties = bdsAutoEnable.getServerProperties();

        this.addAlliases(List.of("settings"));
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.commandConfig.isSettingsForAll() && !isOp) {
            this.sendMessage("&aTylko operatorzy mogą zobaczyć aktualne ustawienia servera");
            this.deniedSound();
            return true;
        }

        this.properties.loadProperties();

        this.sendMessage("System:&b " + SystemUtil.getSystem() + " &d(&1" + SystemUtil.getFullOSNameWithDistribution() + "&d)");
        this.sendMessage("System arch:&b " + SystemUtil.getFullyArchCode() + " &d(&1" + SystemUtil.getCurrentArch() + "&d)");

        this.sendMessage("&eNie wszystkie wartości muszą być załadowane przez server");

        this.sendMessage("Maksymalny zasięg widoku:&b " + this.properties.getViewDistance() + "&e chunk");
        this.sendMessage("Maksymalny zasięg ticków:&b " + this.properties.getTickDistance() + "&e chunk");

        final int threadsCount = this.properties.getMaxThreads();
        final int logicalThreadsCount = ThreadUtil.getLogicalThreads();
        String threadsNote = "";

        if (threadsCount != 0 && threadsCount != logicalThreadsCount) {
            threadsNote = "&d (&bDostępne jest:&1 " + logicalThreadsCount + "&d)";
        } else if (threadsCount == 0) {
            threadsNote = "&d (&bPosiadasz:&1 " + logicalThreadsCount + "&d)";
        }

        this.sendMessage("Maksymalna ilość wątków których może użyć server:&b " + threadsCount + threadsNote);
        this.sendMessage("Maksymalny czas bycia AFK:&b " + this.properties.getPlayerIdleTimeout() + "&e minut");

        this.sendMessage("Autoryzacja ruchu gracza:&b " + this.properties.getServerMovementAuth().getAuthName());

        this.sendMessage("Czy paczki są wymagane do pobrania:&b " + this.properties.isTexturePackRequired());
        this.sendMessage("Czy server emituje telemetrie:&b " + this.properties.isServerTelemetry());

        this.sendMessage("Ile procent chunk może wygenerować ci server:&b " + this.calculateBuildRadiusRatio());
        this.sendMessage("Algorytm kompresji pakietów:&b " + this.properties.getCompressionAlgorithm().getAlgorithmName());
        this.sendMessage("Próg kompresji:&1 " + this.properties.getCompressionThreshold());

        this.sendMessage("&eNie wszystkie wartości muszą być załadowane przez server");

        return true;
    }

    private String calculateBuildRadiusRatio() {
        final double ratio = this.properties.getServerBuildRadiusRatio();
        if (ratio == -1.0 || ratio == 0.0) return "Klient sam generuje chunk";
        return (int) (ratio * 100) + "%";
    }
}
