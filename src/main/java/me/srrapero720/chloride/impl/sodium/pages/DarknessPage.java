package me.srrapero720.chloride.impl.sodium.pages;

import me.srrapero720.chloride.Chloride;
import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.api.config.SaltyConfigBuilder;
import me.srrapero720.chloride.impl.Darkness;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static me.srrapero720.chloride.impl.sodium.SodiumFeatures.*;

public class DarknessPage {
    private static final ResourceLocation BLOCK_LIGHT_ONLY = Chloride.id("darknessBlockLightOnly");

    private DarknessPage() {}

    public static OptionPageBuilder build(final ConfigBuilder b) {
        final OptionPageBuilder page = b.createOptionPage().setName(Component.translatable("chloride.darkness"));

        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createEnumOption(Chloride.id("darknessMode"), Darkness.DarkMode.class)
                        .setName(Component.translatable("chloride.darkness.level.title"))
                        .setTooltip(Component.translatable("chloride.darkness.level.desc"))
                        .setElementNameProvider(enumNames("chloride.darkness.level"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(Darkness.DarkMode.VANILLA)
                        .setBinding(v -> ChlorideConfig.darkness.mode = v, () -> ChlorideConfig.darkness.mode))
                .addOption(b.createBooleanOption(Chloride.id("darknessOnNoSkyLight"))
                        .setName(Component.translatable("chloride.darkness.noskylight.title"))
                        .setTooltip(Component.translatable("chloride.darkness.noskylight.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(false)
                        .setBinding(v -> ChlorideConfig.darkness.onNoSkyLight = v, () -> ChlorideConfig.darkness.onNoSkyLight))
        );

        final OptionGroupBuilder dimensions = b.createOptionGroup()
                .addOption(b.createBooleanOption(Chloride.id("darknessByDefault"))
                        .setName(Component.translatable("chloride.darkness.others.title"))
                        .setTooltip(Component.translatable("chloride.darkness.others.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(false)
                        .setBinding(v -> ChlorideConfig.darkness.byDefault = v, () -> ChlorideConfig.darkness.byDefault))
                .addOption(b.createBooleanOption(Chloride.id("darknessOnOverworld"))
                        .setName(Component.translatable("chloride.darkness.overworld.title"))
                        .setTooltip(Component.translatable("chloride.darkness.overworld.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.darkness.onOverworld = v, () -> ChlorideConfig.darkness.onOverworld));
        // THE WHITELIST ONLY EXISTS ON THE SCREENS THAT CAN DRAW A LIST CONTROL
        if (SaltyConfigBuilder.available()) {
            dimensions.addOption(SaltyConfigBuilder.of(b).createIdentifierListOption(Chloride.id("darknessDimensionWhitelist"))
                    .setName(Component.translatable("chloride.darkness.others.whitelist.title"))
                    .setTooltip(Component.translatable("chloride.darkness.others.whitelist.desc"))
                    .setStorageHandler(STORAGE)
                    .setDefaultValue(ChlorideConfig.toId())
                    .setBinding(v -> ChlorideConfig.darkness.dimensionWhitelist = v, () -> ChlorideConfig.darkness.dimensionWhitelist));
        }
        page.addOptionGroup(dimensions);

        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createBooleanOption(Chloride.id("darknessOnNether"))
                        .setName(Component.translatable("chloride.darkness.nether.title"))
                        .setTooltip(Component.translatable("chloride.darkness.nether.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(false)
                        .setBinding(v -> ChlorideConfig.darkness.onNether = v, () -> ChlorideConfig.darkness.onNether))
                .addOption(b.createIntegerOption(Chloride.id("darknessNetherFogBright"))
                        .setName(Component.translatable("chloride.darkness.nether.brightness.title"))
                        .setTooltip(Component.translatable("chloride.darkness.nether.brightness.desc"))
                        .setValueFormatter(PERCENT)
                        .setRange(0, 100, 1)
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(50)
                        .setBinding(v -> ChlorideConfig.darkness.netherFogBright = v / 100d,
                                () -> Math.toIntExact(Math.round(ChlorideConfig.darkness.netherFogBright * 100))))
        );

        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createBooleanOption(Chloride.id("darknessOnEnd"))
                        .setName(Component.translatable("chloride.darkness.end.title"))
                        .setTooltip(Component.translatable("chloride.darkness.end.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(false)
                        .setBinding(v -> ChlorideConfig.darkness.onEnd = v, () -> ChlorideConfig.darkness.onEnd))
                .addOption(b.createIntegerOption(Chloride.id("darknessEndFogBright"))
                        .setName(Component.translatable("chloride.darkness.end.brightness.title"))
                        .setTooltip(Component.translatable("chloride.darkness.end.brightness.desc"))
                        .setValueFormatter(PERCENT)
                        .setRange(0, 100, 1)
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(50)
                        .setBinding(v -> ChlorideConfig.darkness.endFogBright = v / 100d,
                                () -> Math.toIntExact(Math.round(ChlorideConfig.darkness.endFogBright * 100))))
        );

        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createBooleanOption(BLOCK_LIGHT_ONLY)
                        .setName(Component.translatable("chloride.darkness.blocklightonly.title"))
                        .setTooltip(Component.translatable("chloride.darkness.blocklightonly.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(false)
                        .setBinding(v -> ChlorideConfig.darkness.blockLightOnly = v, () -> ChlorideConfig.darkness.blockLightOnly))
                .addOption(b.createBooleanOption(Chloride.id("darknessAffectedByMoonPhase"))
                        .setName(Component.translatable("chloride.darkness.moonphase.title"))
                        .setTooltip(Component.translatable("chloride.darkness.moonphase.desc"))
                        .setStorageHandler(STORAGE)
                        .setEnabledProvider(state -> !state.readBooleanOption(BLOCK_LIGHT_ONLY), BLOCK_LIGHT_ONLY)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.darkness.affectedByMoonPhase = v, () -> ChlorideConfig.darkness.affectedByMoonPhase))
                .addOption(b.createIntegerOption(Chloride.id("darknessNewMoonBright"))
                        .setName(Component.translatable("chloride.darkness.moonphase.fresh.title"))
                        .setTooltip(Component.translatable("chloride.darkness.moonphase.fresh.desc"))
                        .setValueFormatter(PERCENT)
                        .setRange(0, 100, 1)
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(0)
                        .setBinding(v -> ChlorideConfig.darkness.newMoonBright = v / 100d,
                                () -> Math.toIntExact(Math.round(ChlorideConfig.darkness.newMoonBright * 100d))))
                .addOption(b.createIntegerOption(Chloride.id("darknessFullMoonBright"))
                        .setName(Component.translatable("chloride.darkness.moonphase.full.title"))
                        .setTooltip(Component.translatable("chloride.darkness.moonphase.full.desc"))
                        .setValueFormatter(PERCENT)
                        .setRange(0, 100, 1)
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(25)
                        .setBinding(v -> ChlorideConfig.darkness.fullMoonBright = v / 100d,
                                () -> Math.toIntExact(Math.round(ChlorideConfig.darkness.fullMoonBright * 100))))
        );

        return page;
    }
}
