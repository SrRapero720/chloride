package me.srrapero720.chloride.mixins.impl.sodium;

import net.caffeinemc.mods.sodium.client.config.structure.Option;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the id Sodium keeps package-private; Chloride needs it to know the mod an option comes from. */
@Mixin(Option.class)
public interface OptionAccessor {
    @Accessor("id")
    ResourceLocation chloride$id();
}
