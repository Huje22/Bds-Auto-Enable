package me.indian.bds.command.defaults;

import java.util.List;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.extension.Extension;
import me.indian.bds.extension.ExtensionLoader;
import me.indian.bds.util.MessageUtil;

public class ExtensionsCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final ExtensionLoader extensionLoader;

    public ExtensionsCommand(final BDSAutoEnable bdsAutoEnable) {
        super("extension", "Pokazuje wgrane rozszerzenia");
        this.bdsAutoEnable = bdsAutoEnable;
        this.extensionLoader = this.bdsAutoEnable.getExtensionLoader();

        this.addAlliases(List.of("ex"));
        this.addOption("[extension name]", "Informacje o danym rozserzeniu");
        this.addOption("enable <extension name>", "Włącza dane rozserzenie &cNIEBEZPIECZNE");
        this.addOption("disable <extension name>", "Wyłącza dane rozserzenie &cNIEBEZPIECZNE");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.commandConfig.isExtensionsForAll() && !isOp) {
            this.sendMessage("&aTylko operatorzy mogą zobaczyć aktualne rozserzenia");
            return true;
        }

        final Map<String, Extension> extensions = this.bdsAutoEnable.getExtensionLoader().getExtensions();

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

            final Extension extension = this.bdsAutoEnable.getExtensionLoader().getExtension(extensionName);

            if (extension != null) {
                final List<String> dependencies = extension.getExtensionDescription().dependencies();
                final List<String> softDependencies = extension.getExtensionDescription().softDependencies();

                this.sendMessage("&aVersia:&b " + extension.getVersion());
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

            if (args[0].equalsIgnoreCase("enable")) {
                final String extensionName = args[1];
                final Extension extension = this.extensionLoader.getExtension(extensionName);

                if (extension != null) {
                    if (extension.isEnabled()) {
                        this.sendMessage("&cTe rozserzenie jest już włączone");
                        return true;
                    }

                    this.extensionLoader.enableExtension(extension);
                    return true;
                }
                this.sendMessage("&cNie znaleziono rozszerzenia&b " + extensionName);
                return true;
            }

            if (args[0].equalsIgnoreCase("disable")) {
                if (!isOp) {
                    this.sendMessage("&cPotrzebujesz wyższych uprawnień");
                    return true;
                }


                final String extensionName = args[1];
                final Extension extension = this.extensionLoader.getExtension(extensionName);

                if (extension != null) {
                    if (!extension.isEnabled()) {
                        this.sendMessage("&cTe rozserzenie jest już wyłączone");
                        return true;
                    }

                    this.extensionLoader.disableExtension(extension);
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