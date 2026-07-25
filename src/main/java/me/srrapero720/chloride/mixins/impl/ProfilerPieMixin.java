package me.srrapero720.chloride.mixins.impl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.srrapero720.chloride.ChlorideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class ProfilerPieMixin {
    // KEEPS THE SHIFT+F3 CHART ACTIVE EVERY FRAME WITHOUT TOUCHING THE F3 OVERLAY STATE
    @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showProfilerChart()Z"))
    private boolean chloride$profilerPie(final DebugScreenOverlay instance, final Operation<Boolean> original) {
        return original.call(instance) || ChlorideConfig.ui.profilerPie;
    }
}
