package org.pumpkinclient.pumpkinclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.annotations.Expose;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.pumpkinclient.pumpkinclient.network.CompressionAlgorithm;
import org.pumpkinclient.pumpkinclient.network.NetworkCompression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PumpkinConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("PumpkinClient");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(CompressionAlgorithm.class,
                    (JsonDeserializer<CompressionAlgorithm>) (json, type, context) ->
                            CompressionAlgorithm.fromId(json.getAsString()))
            .create();
    private static PumpkinConfig INSTANCE;

    @Expose
    private CompressionAlgorithm compressionAlgorithm = CompressionAlgorithm.AUTO;

    @Expose
    private int compressionLevel = 4;

    @Expose
    private int maxThreads = 0;

    private PumpkinConfig() {
    }

    public static PumpkinConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PumpkinConfig();
        }
        return INSTANCE;
    }

    public CompressionAlgorithm getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    public void setCompressionAlgorithm(CompressionAlgorithm compressionAlgorithm) {
        this.compressionAlgorithm = compressionAlgorithm;
        clampLevel();
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = switch (compressionAlgorithm) {
            case ZSTD, AUTO -> NetworkCompression.clampZstdLevel(compressionLevel);
            case ZLIB -> NetworkCompression.clampZlibLevel(compressionLevel);
        };
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = Math.clamp(maxThreads, 0, 32);
    }

    public void load(Path configPath) {
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                INSTANCE = GSON.fromJson(json, PumpkinConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new PumpkinConfig();
                }
                clampLevel();
                LOGGER.info("Loaded config: algorithm={}, level={}, maxThreads={}",
                        compressionAlgorithm.getId(), compressionLevel, maxThreads);
            } catch (Exception e) {
                LOGGER.error("Failed to load config, using defaults", e);
                INSTANCE = new PumpkinConfig();
            }
        } else {
            INSTANCE = new PumpkinConfig();
            save(configPath);
        }
    }

    public void save(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(this);
            Files.writeString(configPath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    private void clampLevel() {
        compressionLevel = switch (compressionAlgorithm) {
            case ZSTD, AUTO -> NetworkCompression.clampZstdLevel(compressionLevel);
            case ZLIB -> NetworkCompression.clampZlibLevel(compressionLevel);
        };
    }
}
