package me.srrapero720.chloride.mixins.impl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.FogShape;
import me.srrapero720.chloride.ChlorideConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.CubicSampler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = FogRenderer.class, priority = 910)
public abstract class FogAndBandMixin {
    @Unique private static final float FOG_END = 1_000_000.0F;

    @Inject(method = "setupFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void inject$fogToggle_fogDistance(final Camera camera, final FogRenderer.FogMode fogType, final float viewDistance, final boolean thickFog, final float tickDelta, final CallbackInfo ci, final FogType fogtype, final Entity entity, final FogRenderer.FogData fogrenderer$fogdata, final FogRenderer.MobEffectFogFunction fogrenderer$mobeffectfogfunction) {
        if (camera.getFluidInCamera() != FogType.NONE) return;
        if (fogrenderer$mobeffectfogfunction != null)  return;


        if (!ChlorideConfig.world.fog.enabled) { // FOG IS DISABLED
            fogrenderer$fogdata.start = FOG_END;
            fogrenderer$fogdata.end = FOG_END;
            fogrenderer$fogdata.shape = FogShape.SPHERE;
            return;
        } else if (ChlorideConfig.world.fog.custom) { // OVERRIDE FOG AT ALL
            fogrenderer$fogdata.start = ChlorideConfig.world.fog.start;
            fogrenderer$fogdata.end = ChlorideConfig.world.fog.end;
            fogrenderer$fogdata.shape = ChlorideConfig.world.fog.shape;
            return;
        }

        // TOGGLE PER LEVEL
        if (entity instanceof final Player player) {
            final Level level = player.level();
            if ((level.dimension() == Level.OVERWORLD && !ChlorideConfig.world.fog.onOverworld)
                    || (level.dimension() == Level.NETHER && !ChlorideConfig.world.fog.onNether)
                    || (level.dimension() == Level.END && !ChlorideConfig.world.fog.onEnd)) {
                fogrenderer$fogdata.start = FOG_END;
                fogrenderer$fogdata.end = FOG_END;
                fogrenderer$fogdata.shape = FogShape.SPHERE;
            }

        }
    }

    @WrapOperation(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 redirect$blueband_gaussianSampleColor(final Vec3 vec, final CubicSampler.Vec3Fetcher fetcher, final Operation<Vec3> original) {
        if (!ChlorideConfig.world.fog.blueBand) {
            final Minecraft mc = Minecraft.getInstance();

            if (mc.level.dimensionType().hasSkyLight())
                return mc.level.getSkyColor(mc.gameRenderer.getMainCamera().getPosition(), mc.getFrameTimeNs());
        }
        return original.call(vec, fetcher);
    }

    @WrapOperation(method = "setupColor", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;dot(Lorg/joml/Vector3fc;)F", remap = false))
    private static float redirect$blueband_dot(final Vector3f instance, final Vector3fc v, final Operation<Float> original) {
        if (!ChlorideConfig.world.fog.blueBand) return 0;
        return original.call(instance, v);
    }

    @WrapOperation(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"))
    private static float redirect$blueband_getRainLevel(final ClientLevel instance, final float v, final Operation<Float> original) {
        if (!ChlorideConfig.world.fog.blueBand) return 0;
        return original.call(instance, v);
    }

    @WrapOperation(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F"))
    private static float redirect$blueband_getThunderLevel(final ClientLevel instance, final float v, final Operation<Float> original) {
        if (!ChlorideConfig.world.fog.blueBand) return 0;
        return original.call(instance, v);
    }
}