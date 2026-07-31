package me.srrapero720.chloride.mixins.impl.sodium;

import me.srrapero720.chloride.impl.sodium.OptionInsertions;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOptionBuilderImpl;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOptions;
import net.caffeinemc.mods.sodium.api.config.structure.OptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Swaps Chloride's option builders for the Sodium placeholder they carry, since the group can only build
 * Sodium's own option types, and performs the insertions queued after an option of another mod.
 */
@Mixin(targets = "net.caffeinemc.mods.sodium.client.config.builder.OptionGroupBuilderImpl")
public class OptionGroupBuilderMixin {
    @ModifyVariable(method = "addOption", at = @At("HEAD"), argsOnly = true)
    private OptionBuilder chloride$unwrapSalty(final OptionBuilder option) {
        return option instanceof SaltyOptionBuilderImpl<?> salty ? salty.delegate() : option;
    }

    @Inject(method = "addOption", at = @At("HEAD"), cancellable = true)
    private void chloride$skipHidden(final OptionBuilder option, final CallbackInfoReturnable<OptionGroupBuilder> callback) {
        // CHLORIDE'S OWN BUILDERS CARRY NO ID OF SODIUM'S UNTIL THEY ARE UNWRAPPED, AND NEVER HIDE THEMSELVES
        if (!(option instanceof OptionBuilderAccessor sodium)) return;
        if (SaltyOptions.hidden(sodium.chloride$id())) callback.setReturnValue((OptionGroupBuilder) this);
    }

    @Inject(method = "addOption", at = @At("RETURN"))
    private void chloride$insertAfter(final OptionBuilder option, final CallbackInfoReturnable<OptionGroupBuilder> callback) {
        final OptionBuilder queued = OptionInsertions.take(((OptionBuilderAccessor) option).chloride$id());
        // THE INSERTED OPTION HAS AN ID OF ITS OWN, SO IT CANNOT QUEUE ANOTHER INSERTION BEHIND ITSELF
        if (queued != null) callback.getReturnValue().addOption(queued);
    }
}
