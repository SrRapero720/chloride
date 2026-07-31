package me.srrapero720.chloride.mixins.impl.sodium;

import net.caffeinemc.mods.sodium.client.config.structure.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Exposes the options a group has taken so far, so empty groups can be spotted before they are built. */
@Mixin(targets = "net.caffeinemc.mods.sodium.client.config.builder.OptionGroupBuilderImpl")
public interface OptionGroupBuilderAccessor {
    @Accessor("options")
    List<Option> chloride$options();
}
