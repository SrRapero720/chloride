package me.srrapero720.chloride.api.config;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Builder of an option that takes one string out of a set. It is drawn either as a dropdown, with the active
 * value on the row and the choices floating over the body, or as one single-choice flag row per value.
 */
public interface StringListOptionBuilder extends SaltyOptionBuilder<StringListOptionBuilder> {
    /** How the values are laid out. */
    enum Style {
        /** The active value on the row, the rest in a floating list. */
        DROPDOWN,
        /** One flag row per value, where ticking one unticks the others. */
        RADIO
    }

    /** Sets how the values are drawn. Defaults to {@link Style#DROPDOWN}. */
    StringListOptionBuilder setStyle(Style style);

    /** Sets the values this option can take. The supplier is polled every time the rows are rebuilt. */
    StringListOptionBuilder setEntries(Supplier<List<String>> entries);

    /** Sets the value this option falls back to when reset. Required. */
    StringListOptionBuilder setDefaultValue(String value);

    /** Sets how the value is written to and read from the mod's config. Required. */
    StringListOptionBuilder setBinding(Consumer<String> save, Supplier<String> load);
}
