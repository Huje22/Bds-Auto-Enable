package me.indian.bds.command.defaults;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.extension.Extension;

import java.util.List;

public class ExtensionCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;

    public ExtensionCommand(final BDSAutoEnable bdsAutoEnable) {
        super("extensions", "Pokazuje wgrane rozszerzenia");
        this.bdsAutoEnable = bdsAutoEnable;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.commandConfig.isExtensionsForAll() && !isOp) {
            this.sendMessage("&aTylko operatorzy mogą zobaczyć aktualne ustawienia servera");
            return true;
        }

        final List<Extension> extensions = this.bdsAutoEnable.getExtensionLoader().getExtensions();

        if (args.length == 0) {
            String status = "&a(&r" + extensions.size() + "&a)&r ";

            int counter = 0;
            for (final Extension extension : extensions) {
                status += this.statusColor(extension.isEnabled()) + extension.getName() + (counter < extensions.size() - 1 ? "." : "");
                counter++;
            }

            this.sendMessage(status);
        }

        return false;
    }

    private String statusColor(final boolean enabled) {
        return enabled ? "&a" : "&r";
    }
}