package org.pumpkinclient.pumpkinclient.network;

public enum CompressionAlgorithm {
    ZLIB("ZLib"),
    ZSTD("Zstd");

    private final String id;

    CompressionAlgorithm(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static CompressionAlgorithm fromId(String id) {
        for (CompressionAlgorithm algo : values()) {
            if (algo.id.equalsIgnoreCase(id)) {
                return algo;
            }
        }
        return ZLIB;
    }
}
