package me.srrapero720.chloride.mixins.impl.sodium;

import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Drops groups that ended up empty, which happens when every option they held was hidden. Sodium refuses to
 * build an empty group, so the group has to be skipped before it is asked to.
 */
@Mixin(targets = "net.caffeinemc.mods.sodium.client.config.builder.OptionPageBuilderImpl")
public class OptionPageBuilderMixin {
    @Inject(method = "addOptionGroup", at = @At("HEAD"), cancellable = true)
    private void chloride$skipEmptyGroup(final OptionGroupBuilder group, final CallbackInfoReturnable<OptionPageBuilder> callback) {
        if (((OptionGroupBuilderAccessor) group).chloride$options().isEmpty()) callback.setReturnValue((OptionPageBuilder) this);
    }
}
