package org.pumpkinclient.pumpkinclient;

import java.nio.file.Path;
import org.pumpkinclient.pumpkinclient.config.PumpkinConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Pumpkinclient {
    public static final String MOD_ID = "pumpkinclient";
    public static final Logger LOGGER = LoggerFactory.getLogger("PumpkinClient");

    private static Path configPath;

    public static void init() {
        LOGGER.info("PumpkinClient initializing...");
    }

    public static void setConfigPath(Path path) {
        configPath = path.resolve(MOD_ID + ".json");
        PumpkinConfig.getInstance().load(configPath);
    }

    public static void saveConfig() {
        if (configPath != null) {
            PumpkinConfig.getInstance().save(configPath);
        }
    }

    public static PumpkinConfig getConfig() {
        return PumpkinConfig.getInstance();
    }
}
