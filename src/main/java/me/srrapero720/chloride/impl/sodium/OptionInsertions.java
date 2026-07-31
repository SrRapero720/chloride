package me.srrapero720.chloride.impl.sodium;

import net.caffeinemc.mods.sodium.api.config.structure.OptionBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Options Chloride wants placed right after an option of another mod. Sodium's API can only append to a group
 * of its own, so the insertion is queued here and {@code OptionGroupBuilderMixin} performs it while the group
 * that owns the anchor is still being built.
 */
public final class OptionInsertions {
    private static final Map<ResourceLocation, OptionBuilder> PENDING = new HashMap<>();

    private OptionInsertions() {}

    /** Queues {@code option} to be added right after {@code anchor} lands in its group. */
    public static void after(final ResourceLocation anchor, final OptionBuilder option) {
        PENDING.put(anchor, option);
    }

    /** Takes the option queued after {@code anchor}, if any; insertions only happen once. */
    @Nullable
    public static OptionBuilder take(final ResourceLocation anchor) {
        return PENDING.remove(anchor);
    }
}
