package me.srrapero720.chloride.mixins.impl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.impl.Overlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(Minecraft.class)
public abstract class OverlayMixin {
    @Shadow public static int fps;
    @Shadow @Final public Options options;
    @Shadow private MetricsRecorder metricsRecorder;
    @Shadow public ClientLevel level;
    @Shadow private double gpuUtilization;

    @Shadow public abstract DebugScreenOverlay getDebugOverlay();

    @Unique private double chloride$gpuUsage = 0;

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/metrics/profiling/MetricsRecorder;isRecording()Z"))
    private boolean redirect$renderDebug(final MetricsRecorder instance) {
        return this.level == null ? instance.isRecording() : ChlorideConfig.overlay.fps.systemDetails.gpu() || instance.isRecording();
    }

    @Redirect(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;gpuUtilization:D", opcode = Opcodes.PUTFIELD))
    private void redirect$assign(final Minecraft instance, final double value) {
        this.chloride$gpuUsage = value;
    }

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;gpuUtilization:D", opcode = Opcodes.GETFIELD, ordinal = 0, shift = At.Shift.BEFORE))
    private void inject$getGPU(final boolean pRenderLevel, final CallbackInfo ci) {
        this.gpuUtilization = this.chloride$gpuUsage;
        Overlay.pushAvgFps(fps);
    }

    // MICRO OPTIMIZATION
    @WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
    private String redirect$removeString(final Locale l, final String format, final Object[] args, final Operation<String> original) {
        if (this.getDebugOverlay().showDebugScreen() && !this.metricsRecorder.isRecording()) return original.call(l, format, args);
        else return "";
    }
}
