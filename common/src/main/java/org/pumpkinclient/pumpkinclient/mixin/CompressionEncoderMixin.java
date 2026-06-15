package org.pumpkinclient.pumpkinclient.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.CompressionEncoder;
import net.minecraft.network.FriendlyByteBuf;
import org.pumpkinclient.pumpkinclient.config.PumpkinConfig;
import org.pumpkinclient.pumpkinclient.network.CompressionAlgorithm;
import org.pumpkinclient.pumpkinclient.network.NetworkCompression;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CompressionEncoder.class)
public abstract class CompressionEncoderMixin {

    @Shadow
    private int threshold;

    @Inject(method = "encode", at = @At("HEAD"), cancellable = true)
    private void onEncode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out, CallbackInfo ci) {
        PumpkinConfig config = PumpkinConfig.getInstance();
        if (config.getCompressionAlgorithm() != CompressionAlgorithm.ZSTD) {
            return;
        }
        int size = in.readableBytes();
        if (size < this.threshold) {
            new FriendlyByteBuf(out).writeVarInt(0);
            out.writeBytes(in);
        } else {
            byte[] data = new byte[size];
            in.readBytes(data);
            byte[] compressed = NetworkCompression.compress(data, CompressionAlgorithm.ZSTD, config.getCompressionLevel(), config.getMaxThreads());
            new FriendlyByteBuf(out).writeVarInt(size);
            out.writeBytes(compressed);
        }
        ci.cancel();
    }
}
