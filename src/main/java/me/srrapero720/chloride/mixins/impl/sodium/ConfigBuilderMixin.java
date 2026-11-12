package me.srrapero720.chloride.mixins.impl.sodium;

import me.srrapero720.chloride.api.config.*;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOptionBuilderImpl;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOptions;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.EnumOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.PageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Makes the builder Sodium hands to every config entry point implement {@link SaltyConfigBuilder}, so mods can
 * cast it and declare Chloride's extended option types.
 */
@Mixin(targets = "net.caffeinemc.mods.sodium.client.config.builder.ConfigBuilderImpl")
public abstract class ConfigBuilderMixin implements SaltyConfigBuilder {
    @Override
    public StringOptionBuilder createStringOption(final ResourceLocation id) {
        return SaltyOptionBuilderImpl.text((ConfigBuilder) this, id);
    }

    @Override
    public IdentifierListOptionBuilder createIdentifierListOption(final ResourceLocation id) {
        return SaltyOptionBuilderImpl.identifiers((ConfigBuilder) this, id);
    }

    @Override
    public StringListOptionBuilder createStringListOption(final ResourceLocation id) {
        return SaltyOptionBuilderImpl.choices((ConfigBuilder) this, id);
    }

    @Override
    public <E extends Enum<E>> EnumOptionBuilder<E> createDropdownEnumOption(final ResourceLocation id, final Class<E> enumClass) {
        SaltyOptions.markDropdown(id);
        return this.createEnumOption(id, enumClass);
    }

    @Override
    public ActionOptionBuilder createActionOption(final ResourceLocation id) {
        return SaltyOptionBuilderImpl.action((ConfigBuilder) this, id);
    }

    @Override
    public void hideOption(final ResourceLocation id) {
        SaltyOptions.hide(id);
    }

    @Override
    public void replacePage(final String configId, final Component name, final PageBuilder replacement) {
        SaltyOptions.replacePage(configId, name, replacement);
    }
}
