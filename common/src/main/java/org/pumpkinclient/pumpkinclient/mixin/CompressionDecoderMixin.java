package org.pumpkinclient.pumpkinclient.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.CompressionDecoder;
import org.pumpkinclient.pumpkinclient.config.PumpkinConfig;
import org.pumpkinclient.pumpkinclient.network.CompressionAlgorithm;
import org.pumpkinclient.pumpkinclient.network.NetworkCompression;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompressionDecoder.class)
public abstract class CompressionDecoderMixin {

    @Unique
    private byte[] pumpkinCompressedData;

    @Inject(method = "setupInflaterInput", at = @At("HEAD"))
    private void onSetupInflaterInput(ByteBuf buf, CallbackInfo ci) {
        PumpkinConfig config = PumpkinConfig.getInstance();
        if (config.getCompressionAlgorithm() == CompressionAlgorithm.ZSTD) {
            pumpkinCompressedData = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), pumpkinCompressedData);
        }
    }

    @Inject(method = "inflate", at = @At("HEAD"), cancellable = true)
    private void onInflate(ChannelHandlerContext ctx, int uncompressedSize, CallbackInfoReturnable<ByteBuf> cir) {
        PumpkinConfig config = PumpkinConfig.getInstance();
        if (config.getCompressionAlgorithm() != CompressionAlgorithm.ZSTD) {
            return;
        }
        byte[] decompressed = NetworkCompression.decompress(pumpkinCompressedData, uncompressedSize, CompressionAlgorithm.ZSTD);
        cir.setReturnValue(Unpooled.wrappedBuffer(decompressed));
    }
}
