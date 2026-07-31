package me.srrapero720.chloride.api.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Builder of a text option, drawn as an inline edit box on the option row. */
public interface StringOptionBuilder extends SaltyOptionBuilder<StringOptionBuilder> {
    /** Sets the value this option falls back to when reset. Required. */
    StringOptionBuilder setDefaultValue(String value);

    /** Sets how the value is written to and read from the mod's config. Required. */
    StringOptionBuilder setBinding(Consumer<String> save, Supplier<String> load);

    /** Sets the maximum amount of characters the edit box accepts. Defaults to 64. */
    StringOptionBuilder setMaxLength(int length);
}
