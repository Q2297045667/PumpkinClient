package org.pumpkinclient.pumpkinclient.mixin;

import net.minecraft.network.Connection;
import org.pumpkinclient.pumpkinclient.Pumpkinclient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Inject(method = "setupCompression", at = @At("HEAD"))
    private void onSetupCompression(int threshold, boolean validateDecompressed, CallbackInfo ci) {
        Pumpkinclient.resetEffectiveAlgorithm();
    }
}
