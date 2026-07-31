package me.srrapero720.chloride.impl.sodium.salty;

import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Value of one of Chloride's own option types. Sodium only stores an inert placeholder carrying the name,
 * tooltip and enabled state, so the pending value, the applied value and the binding live here instead.
 */
public abstract class SaltyOption<V> extends SaltyEntry {
    private final StorageEventHandler storage;
    private final Consumer<V> save;
    private final V fallback;
    private final Supplier<V> load;
    private V applied;
    private V pending;

    private SaltyOption(final ResourceLocation id, @Nullable final Consumer<List<Path>> fileDrop, @Nullable final StorageEventHandler storage,
                        final Consumer<V> save, final Supplier<V> load, final V fallback) {
        super(id, fileDrop);
        this.storage = storage;
        this.save = save;
        this.load = load;
        this.fallback = fallback;
        this.undo();
    }

    /** The value being edited, which the row widgets read and write directly. */
    public V value() {
        return this.pending;
    }

    public void value(final V value) {
        this.pending = value;
    }

    /** The value loaded from the binding or last applied by this screen. */
    public V applied() {
        return this.copy(this.applied);
    }

    /** The value configured as this option's default. */
    public V defaultValue() {
        return this.copy(this.fallback);
    }

    /** True when the loaded value matches the configured default. */
    public boolean appliedDefault() {
        return Objects.equals(this.applied, this.fallback);
    }

    /** Drops only this option's pending edit without reloading its binding. */
    public void restore() {
        this.pending = this.copy(this.applied);
    }

    @Override
    public boolean changed() {
        return !Objects.equals(this.pending, this.applied);
    }

    /** Writes the pending value through the binding; the storage handler is flushed by {@link SaltyOptions}. */
    @Nullable
    @Override
    StorageEventHandler apply() {
        if (!this.changed()) return null;
        this.applied = this.copy(this.pending);
        this.save.accept(this.applied);
        return this.storage;
    }

    /** Drops the pending value and reads the config again. */
    @Override
    void undo() {
        this.applied = this.copy(this.load.get());
        this.pending = this.copy(this.applied);
    }

    @Override
    public void reset() {
        this.pending = this.copy(this.fallback);
    }

    // THE CONFIG INSTANCE IS NEVER SHARED WITH THE PENDING STATE, OR EDITING WOULD WRITE THROUGH IT
    protected abstract V copy(V value);

    /** Free text value. */
    public static final class Text extends SaltyOption<String> {
        public final int maxLength;

        Text(final ResourceLocation id, final Consumer<List<Path>> fileDrop, final StorageEventHandler storage, final Consumer<String> save,
             final Supplier<String> load, final String fallback, final int maxLength) {
            super(id, fileDrop, storage, save, load, fallback);
            this.maxLength = maxLength;
        }

        @Override
        protected String copy(final String value) {
            return value;
        }
    }

    /** One string out of a set, shown as a dropdown or as a column of single-choice flags. */
    public static final class Choices extends SaltyOption<String> {
        private final Supplier<List<String>> entries;
        private final boolean radio;

        Choices(final ResourceLocation id, final Consumer<List<Path>> fileDrop, final StorageEventHandler storage, final Consumer<String> save,
                final Supplier<String> load, final String fallback, final Supplier<List<String>> entries, final boolean radio) {
            super(id, fileDrop, storage, save, load, fallback);
            this.entries = entries;
            this.radio = radio;
        }

        /** The values this option can take; they are resolved every time the row is built. */
        public List<String> entries() {
            return this.entries.get();
        }

        /** True when the values are drawn as one flag row each instead of a dropdown. */
        public boolean radio() {
            return this.radio;
        }

        @Override
        protected String copy(final String value) {
            return value;
        }
    }

    /** List of identifiers, edited entry by entry. */
    public static final class Identifiers extends SaltyOption<List<ResourceLocation>> {
        Identifiers(final ResourceLocation id, final Consumer<List<Path>> fileDrop, final StorageEventHandler storage,
                    final Consumer<List<ResourceLocation>> save, final Supplier<List<ResourceLocation>> load,
                    final List<ResourceLocation> fallback) {
            super(id, fileDrop, storage, save, load, fallback);
        }

        @Override
        protected List<ResourceLocation> copy(final List<ResourceLocation> value) {
            return new ArrayList<>(value);
        }
    }
}
