package me.srrapero720.chloride.impl.sodium.pages;

import com.mojang.blaze3d.shaders.FogShape;
import me.srrapero720.chloride.Chloride;
import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.impl.ChunkFade;
import me.srrapero720.chloride.impl.LeavesCulling;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.EnumOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.network.chat.Component;

import static me.srrapero720.chloride.impl.sodium.SodiumFeatures.*;

public class WorldPage {
    private WorldPage() {}

    public static OptionPageBuilder build(final ConfigBuilder b) {
        final OptionPageBuilder page = b.createOptionPage().setName(Component.translatable("chloride.world"));

        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createBooleanOption(Chloride.id("blueBand"))
                        .setName(Component.translatable("chloride.world.blueband.title"))
                        .setTooltip(Component.translatable("chloride.world.blueband.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.world.fog.blueBand = v, () -> ChlorideConfig.world.fog.blueBand))
        );

        if (!ChlorideConfig.modpackMode) {
            page.addOptionGroup(b.createOptionGroup()
                    .addOption(b.createBooleanOption(Chloride.id("fog"))
                            .setName(Component.translatable("chloride.world.fog.title"))
                            .setTooltip(Component.translatable("chloride.world.fog.desc"))
                            .setStorageHandler(STORAGE)
                            .setImpact(OptionImpact.LOW)
                            .setDefaultValue(true)
                            .setBinding(v -> ChlorideConfig.world.fog.enabled = v, () -> ChlorideConfig.world.fog.enabled))
                    .addOption(b.createBooleanOption(Chloride.id("fogOnOverworld"))
                            .setName(Component.translatable("chloride.world.fog.overworld.title"))
                            .setTooltip(Component.translatable("chloride.world.fog.overworld.desc"))
                            .setStorageHandler(STORAGE)
                            .setImpact(OptionImpact.LOW)
                            .setDefaultValue(true)
                            .setBinding(v -> ChlorideConfig.world.fog.onOverworld = v, () -> ChlorideConfig.world.fog.onOverworld))
                    .addOption(b.createBooleanOption(Chloride.id("fogOnNether"))
                            .setName(Component.translatable("chloride.world.fog.nether.title"))
                            .setTooltip(Component.translatable("chloride.world.fog.nether.desc"))
                            .setStorageHandler(STORAGE)
                            .setImpact(OptionImpact.LOW)
                            .setDefaultValue(true)
                            .setBinding(v -> ChlorideConfig.world.fog.onNether = v, () -> ChlorideConfig.world.fog.onNether))
                    .addOption(b.createBooleanOption(Chloride.id("fogOnEnd"))
                            .setName(Component.translatable("chloride.world.fog.end.title"))
                            .setTooltip(Component.translatable("chloride.world.fog.end.desc"))
                            .setStorageHandler(STORAGE)
                            .setImpact(OptionImpact.LOW)
                            .setDefaultValue(true)
                            .setBinding(v -> ChlorideConfig.world.fog.onEnd = v, () -> ChlorideConfig.world.fog.onEnd))
                    .addOption(b.createBooleanOption(Chloride.id("customFog"))
                            .setName(Component.translatable("chloride.world.custom_fog.title"))
                            .setTooltip(Component.translatable("chloride.world.custom_fog.desc"))
                            .setStorageHandler(STORAGE)
                            .setImpact(OptionImpact.LOW)
                            .setDefaultValue(false)
                            .setBinding(v -> ChlorideConfig.world.fog.custom = v, () -> ChlorideConfig.world.fog.custom))
                    .addOption(b.createIntegerOption(Chloride.id("fogStart"))
                            .setName(Component.translatable("chloride.world.custom_fog.start.title"))
                            .setTooltip(Component.translatable("chloride.world.custom_fog.start.desc"))
                            .setValueFormatter(NUMBER)
                            .setRange(-1000, 1000, 10)
                            .setStorageHandler(STORAGE)
                            .setDefaultValue(0)
                            .setBinding(v -> ChlorideConfig.world.fog.start = v, () -> ChlorideConfig.world.fog.start))
                    .addOption(b.createIntegerOption(Chloride.id("fogEnd"))
                            .setName(Component.translatable("chloride.world.custom_fog.end.title"))
                            .setTooltip(Component.translatable("chloride.world.custom_fog.end.desc"))
                            .setValueFormatter(NUMBER)
                            .setRange(100, 10000, 50)
                            .setStorageHandler(STORAGE)
                            .setDefaultValue(192)
                            .setBinding(v -> ChlorideConfig.world.fog.end = v, () -> ChlorideConfig.world.fog.end))
                    .addOption(b.createEnumOption(Chloride.id("fogShape"), FogShape.class)
                            .setName(Component.translatable("chloride.world.custom_fog.shape.title"))
                            .setTooltip(Component.translatable("chloride.world.custom_fog.shape.desc"))
                            .setElementNameProvider(enumNames("chloride.world.custom_fog.shape"))
                            .setStorageHandler(STORAGE)
                            .setDefaultValue(FogShape.CYLINDER)
                            .setBinding(v -> ChlorideConfig.world.fog.shape = v, () -> ChlorideConfig.world.fog.shape))
            );
        }

        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createEnumOption(Chloride.id("leavesCulling"), LeavesCulling.LeavesCullingMode.class)
                        .setName(Component.translatable("chloride.world.leaves_culling.title"))
                        .setTooltip(Component.translatable("chloride.world.leaves_culling.desc"))
                        .setElementNameProvider(enumNames("chloride.world.leaves_culling"))
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.HIGH)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .setDefaultValue(LeavesCulling.LeavesCullingMode.OFF)
                        .setBinding(v -> ChlorideConfig.world.leavesCulling = v, () -> ChlorideConfig.world.leavesCulling))
                .addOption(b.createIntegerOption(Chloride.id("cloudsHeight"))
                        .setName(Component.translatable("chloride.world.clouds.height.title"))
                        .setTooltip(Component.translatable("chloride.world.clouds.height.desc"))
                        .setValueFormatter(BLOCKS)
                        .setRange(64, 364, 4)
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(192)
                        .setBinding(v -> ChlorideConfig.world.cloudsHeight = v, () -> ChlorideConfig.world.cloudsHeight))
                .addOption(b.createIntegerOption(Chloride.id("lowerVoidHorizon"))
                        .setName(Component.translatable("chloride.world.void_horizon.title"))
                        .setTooltip(Component.translatable("chloride.world.void_horizon.desc"))
                        .setValueFormatter(VOID_HORIZON)
                        .setRange(-64, 256, 1)
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(63)
                        .setBinding(v -> ChlorideConfig.world.lowerVoidHorizon = v, () -> ChlorideConfig.world.lowerVoidHorizon))
                .addOption(b.createBooleanOption(Chloride.id("farSkybox"))
                        .setName(Component.translatable("chloride.world.far_skybox.title"))
                        .setTooltip(Component.translatable("chloride.world.far_skybox.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.world.farSkybox = v, () -> ChlorideConfig.world.farSkybox))
                .addOption(b.createBooleanOption(Chloride.id("pistonPush"))
                        .setName(Component.translatable("chloride.world.piston_push.title"))
                        .setTooltip(Component.translatable("chloride.world.piston_push.desc"))
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.LOW)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.world.pistonPush = v, () -> ChlorideConfig.world.pistonPush))
        );

        // World "amazings" (chunk fade) — kept disabled, mirrors the previous behaviour.
        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createEnumOption(Chloride.id("chunkFadeSpeed"), ChunkFade.Speed.class)
                        .setName(Component.translatable("chloride.world.fade.title"))
                        .setTooltip(Component.translatable("chloride.world.fade.desc"))
                        .setElementNameProvider(EnumOptionBuilder.nameProviderFrom(
                                Component.translatable("options.off"),
                                Component.translatable("options.graphics.fast"),
                                Component.translatable("options.graphics.fancy")))
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.LOW)
                        .setEnabled(false)
                        .setDefaultValue(ChunkFade.Speed.SLOW)
                        .setBinding(v -> ChlorideConfig.world.chunkFadeSpeed = v, () -> ChlorideConfig.world.chunkFadeSpeed))
        );

        return page;
    }
}
