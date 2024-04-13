package me.indian.bds.command.defaults;

import java.util.Arrays;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.pack.PackManager;
import me.indian.bds.pack.component.BehaviorPack;
import me.indian.bds.pack.component.TexturePack;

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
            return true;
        }

        this.sendMessage("&a---&bTekstury&a---");
        int counter = 1;
        for (final TexturePack texturePack : this.packManager.getResourcePackLoader().getLoadedTexturePacks()) {
            this.sendMessage(counter + ".&b " + texturePack.name() + "&1 " + Arrays.toString(texturePack.version()));
            counter++;
        }
        counter = 1;

        this.sendMessage(" ");
        this.sendMessage("&a---&bBehaviory&a---");
        for (final BehaviorPack behaviorPack : this.packManager.getBehaviorPackLoader().getLoadedBehaviorPacks()) {
            this.sendMessage(counter + ".&b " + behaviorPack.name() + "&1 " + Arrays.toString(behaviorPack.version()));
            counter++;
        }
        this.sendMessage(" ");

        return false;
    }
}