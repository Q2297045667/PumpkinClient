package org.pumpkinclient.pumpkinclient.neoforge;

import net.neoforged.fml.loading.FMLPaths;
import org.pumpkinclient.pumpkinclient.Pumpkinclient;
import net.neoforged.fml.common.Mod;

@Mod(Pumpkinclient.MOD_ID)
public final class PumpkinclientNeoForge {
    public PumpkinclientNeoForge() {
        Pumpkinclient.init();
        Pumpkinclient.setConfigPath(FMLPaths.CONFIGDIR.get());
    }
}
