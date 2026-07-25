package me.srrapero720.chloride.mixins.impl;

import me.srrapero720.chloride.ChlorideConfig;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class FastSneakMixin {
    @Shadow private Entity entity;
    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;

    // OVERWRITES THE SMOOTHING LERP RESULT SO THE CAMERA SNAPS WHEN THE SNEAK STATE CHANGES
    @Inject(method = "tick", at = @At("TAIL"))
    private void chloride$fastSneak(final CallbackInfo ci) {
        if (ChlorideConfig.player.fastSneak && this.entity != null) {
            this.eyeHeight = this.eyeHeightOld = this.entity.getEyeHeight();
        }
    }
}
