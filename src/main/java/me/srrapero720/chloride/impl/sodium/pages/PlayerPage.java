package me.srrapero720.chloride.impl.sodium.pages;

import me.srrapero720.chloride.Chloride;
import me.srrapero720.chloride.ChlorideConfig;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static me.srrapero720.chloride.impl.sodium.SodiumFeatures.*;

public class PlayerPage {
    private PlayerPage() {}

    public static OptionPageBuilder build(final ConfigBuilder b) {
        final var options = Minecraft.getInstance().options;
        final OptionPageBuilder page = b.createOptionPage().setName(Component.translatable("chloride.player"));

        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createBooleanOption(Chloride.id("fastSneak"))
                        .setName(Component.translatable("chloride.player.fast_sneak.title"))
                        .setTooltip(Component.translatable("chloride.player.fast_sneak.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(false)
                        .setBinding(v -> ChlorideConfig.player.fastSneak = v, () -> ChlorideConfig.player.fastSneak))
                .addOption(b.createIntegerOption(Chloride.id("fovEffectScale"))
                        .setName(Component.translatable("options.fovEffectScale"))
                        .setTooltip(Component.translatable("options.fovEffectScale.tooltip"))
                        .setValueFormatter(PERCENT)
                        .setRange(0, 100, 1)
                        .setStorageHandler(VANILLA_STORAGE)
                        .setImpact(OptionImpact.LOW)
                        .setDefaultValue(100)
                        .setBinding(v -> options.fovEffectScale().set(v / 100d),
                                () -> (int) Math.round(options.fovEffectScale().get() * 100)))
                .addOption(b.createBooleanOption(Chloride.id("clearSpectator"))
                        .setName(Component.translatable("chloride.player.clear_spectator.title"))
                        .setTooltip(Component.translatable("chloride.player.clear_spectator.desc"))
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.LOW)
                        .setDefaultValue(false)
                        .setBinding(v -> ChlorideConfig.player.clearSpectator = v, () -> ChlorideConfig.player.clearSpectator))
        );

        return page;
    }
}
