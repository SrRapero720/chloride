package me.srrapero720.chloride.impl.sodium.pages;

import me.srrapero720.chloride.Chloride;
import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.impl.Borderless;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static me.srrapero720.chloride.impl.sodium.SodiumFeatures.*;

public class InterfacePage {
    private static final ResourceLocation HIDE_JREMI = Chloride.id("hideJREMI");
    private static final ResourceLocation TOASTS = Chloride.id("toasts");

    private InterfacePage() {}

    public static OptionPageBuilder build(final ConfigBuilder b) {
        final boolean jremi = Chloride.installed("jei") || Chloride.installed("roughlyenoughitems") || Chloride.installed("emi");

        final OptionPageBuilder page = b.createOptionPage().setName(Component.translatable("chloride.interface"));

        // Screens
        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createBooleanOption(Chloride.id("fontShadows"))
                        .setName(Component.translatable("chloride.interface.font.shadow.title"))
                        .setTooltip(Component.translatable("chloride.interface.font.shadow.desc"))
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.VARIES)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.ui.fontShadows = v, () -> ChlorideConfig.ui.fontShadows))
                .addOption(b.createBooleanOption(HIDE_JREMI)
                        .setName(Component.translatable("chloride.interface.jei.title"))
                        .setTooltip(Component.translatable("chloride.interface.jei.desc"))
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.MEDIUM)
                        .setEnabled(jremi)
                        .setDefaultValue(false)
                        .setBinding(v -> ChlorideConfig.ui.hideJREMI = v, () -> ChlorideConfig.ui.hideJREMI))
                .addOption(b.createBooleanOption(Chloride.id("hideJREMIHint"))
                        .setName(Component.translatable("chloride.interface.jei.hint.title"))
                        .setTooltip(Component.translatable("chloride.interface.jei.hint.desc"))
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.LOW)
                        .setEnabledProvider(state -> jremi && state.readBooleanOption(HIDE_JREMI), HIDE_JREMI)
                        .setDefaultValue(false)
                        .setBinding(v -> ChlorideConfig.ui.hideJREMIHint = v, () -> ChlorideConfig.ui.hideJREMIHint))
        );


        // Misc
        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createEnumOption(Chloride.id("borderlessAttachModeF11"), Borderless.AttachMode.class)
                        .setName(Component.translatable("chloride.interface.borderless.f11.title"))
                        .setTooltip(Component.translatable("chloride.interface.borderless.f11.desc"))
                        .setElementNameProvider(enumNames("chloride.interface.borderless.f11"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(Borderless.AttachMode.ATTACH)
                        .setBinding(v -> ChlorideConfig.fullscreen.attachModeF11 = v, () -> ChlorideConfig.fullscreen.attachModeF11))
                .addOption(b.createBooleanOption(Chloride.id("fastLanguageReload"))
                        .setName(Component.translatable("chloride.interface.language.fast_reload.title"))
                        .setTooltip(Component.translatable("chloride.interface.language.fast_reload.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.ui.fastLanguageReload = v, () -> ChlorideConfig.ui.fastLanguageReload))
        );

        return page;
    }
}
