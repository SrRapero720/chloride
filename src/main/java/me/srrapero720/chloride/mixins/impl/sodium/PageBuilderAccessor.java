package me.srrapero720.chloride.mixins.impl.sodium;

import net.caffeinemc.mods.sodium.client.config.builder.PageBuilderImpl;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the name of a page being built, which is how a queued page replacement finds its target. */
@Mixin(PageBuilderImpl.class)
public interface PageBuilderAccessor {
    @Accessor("name")
    Component chloride$name();
}
