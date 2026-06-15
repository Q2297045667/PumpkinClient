package org.pumpkinclient.pumpkinclient.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.CompressionDecoder;
import org.pumpkinclient.pumpkinclient.Pumpkinclient;
import org.pumpkinclient.pumpkinclient.network.CompressionAlgorithm;
import org.pumpkinclient.pumpkinclient.network.NetworkCompression;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompressionDecoder.class)
public abstract class CompressionDecoderMixin {

    @Unique
    private static final Logger LOGGER = Pumpkinclient.LOGGER;

    @Unique
    private byte[] pumpkinCompressedData;

    @Inject(method = "setupInflaterInput", at = @At("HEAD"))
    private void onSetupInflaterInput(ByteBuf buf, CallbackInfo ci) {
        if (isVanillaZlib()) {
            return;
        }
        pumpkinCompressedData = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), pumpkinCompressedData);
    }

    @Inject(method = "inflate", at = @At("HEAD"), cancellable = true)
    private void onInflate(ChannelHandlerContext ctx, int uncompressedSize, CallbackInfoReturnable<ByteBuf> cir) {
        if (isVanillaZlib()) {
            return;
        }
        if (pumpkinCompressedData == null) {
            return;
        }

        byte[] data = pumpkinCompressedData;
        pumpkinCompressedData = null;

        if (uncompressedSize == 0) {
            cir.setReturnValue(Unpooled.wrappedBuffer(data));
            return;
        }

        CompressionAlgorithm configured = Pumpkinclient.getConfig().getCompressionAlgorithm();
        CompressionAlgorithm effective = Pumpkinclient.getEffectiveAlgorithm();

        try {
            byte[] decompressed = NetworkCompression.decompress(data, uncompressedSize, effective);
            cir.setReturnValue(Unpooled.wrappedBuffer(decompressed));
        } catch (Exception firstError) {
            if (configured != CompressionAlgorithm.AUTO) {
                throw new RuntimeException(
                        "Decompression failed: server compression does not match configured "
                                + configured.getId() + ". Try switching to Auto mode.", firstError);
            }

            CompressionAlgorithm fallback = effective.fallback();
            LOGGER.warn("Auto-detect: {} failed, trying {}...", effective.getId(), fallback.getId());

            try {
                byte[] decompressed = NetworkCompression.decompress(data, uncompressedSize, fallback);
                cir.setReturnValue(Unpooled.wrappedBuffer(decompressed));
                LOGGER.info("Auto-detect: server uses {}. Switching.", fallback.getId());
                Pumpkinclient.setEffectiveAlgorithm(fallback);
            } catch (Exception secondError) {
                throw new RuntimeException(
                        "Decompression failed: tried " + effective.getId() + " and " + fallback.getId() + ".",
                        firstError);
            }
        }
    }

    @Unique
    private static boolean isVanillaZlib() {
        return Pumpkinclient.getConfig().getCompressionAlgorithm() == CompressionAlgorithm.ZLIB;
    }
}
