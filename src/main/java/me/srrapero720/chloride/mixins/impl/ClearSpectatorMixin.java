package me.srrapero720.chloride.mixins.impl;

import me.srrapero720.chloride.ChlorideConfig;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class ClearSpectatorMixin {
    // BLOCKS SPECTATOR/MOB POST-PROCESS SHADERS; MENU BLUR LOADS THROUGH loadBlurEffect AND IS NOT AFFECTED
    @Inject(method = "loadEffect", at = @At("HEAD"), cancellable = true)
    private void chloride$clearSpectator(final CallbackInfo ci) {
        if (ChlorideConfig.player.clearSpectator) ci.cancel();
    }
}
