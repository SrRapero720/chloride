package me.srrapero720.chloride.mixins.impl;

import me.srrapero720.chloride.ChlorideConfig;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonHeadRenderer.class)
public class PistonPushMixin {
    // SKIPS THE MOVING-BLOCK RENDER SO PISTONS VISUALLY SNAP TO THEIR FINAL STATE
    @Inject(method = "render(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("HEAD"), cancellable = true)
    private void chloride$pistonPush(final CallbackInfo ci) {
        if (!ChlorideConfig.world.pistonPush) ci.cancel();
    }
}
