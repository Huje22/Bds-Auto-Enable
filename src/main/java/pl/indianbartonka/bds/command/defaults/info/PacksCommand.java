package pl.indianbartonka.bds.command.defaults.info;

import java.util.Arrays;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.pack.PackManager;
import pl.indianbartonka.bds.pack.component.BehaviorPack;
import pl.indianbartonka.bds.pack.component.TexturePack;

public class PacksCommand extends Command {

    private final PackManager packManager;

    public PacksCommand(final BDSAutoEnable bdsAutoEnable) {
        super("packs", "Pokazuje dostępne paczki");
        this.packManager = bdsAutoEnable.getPackManager();
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.commandConfig.isPacksForAll() && !isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            this.deniedSound();
            return true;
        }

        this.sendMessage("&a---&bTekstury&a---");
        int counter = 1;
        for (final TexturePack texturePack : this.packManager.getResourcePackLoader().getLoadedTexturePacks()) {
            this.sendMessage(counter + ".&b " + texturePack.getName() + "&1 " + Arrays.toString(texturePack.getVersion()));
            counter++;
        }
        counter = 1;

        this.sendMessage(" ");
        this.sendMessage("&a---&bBehaviory&a---");
        for (final BehaviorPack behaviorPack : this.packManager.getBehaviorPackLoader().getLoadedBehaviorPacks()) {
            this.sendMessage(counter + ".&b " + behaviorPack.getName() + "&1 " + Arrays.toString(behaviorPack.getVersion()));
            counter++;
        }
        this.sendMessage(" ");

        return false;
    }
}