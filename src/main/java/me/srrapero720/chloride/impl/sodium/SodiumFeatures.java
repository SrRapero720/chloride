package me.srrapero720.chloride.impl.sodium;

import me.srrapero720.chloride.Chloride;
import me.srrapero720.chloride.ChlorideConfig;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class SodiumFeatures {
    public static final ResourceLocation LOGO = ResourceLocation.fromNamespaceAndPath(Chloride.ID, "textures/gui/logo.png");
    public static final StorageEventHandler STORAGE = ChlorideConfig::write;
    public static final StorageEventHandler VANILLA_STORAGE = () -> Minecraft.getInstance().options.save();

    public static <E extends Enum<E>> Function<E, Component> enumNames(final String translation) {
        return e -> Component.translatable(translation + "." + e.name().toLowerCase());
    }

    public static final ControlValueFormatter VOID_HORIZON = v -> v == 63.0f ? Component.translatable("chloride.world.void_horizon.vanilla") : Component.literal(String.valueOf(v));
    public static final ControlValueFormatter NUMBER = v -> Component.literal(String.valueOf(v));
    public static final ControlValueFormatter PERCENT = v -> Component.literal(v + "%");
    public static final ControlValueFormatter BLOCKS = v -> Component.literal(v + " blocks");

    public static ControlValueFormatter suffix(final String suffix) {
        return v -> Component.literal(v + suffix);
    }
}
