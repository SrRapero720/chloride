package me.srrapero720.chloride.impl.sodium.pages;

import me.srrapero720.chloride.Chloride;
import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.impl.Zoom;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static me.srrapero720.chloride.impl.sodium.SodiumFeatures.*;

public class ZoomPage {
    private ZoomPage() {}

    public static OptionPageBuilder build(final ConfigBuilder b) {
        final OptionPageBuilder page = b.createOptionPage().setName(Component.translatable("chloride.zoom"));

        final MutableComponent zoomTooltip = Component.translatable("chloride.zoom.desc")
                .append("[")
                .append(Component.keybind("chloride.zoom").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal("]").append("\n"));
        if (!Zoom.canUseZoom()) zoomTooltip.append(Component.translatable("chloride.zoom.forbidden"));

        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createBooleanOption(Chloride.id("enableZoom"))
                        .setName(Component.translatable("chloride.zoom.title"))
                        .setTooltip(zoomTooltip)
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.LOW)
                        .setEnabled(Zoom.canUseZoom())
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.zoom.enabled = v, () -> ChlorideConfig.zoom.enabled))
                .addOption(b.createIntegerOption(Chloride.id("maxZoom"))
                        .setName(Component.translatable("chloride.zoom.max.title"))
                        .setTooltip(Component.translatable("chloride.zoom.max.desc"))
                        .setValueFormatter(suffix("°"))
                        .setRange(10, 100, 1)
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(50)
                        .setBinding(v -> ChlorideConfig.zoom.max = (double) v,
                                () -> Math.toIntExact(Math.round(ChlorideConfig.zoom.max))))
                .addOption(b.createBooleanOption(Chloride.id("smoothZoom"))
                        .setName(Component.translatable("chloride.zoom.smooth.title"))
                        .setTooltip(Component.translatable("chloride.zoom.smooth.desc"))
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.LOW)
                        .setEnabled(Zoom.canUseZoom())
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.zoom.smooth = v, () -> ChlorideConfig.zoom.smooth))
        );

        return page;
    }
}
