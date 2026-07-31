package me.srrapero720.chloride.impl.sodium.salty;

import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.structure.PageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Everything Chloride keeps aside from Sodium's config: the values of its own option types, the enum options
 * drawn as dropdowns, the options hidden from other mods and the pages replaced on them.
 */
public final class SaltyOptions {
    private static final Map<ResourceLocation, SaltyEntry> ENTRIES = new HashMap<>();
    private static final Set<ResourceLocation> DROPDOWNS = new HashSet<>();
    private static final Set<ResourceLocation> HIDDEN = new HashSet<>();
    private static final Map<PageKey, PageBuilder> PAGES = new HashMap<>();

    private SaltyOptions() {}

    static void register(final SaltyEntry entry) {
        ENTRIES.put(entry.id, entry);
    }

    /** Marks an enum option as drawn with the dropdown style instead of Sodium's cycling bars. */
    public static void markDropdown(final ResourceLocation id) {
        DROPDOWNS.add(id);
    }

    public static boolean dropdown(final ResourceLocation id) {
        return DROPDOWNS.contains(id);
    }

    /** Hides an option of another mod; the group it belongs to is dropped as well when it ends up empty. */
    public static void hide(final ResourceLocation id) {
        HIDDEN.add(id);
    }

    public static boolean hidden(final ResourceLocation id) {
        return HIDDEN.contains(id);
    }

    /** Swaps the page named {@code name} of the mod {@code configId} for one of Chloride's own. */
    public static void replacePage(final String configId, final Component name, final PageBuilder replacement) {
        PAGES.put(new PageKey(configId, name), replacement);
    }

    @Nullable
    public static PageBuilder replacement(final String configId, final Component name) {
        return PAGES.get(new PageKey(configId, name));
    }

    /** Drops every registration; the config structure is rebuilt whenever the active screen changes. */
    public static void clear() {
        ENTRIES.clear();
        DROPDOWNS.clear();
        HIDDEN.clear();
        PAGES.clear();
    }

    @Nullable
    public static SaltyEntry get(final ResourceLocation id) {
        return ENTRIES.get(id);
    }

    public static boolean anyChanged() {
        for (final SaltyEntry entry: ENTRIES.values()) {
            if (entry.changed()) return true;
        }
        return false;
    }

    public static void applyAll() {
        final Set<StorageEventHandler> handlers = new HashSet<>();
        for (final SaltyEntry entry: ENTRIES.values()) {
            final StorageEventHandler handler = entry.apply();
            if (handler != null) handlers.add(handler);
        }
        // ONE FLUSH PER HANDLER, HOWEVER MANY OPTIONS SHARE IT
        for (final StorageEventHandler handler: handlers) handler.afterSave();
    }

    public static void undoAll() {
        for (final SaltyEntry entry: ENTRIES.values()) entry.undo();
    }

    private record PageKey(String configId, Component name) {}
}
