package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.extension.Extension;
import me.indian.bds.util.MessageUtil;

import java.util.List;
import java.util.Map;

public class ExtensionsCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public ExtensionsCommand(final BDSAutoEnable bdsAutoEnable) {
        super("extension", "Pokazuje wgrane rozszerzenia");
        this.bdsAutoEnable = bdsAutoEnable;

        this.addAlliases(List.of("ex"));
        this.addOption("[extension name]", "informacje o danej wtyczce");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.commandConfig.isExtensionsForAll() && !isOp) {
            this.sendMessage("&aTylko operatorzy mogą zobaczyć aktualne ustawienia servera");
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
        } else if (args.length == 1) {
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
        return false;
    }

    private String statusColor(final boolean enabled) {
        return enabled ? "&a" : "&c";
    }
}