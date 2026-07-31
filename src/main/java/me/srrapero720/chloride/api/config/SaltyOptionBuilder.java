package me.srrapero720.chloride.api.config;

import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.structure.OptionBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Shared settings of Chloride's option types. Instances are added to a Sodium option group like any other
 * option builder; the group stores a placeholder option carrying the name, tooltip and enabled state, while
 * the value itself lives on Chloride's side.
 *
 * @param <B> The concrete builder type, so every setter keeps the chain going.
 */
public interface SaltyOptionBuilder<B extends SaltyOptionBuilder<B>> extends OptionBuilder {
    /** Sets the display name of this option. Required. */
    @Override
    B setName(Component name);

    /** Sets the tooltip shown when the option is hovered. Required. */
    B setTooltip(Component tooltip);

    /** Sets the handler run once after this option has been written to its binding. */
    B setStorageHandler(StorageEventHandler storage);

    /**
     * Sets what happens when files are dropped on the settings screen while this option is on show, which is
     * how a list of installable things takes new entries.
     */
    B setFileDrop(Consumer<List<Path>> handler);

    @Override
    B setEnabled(boolean available);

    @Override
    B setEnabledProvider(Function<ConfigState, Boolean> provider, ResourceLocation... dependencies);
}
