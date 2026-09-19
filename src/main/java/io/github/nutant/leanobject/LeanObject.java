package io.github.nutant.leanobject;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Config.MODID)
public final class LeanObject {

    public LeanObject() {
        if (FMLEnvironment.dist.isClient()) {
            Client.init();
        }
    }
}
