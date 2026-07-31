package me.srrapero720.chloride.mixins.impl.iris;

import me.srrapero720.chloride.impl.sodium.IrisShaders;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.irisshaders.iris.compat.sodium.config.IrisConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the moment Iris declares its Sodium options, which is the only point where the swap of its shader
 * pack page can be queued no matter the order the config entry points run in.
 */
@Mixin(IrisConfig.class)
public class IrisConfigMixin {
    @Inject(method = "registerConfigLate", at = @At("HEAD"))
    private void chloride$integrateShaders(final ConfigBuilder builder, final CallbackInfo callback) {
        IrisShaders.integrate(builder);
    }
}
