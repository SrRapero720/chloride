package me.srrapero720.chloride.mixins.impl.sodium;

import me.srrapero720.chloride.impl.sodium.salty.SaltyOptionBuilderImpl;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOptions;
import net.caffeinemc.mods.sodium.api.config.structure.OptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.PageBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Lets Chloride's option types replace an option of another mod, swapping the builder for the Sodium
 * placeholder it carries, the same way {@code OptionGroupBuilderMixin} does when adding one to a group.
 */
@Mixin(targets = "net.caffeinemc.mods.sodium.client.config.builder.ModOptionsBuilderImpl")
public class ModOptionsBuilderMixin {
    @Shadow @Final private String configId;

    @ModifyVariable(method = "registerOptionReplacement", at = @At("HEAD"), argsOnly = true)
    private OptionBuilder chloride$unwrapSalty(final OptionBuilder replacement) {
        return replacement instanceof SaltyOptionBuilderImpl<?> salty ? salty.delegate() : replacement;
    }

    // A PAGE ANOTHER MOD USES TO JUMP INTO ITS OWN SCREEN CAN BE TRADED FOR ONE THAT LIVES IN THE SETTINGS
    @ModifyVariable(method = "addPage", at = @At("HEAD"), argsOnly = true)
    private PageBuilder chloride$replacePage(final PageBuilder page) {
        final PageBuilder replacement = SaltyOptions.replacement(this.configId, ((PageBuilderAccessor) page).chloride$name());
        return replacement == null ? page : replacement;
    }
}
