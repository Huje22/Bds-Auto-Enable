package pl.indianbartonka.bds.command.defaults.info;

import java.util.List;
import java.util.Map;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.extension.Extension;
import pl.indianbartonka.bds.extension.ExtensionManager;
import pl.indianbartonka.util.MessageUtil;

public class ExtensionsCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final ExtensionManager extensionManager;

    public ExtensionsCommand(final BDSAutoEnable bdsAutoEnable) {
        super("extension", "Pokazuje wgrane rozszerzenia");
        this.bdsAutoEnable = bdsAutoEnable;
        this.extensionManager = this.bdsAutoEnable.getExtensionManager();

        this.addAlliases(List.of("ex"));
        this.addOption("[extension name]", "Informacje o danym rozszerzeniu");
        this.addOption("enable <extension name>", "Włącza dane rozszerzenie &cNIEBEZPIECZNE");
        this.addOption("disable <extension name>", "Wyłącza dane rozszerzenie &cNIEBEZPIECZNE");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.commandConfig.isExtensionsForAll() && !isOp) {
            this.sendMessage("&aTylko operatorzy mogą zobaczyć aktualne rozszerzenia");
            this.deniedSound();
            return true;
        }

        final Map<String, Extension> extensions = this.bdsAutoEnable.getExtensionManager().getExtensions();

        if (args.length == 0) {
            String status = "&a(&r" + extensions.size() + "&a)&r ";

            int counter = 0;
            for (final Map.Entry<String, Extension> entry : extensions.entrySet()) {
                final Extension extension = entry.getValue();
                status += this.statusColor(extension.isEnabled()) + extension.getName()
                        + " &b" + extension.getVersion() + "&6"
                        + (counter < extensions.size() - 1 ? ", " : "");
                counter++;
            }

            this.sendMessage(status);
            return true;
        }

        if (args.length == 1) {
            final String extensionName = args[0];

            final Extension extension = this.bdsAutoEnable.getExtensionManager().getExtension(extensionName);

            if (extension != null) {
                final List<String> dependencies = extension.getExtensionDescription().dependencies();
                final List<String> softDependencies = extension.getExtensionDescription().softDependencies();
                final String prefix = extension.getExtensionDescription().prefix();

                this.sendMessage("&aNazwa:&b " + extension.getName());
                if (!prefix.equalsIgnoreCase(extension.getName())) {
                    this.sendMessage("&aPrefix:&b " + prefix);
                }
                this.sendMessage("&aWersja:&b " + extension.getVersion());
                this.sendMessage("&aAutorzy&b " + MessageUtil.stringListToString(extension.getAuthors(), "&a,&b "));
                this.sendMessage("&aOpis:&b " + extension.getDescription());
                this.sendMessage("&aKlasa główna:&6 " + extension.getExtensionDescription().mainClass());
                if (!dependencies.isEmpty()) {
                    this.sendMessage("&aZależności:&b " + dependencies);
                }

                if (!softDependencies.isEmpty()) {
                    this.sendMessage("&aMiękkie Zależności&b " + softDependencies);
                }
                return true;
            }

            this.sendMessage("&cNie znaleziono rozszerzenia&b " + extensionName);
            return true;
        }

        if (args.length == 2) {
            if (!isOp) {
                this.sendMessage("&cPotrzebujesz wyższych uprawnień");
                return true;
            }

            final String extensionName = args[1];
            final Extension extension = this.extensionManager.getExtension(extensionName);

            if (args[0].equalsIgnoreCase("enable")) {
                if (extension != null) {
                    if (extension.isEnabled()) {
                        this.sendMessage("&cTe rozszerzenie jest już włączone");
                        return true;
                    }

                    this.sendMessage("&aWłączanie&b " + extension.getName() + "&r...");
                    this.extensionManager.enableExtension(extension);
                    return true;
                }
                this.sendMessage("&cNie znaleziono rozszerzenia&b " + extensionName);
                return true;
            }

            if (args[0].equalsIgnoreCase("disable")) {
                if (extension != null) {
                    if (!extension.isEnabled()) {
                        this.sendMessage("&cTe rozszerzenie jest już wyłączone");
                        return true;
                    }

                    this.sendMessage("&aWyłączanie&b " + extension.getName() + "&r...");
                    this.extensionManager.disableExtension(extension);
                    return true;
                }
                this.sendMessage("&cNie znaleziono rozszerzenia&b " + extensionName);
                return true;
            }
        }
        return false;
    }

    private String statusColor(final boolean enabled) {
        return enabled ? "&a" : "&c";
    }
}