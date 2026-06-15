package org.pumpkinclient.pumpkinclient.network;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum CompressionAlgorithm {
    ZLIB("ZLib"),
    ZSTD("Zstd"),
    AUTO("Auto");

    private static final Map<String, CompressionAlgorithm> BY_ID;
    static {
        Map<String, CompressionAlgorithm> map = new HashMap<>();
        for (CompressionAlgorithm algo : values()) {
            map.put(algo.id.toLowerCase(), algo);
        }
        BY_ID = Collections.unmodifiableMap(map);
    }

    private final String id;

    CompressionAlgorithm(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static CompressionAlgorithm fromId(String id) {
        CompressionAlgorithm result = BY_ID.get(id.toLowerCase());
        return result != null ? result : AUTO;
    }

    public CompressionAlgorithm fallback() {
        return switch (this) {
            case ZLIB -> ZSTD;
            case ZSTD -> ZLIB;
            case AUTO -> ZLIB;
        };
    }
}
