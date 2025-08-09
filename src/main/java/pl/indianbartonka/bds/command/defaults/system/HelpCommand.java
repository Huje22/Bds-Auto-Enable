package pl.indianbartonka.bds.command.defaults.system;

import java.util.Map;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.extension.Extension;

public class HelpCommand extends Command {

    private final Map<Command, Extension> commandMap;

    public HelpCommand(final Map<Command, Extension> commandMap) {
        super("help", "Lista pomocnych poleceń");
        this.commandMap = commandMap;
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        this.sendMessage("&a---------------------");
        this.commandMap.forEach((command, extension) -> this.sendMessage("&a" + command.getName() + " &4-&b " + command.getDescription() + " &e" + this.getExtensionInfo(extension, isOp)));
        this.sendMessage("&a---------------------");
        return true;
    }

    private String getExtensionInfo(final Extension extension, final boolean isOp) {
        String info = "";

        if (extension != null && isOp) {
            info = extension.getName() + "&r (Wersja:&a " + extension.getVersion() + "&r Autor:&a " + extension.getAuthor() + "&r)";
        }

        return info;
    }
}