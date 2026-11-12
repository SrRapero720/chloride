package me.srrapero720.chloride.impl.sodium.salty;

import me.srrapero720.chloride.api.config.*;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ExternalButtonOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionBuilder;
import net.caffeinemc.mods.sodium.client.gui.options.control.ExternalButtonControl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Builds one of Chloride's option types. Adding it to a Sodium group registers the value on Chloride's side
 * and hands Sodium an inert external-button option as the placeholder carrying name, tooltip and enabled
 * state (see {@code OptionGroupBuilderMixin}, which performs the swap).
 *
 * @param <B> The public builder type this implementation returns from its setters.
 */
public abstract class SaltyOptionBuilderImpl<B extends SaltyOptionBuilder<B>> implements SaltyOptionBuilder<B> {
    private final ConfigBuilder builder;
    final ResourceLocation id;
    StorageEventHandler storage;
    Consumer<List<Path>> fileDrop;
    private Component name;
    private Component tooltip;
    private Boolean enabled;
    private Function<ConfigState, Boolean> enabledProvider;
    private ResourceLocation[] dependencies = new ResourceLocation[0];

    private SaltyOptionBuilderImpl(final ConfigBuilder builder, final ResourceLocation id) {
        this.builder = builder;
        this.id = id;
    }

    public static StringOptionBuilder text(final ConfigBuilder builder, final ResourceLocation id) {
        return new Text(builder, id);
    }

    public static IdentifierListOptionBuilder identifiers(final ConfigBuilder builder, final ResourceLocation id) {
        return new Identifiers(builder, id);
    }

    public static StringListOptionBuilder choices(final ConfigBuilder builder, final ResourceLocation id) {
        return new Choices(builder, id);
    }

    public static ActionOptionBuilder action(final ConfigBuilder builder, final ResourceLocation id) {
        return new Action(builder, id);
    }

    /** Registers the value and returns the placeholder Sodium stores in its option group. */
    public OptionBuilder delegate() {
        Validate.notNull(this.name, "Name must be set");
        Validate.notNull(this.tooltip, "Tooltip must be set");
        SaltyOptions.register(this.build());

        final ExternalButtonOptionBuilder placeholder = this.builder.createExternalButtonOption(this.id)
                .setName(this.name)
                .setTooltip(this.tooltip)
                .setScreenConsumer(screen -> {});
        if (this.enabledProvider != null) placeholder.setEnabledProvider(this.enabledProvider, this.dependencies);
        else if (this.enabled != null) placeholder.setEnabled(this.enabled);
        return placeholder;
    }

    abstract SaltyEntry build();

    @SuppressWarnings("unchecked")
    private B self() {
        return (B) this;
    }

    @Override
    public B setName(final Component name) {
        this.name = name;
        return this.self();
    }

    @Override
    public B setTooltip(final Component tooltip) {
        this.tooltip = tooltip;
        return this.self();
    }

    @Override
    public B setStorageHandler(final StorageEventHandler storage) {
        this.storage = storage;
        return this.self();
    }

    @Override
    public B setFileDrop(final Consumer<List<Path>> handler) {
        this.fileDrop = handler;
        return this.self();
    }

    @Override
    public B setEnabled(final boolean available) {
        this.enabled = available;
        return this.self();
    }

    @Override
    public B setEnabledProvider(final Function<ConfigState, Boolean> provider, final ResourceLocation... dependencies) {
        this.enabledProvider = provider;
        this.dependencies = dependencies;
        return this.self();
    }

    private static final class Text extends SaltyOptionBuilderImpl<StringOptionBuilder> implements StringOptionBuilder {
        private static final int DEFAULT_MAX_LENGTH = 64;

        private Consumer<String> save;
        private Supplier<String> load;
        private String fallback;
        private int maxLength = DEFAULT_MAX_LENGTH;

        private Text(final ConfigBuilder builder, final ResourceLocation id) {
            super(builder, id);
        }

        @Override
        SaltyOption<?> build() {
            Validate.notNull(this.save, "Binding must be set");
            Validate.notNull(this.fallback, "Default value must be set");
            return new SaltyOption.Text(this.id, this.fileDrop, this.storage, this.save, this.load, this.fallback, this.maxLength);
        }

        @Override
        public StringOptionBuilder setDefaultValue(final String value) {
            this.fallback = value;
            return this;
        }

        @Override
        public StringOptionBuilder setBinding(final Consumer<String> save, final Supplier<String> load) {
            this.save = save;
            this.load = load;
            return this;
        }

        @Override
        public StringOptionBuilder setMaxLength(final int length) {
            this.maxLength = length;
            return this;
        }
    }

    private static final class Choices extends SaltyOptionBuilderImpl<StringListOptionBuilder> implements StringListOptionBuilder {
        private Supplier<List<String>> entries = List::of;
        private Style style = Style.DROPDOWN;
        private Consumer<String> save;
        private Supplier<String> load;
        private String fallback;

        private Choices(final ConfigBuilder builder, final ResourceLocation id) {
            super(builder, id);
        }

        @Override
        SaltyOption<?> build() {
            Validate.notNull(this.save, "Binding must be set");
            Validate.notNull(this.fallback, "Default value must be set");
            return new SaltyOption.Choices(this.id, this.fileDrop, this.storage, this.save, this.load, this.fallback, this.entries, this.style == Style.RADIO);
        }

        @Override
        public StringListOptionBuilder setStyle(final Style style) {
            this.style = style;
            return this;
        }

        @Override
        public StringListOptionBuilder setEntries(final Supplier<List<String>> entries) {
            this.entries = entries;
            return this;
        }

        @Override
        public StringListOptionBuilder setDefaultValue(final String value) {
            this.fallback = value;
            return this;
        }

        @Override
        public StringListOptionBuilder setBinding(final Consumer<String> save, final Supplier<String> load) {
            this.save = save;
            this.load = load;
            return this;
        }
    }

    private static final class Action extends SaltyOptionBuilderImpl<ActionOptionBuilder> implements ActionOptionBuilder {
        private Component label = ExternalButtonControl.BASE_BUTTON_TEXT;
        private Runnable action;

        private Action(final ConfigBuilder builder, final ResourceLocation id) {
            super(builder, id);
        }

        @Override
        SaltyEntry build() {
            Validate.notNull(this.action, "Action must be set");
            return new SaltyAction(this.id, this.fileDrop, this.label, this.action);
        }

        @Override
        public ActionOptionBuilder setLabel(final Component label) {
            this.label = label;
            return this;
        }

        @Override
        public ActionOptionBuilder setAction(final Runnable action) {
            this.action = action;
            return this;
        }
    }

    private static final class Identifiers extends SaltyOptionBuilderImpl<IdentifierListOptionBuilder> implements IdentifierListOptionBuilder {
        private Consumer<List<ResourceLocation>> save;
        private Supplier<List<ResourceLocation>> load;
        private List<ResourceLocation> fallback;

        private Identifiers(final ConfigBuilder builder, final ResourceLocation id) {
            super(builder, id);
        }

        @Override
        SaltyOption<?> build() {
            Validate.notNull(this.save, "Binding must be set");
            Validate.notNull(this.fallback, "Default value must be set");
            return new SaltyOption.Identifiers(this.id, this.fileDrop, this.storage, this.save, this.load, this.fallback);
        }

        @Override
        public IdentifierListOptionBuilder setDefaultValue(final List<ResourceLocation> entries) {
            this.fallback = entries;
            return this;
        }

        @Override
        public IdentifierListOptionBuilder setBinding(final Consumer<List<ResourceLocation>> save, final Supplier<List<ResourceLocation>> load) {
            this.save = save;
            this.load = load;
            return this;
        }
    }
}
