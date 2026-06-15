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
    private static volatile PumpkinConfig INSTANCE;

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
            synchronized (PumpkinConfig.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PumpkinConfig();
                }
            }
        }
        return INSTANCE;
    }

    public CompressionAlgorithm getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    public void setCompressionAlgorithm(CompressionAlgorithm algorithm) {
        this.compressionAlgorithm = algorithm;
        clampLevel();
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public void setCompressionLevel(int level) {
        this.compressionLevel = clamp(level);
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
                PumpkinConfig loaded = GSON.fromJson(json, PumpkinConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                } else {
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
            Files.writeString(configPath, GSON.toJson(this), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    private void clampLevel() {
        this.compressionLevel = clamp(compressionLevel);
    }

    private int clamp(int level) {
        return switch (compressionAlgorithm) {
            case ZSTD, AUTO -> NetworkCompression.clampZstdLevel(level);
            case ZLIB -> NetworkCompression.clampZlibLevel(level);
        };
    }
}
