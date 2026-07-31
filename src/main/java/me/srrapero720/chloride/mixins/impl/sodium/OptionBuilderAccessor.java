package me.srrapero720.chloride.mixins.impl.sodium;

import net.caffeinemc.mods.sodium.client.config.builder.OptionBuilderImpl;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the id of an option being built, which is how a queued insertion finds its anchor. */
@Mixin(OptionBuilderImpl.class)
public interface OptionBuilderAccessor {
    @Accessor("id")
    ResourceLocation chloride$id();
}
