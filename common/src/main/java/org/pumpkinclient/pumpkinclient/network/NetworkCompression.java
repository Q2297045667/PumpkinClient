package org.pumpkinclient.pumpkinclient.network;

import com.github.luben.zstd.Zstd;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class NetworkCompression {

    private NetworkCompression() {
    }

    public static byte[] compress(byte[] input, CompressionAlgorithm algorithm, int level, int maxThreads) {
        return switch (algorithm) {
            case ZLIB -> compressZlib(input, level);
            case ZSTD -> compressZstd(input, level);
            case AUTO -> throw new IllegalArgumentException("AUTO is not a valid compression algorithm");
        };
    }

    public static byte[] decompress(byte[] input, int uncompressedSize, CompressionAlgorithm algorithm) {
        return switch (algorithm) {
            case ZLIB -> decompressZlib(input, uncompressedSize);
            case ZSTD -> decompressZstd(input, uncompressedSize);
            case AUTO -> throw new IllegalArgumentException("AUTO is not a valid compression algorithm");
        };
    }

    private static byte[] compressZlib(byte[] input, int level) {
        Deflater deflater = new Deflater(level);
        try {
            deflater.setInput(input);
            deflater.finish();
            byte[] buffer = new byte[input.length + 64];
            int total = 0;
            while (!deflater.finished()) {
                int written = deflater.deflate(buffer, total, buffer.length - total);
                total += written;
                if (total >= buffer.length) {
                    byte[] expanded = new byte[buffer.length * 2];
                    System.arraycopy(buffer, 0, expanded, 0, total);
                    buffer = expanded;
                }
            }
            byte[] result = new byte[total];
            System.arraycopy(buffer, 0, result, 0, total);
            return result;
        } finally {
            deflater.end();
        }
    }

    private static byte[] compressZstd(byte[] input, int level) {
        return Zstd.compress(input, clampZstdLevel(level));
    }

    private static byte[] decompressZlib(byte[] input, int uncompressedSize) {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(input);
            byte[] result = new byte[uncompressedSize];
            inflater.inflate(result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("ZLIB decompression failed", e);
        } finally {
            inflater.end();
        }
    }

    private static byte[] decompressZstd(byte[] input, int uncompressedSize) {
        byte[] result = new byte[uncompressedSize];
        long decompressed = Zstd.decompress(result, input);
        if (decompressed < 0) {
            throw new RuntimeException("ZSTD decompression failed with error " + decompressed);
        }
        return result;
    }

    public static int clampZstdLevel(int level) {
        return Math.clamp(level, 1, 22);
    }

    public static int clampZlibLevel(int level) {
        return Math.clamp(level, 0, 9);
    }
}
