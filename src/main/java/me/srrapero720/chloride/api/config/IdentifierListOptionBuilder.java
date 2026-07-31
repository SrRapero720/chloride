package me.srrapero720.chloride.api.config;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Builder of a list-of-identifiers option, drawn as a collapsible list with add and remove controls. */
public interface IdentifierListOptionBuilder extends SaltyOptionBuilder<IdentifierListOptionBuilder> {
    /** Sets the entries this option falls back to when reset. Required. */
    IdentifierListOptionBuilder setDefaultValue(List<ResourceLocation> entries);

    /** Sets how the entries are written to and read from the mod's config. Required. */
    IdentifierListOptionBuilder setBinding(Consumer<List<ResourceLocation>> save, Supplier<List<ResourceLocation>> load);
}
