package me.srrapero720.chloride.mixins.impl;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderer.class)
public class NameTagToggleMixin<T extends Entity> {

//    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
//    public void inject$render(T pEntity, Component pDisplayName, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
//
//
//    }
}
