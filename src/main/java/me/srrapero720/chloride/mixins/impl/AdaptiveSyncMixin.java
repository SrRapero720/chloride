package me.srrapero720.chloride.mixins.impl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.impl.AdaptiveSync;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Window.class)
public class AdaptiveSyncMixin {
    // SWAP INTERVAL -1 (EXT_swap_control_tear) TEARS INSTEAD OF STALLING WHEN A FRAME MISSES VBLANK
    @WrapOperation(method = "updateVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V", remap = false))
    private void chloride$adaptiveSync(final int interval, final Operation<Void> original) {
        GLFW.glfwSwapInterval(interval > 0 && ChlorideConfig.fullscreen.adaptiveSync && AdaptiveSync.supported() ? -1 : interval);
    }
}
