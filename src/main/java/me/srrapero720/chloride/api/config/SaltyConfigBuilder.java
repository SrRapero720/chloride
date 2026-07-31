package me.srrapero720.chloride.api.config;

import me.srrapero720.chloride.impl.SettingsScreens;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.EnumOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.PageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Sodium's config builder extended with the option types Chloride's settings screens can draw. The builder
 * Sodium hands to a {@code ConfigEntryPoint} always implements this interface, so it is safe to cast:
 *
 * <pre>{@code
 * public void registerConfigLate(ConfigBuilder builder) {
 *     SaltyConfigBuilder salty = SaltyConfigBuilder.of(builder);
 *     group.addOption(salty.createIdentifierListOption(id).setName(..).setTooltip(..).setBinding(..));
 * }
 * }</pre>
 *
 * These options are only registered while one of Chloride's screens is active, because Sodium's own screen
 * cannot render them. Guard their registration with {@code SaltyConfigBuilder.available()}.
 */
public interface SaltyConfigBuilder extends ConfigBuilder {
    /** Casts Sodium's builder to this interface. */
    static SaltyConfigBuilder of(final ConfigBuilder builder) {
        return (SaltyConfigBuilder) builder;
    }

    /** True while the active settings screen is one of Chloride's, the only ones that draw these options. */
    static boolean available() {
        return SettingsScreens.extended();
    }

    /**
     * Creates a text option, drawn as an inline edit box.
     *
     * @param id The unique identifier for this option.
     */
    StringOptionBuilder createStringOption(ResourceLocation id);

    /**
     * Creates a list-of-identifiers option, drawn as a collapsible list with an entry per identifier plus an
     * input row to append new ones.
     *
     * @param id The unique identifier for this option.
     */
    IdentifierListOptionBuilder createIdentifierListOption(ResourceLocation id);

    /**
     * Creates a string option limited to a set of values, drawn as a dropdown.
     *
     * @param id The unique identifier for this option.
     */
    StringListOptionBuilder createStringListOption(ResourceLocation id);

    /**
     * Creates an enum option drawn as a dropdown instead of Sodium's cycling bars. The returned builder is
     * Sodium's own, so the option keeps working on every screen; only its looks change here.
     *
     * @param id        The unique identifier for this option.
     * @param enumClass The enum class for this option.
     */
    <E extends Enum<E>> EnumOptionBuilder<E> createDropdownEnumOption(ResourceLocation id, Class<E> enumClass);

    /**
     * Creates an option that runs something instead of holding a value, drawn as a button.
     *
     * @param id The unique identifier for this option.
     */
    ActionOptionBuilder createActionOption(ResourceLocation id);

    /**
     * Hides an option registered by another mod. The group that held it is dropped too when nothing else is
     * left in it, so replacing a mod's entry point into its own screen leaves no empty space behind.
     *
     * @param id The id of the option to hide.
     */
    void hideOption(ResourceLocation id);

    /**
     * Replaces a page of another mod with one of your own, which is how a screen another mod opens externally
     * can be brought into the settings instead.
     *
     * @param configId    The config id of the mod owning the page.
     * @param name        The name of the page to replace.
     * @param replacement The page shown in its place.
     */
    void replacePage(String configId, Component name, PageBuilder replacement);
}
