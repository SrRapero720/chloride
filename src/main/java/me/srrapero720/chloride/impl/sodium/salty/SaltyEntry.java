package me.srrapero720.chloride.impl.sodium.salty;

import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Anything Chloride keeps on its side for an option Sodium only stores as a placeholder. Entries without a
 * value (plain actions) inherit the no-op state handling, so the registry treats every kind the same way.
 */
public abstract class SaltyEntry {
    public final ResourceLocation id;
    private final Consumer<List<Path>> fileDrop;

    SaltyEntry(final ResourceLocation id, @Nullable final Consumer<List<Path>> fileDrop) {
        this.id = id;
        this.fileDrop = fileDrop;
    }

    /** Takes the files dropped on the screen while this entry is on show; false when it wants none. */
    public boolean drop(final List<Path> paths) {
        if (this.fileDrop == null) return false;
        this.fileDrop.accept(paths);
        return true;
    }

    public boolean changed() {
        return false;
    }

    public void reset() {}

    @Nullable
    StorageEventHandler apply() {
        return null;
    }

    void undo() {}
}
