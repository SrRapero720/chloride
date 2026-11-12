package me.srrapero720.chloride.mixins.impl;

import it.unimi.dsi.fastutil.ints.IntList;
import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.api.IParticleTypeData;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ParticlesMixins {

    // SMALL OPTIMIZATION
    @Mixin(ParticleType.class)
    public static class ParticleTypeMixin implements IParticleTypeData {
        @Unique private ResourceLocation chloride$id;

        @Override
        public ResourceLocation getId() {
            if (this.chloride$id == null) {
                this.chloride$id = BuiltInRegistries.PARTICLE_TYPE.getKey((ParticleType<?>) (Object) this);
            }
            return this.chloride$id;
        }
    }

    @Mixin(LevelRenderer.class)
    public static class LevelRendererMixin {
        @Inject(method = "tickRain", at = @At(value = "HEAD"), cancellable = true)
        public void inject$tick(Camera cam, CallbackInfo callbackInfo) {
            if (!ChlorideConfig.particles.rainDrops) {
                callbackInfo.cancel();
            }
        }

        @Inject(method = "renderSnowAndRain", at = @At(value = "HEAD"), cancellable = true)
        private void inject$render(LightTexture lightTex, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
            if (!ChlorideConfig.particles.rain) {
                ci.cancel();
            }
        }
    }

    @Mixin(ParticleEngine.class)
    public static class EngineMixin {
        @Inject(method = "destroy", at = @At(value = "HEAD"), cancellable = true)
        public void inject$destroy(BlockPos pPos, BlockState pSide, CallbackInfo ci) {
            if (!ChlorideConfig.particles.blockDestroyed) {
                ci.cancel();
            }
        }

        @Inject(method = "crack", at = @At(value = "HEAD"), cancellable = true)
        public void inject$crack(BlockPos pos, Direction direction, CallbackInfo ci) {
            if (!ChlorideConfig.particles.blockCracking) {
                ci.cancel();
            }
        }

        @Inject(method = "createParticle", at = @At(value = "HEAD"), cancellable = true)
        public void inject$create(ParticleOptions parameters, double x, double y, double z, double speeX, double speeY, double speeZ, CallbackInfoReturnable<Particle> cir) {
            final var id = ((IParticleTypeData) parameters.getType()).getId();
            if (ChlorideConfig.particles.disabled.contains(id)) {
                cir.setReturnValue(null);
            }
        }

        @Redirect(method = "makeParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"))
        public ResourceLocation redirect$particleRegistry(Registry instance, Object t) {
            return ((IParticleTypeData) t).getId();
        }
    }

    @Mixin(FireworkParticles.Starter.class)
    public static final class FireworkStarterMixin {
        @Unique private ResourceLocation chloride$id;

        @Inject(method = "createParticle", at = @At(value = "HEAD"), cancellable = true)
        public void inject$create(double x, double y, double z, double velocityX, double velocityY, double velocityZ, IntList colors, IntList fadeColors, boolean trail, boolean flicker, CallbackInfo ci) {
            if (ChlorideConfig.particles.disabled.contains(this.getId())) {
                ci.cancel();
            }
        }

        @Unique
        private ResourceLocation getId() {
            if (this.chloride$id == null) {
                this.chloride$id = BuiltInRegistries.PARTICLE_TYPE.getKey(ParticleTypes.FIREWORK.getType());
            }
            return this.chloride$id;
        }
    }
}
