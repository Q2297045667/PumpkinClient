package org.pumpkinclient.pumpkinclient.fabric;

import org.pumpkinclient.pumpkinclient.Pumpkinclient;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class PumpkinclientFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Pumpkinclient.init();
        Pumpkinclient.setConfigPath(FabricLoader.getInstance().getConfigDir());
    }
}
