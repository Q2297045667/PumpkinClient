package org.pumpkinclient.pumpkinclient.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import org.pumpkinclient.pumpkinclient.Pumpkinclient;

public final class PumpkinclientFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Pumpkinclient.LOGGER.info("PumpkinClient client initialized");
    }
}
