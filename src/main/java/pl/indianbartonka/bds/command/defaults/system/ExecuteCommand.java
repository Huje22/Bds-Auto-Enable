package pl.indianbartonka.bds.command.defaults.system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.util.MinecraftUtil;
import pl.indianbartonka.util.MessageUtil;

public class ExecuteCommand extends Command {

    public ExecuteCommand() {
        super("execute", "Wykonaj polecenie w systemie");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (isOp) {
            try {
                final ProcessBuilder processBuilder = new ProcessBuilder(args);
                final Process process = processBuilder.start();

                try (final BufferedReader reader = process.inputReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        this.sendMessage(line);
                    }
                }

                if (process.waitFor(90, TimeUnit.SECONDS)) {
                    this.sendMessage("&aKoniec procesu");
                } else {
                    this.sendMessage("&cNie udało się wykonać procesu w czasie&b 90 sekund");
                    process.destroy();
                }
            } catch (final Exception exception) {
                this.sendMessage("&cNie udało się wykonać polecenia!");
                this.sendMessage(MinecraftUtil.fixMessage(MessageUtil.getStackTraceAsString(exception)));
            }
        } else {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
        }
        return true;
    }
}