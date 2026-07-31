package me.srrapero720.chloride.impl;

import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOptions;
import me.srrapero720.chloride.impl.sodium.screens.ChlorideSettingsScreen;
import me.srrapero720.chloride.impl.sodium.screens.PopuliSettingsScreen;
import me.srrapero720.chloride.impl.sodium.screens.ReeseSettingsScreen;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Settings screen styles for Sodium's video settings. Sodium's own screen and Chloride's are selectable
 * anywhere; alternative styles remain locked to dev environments while they are polished.
 */
public class SettingsScreens {
    /** True while the active style is one of Chloride's, the only ones that draw the extended option types. */
    public static boolean extended() {
        // A STYLE THAT CANNOT OPEN (A HAND-EDITED CONFIG ON PRODUCTION) FALLS BACK TO SODIUM'S SCREEN, AND SO DO ITS PAGES
        final Style style = ChlorideConfig.ui.settingsScreen;
        return style != Style.SODIUM;
    }

    /**
     * Switches from the current settings screen style to the next selectable one, persisting the choice and
     * rebuilding Sodium's config, since the pages a style gets are decided while they are being built.
     */
    public static void next(final Style current, final Screen prev) {
        final Style[] styles = Style.values();
        Style next = current;
        next = styles[(next.ordinal() + 1) % styles.length];

        ChlorideConfig.ui.settingsScreen = next;
        ChlorideConfig.write();
        SaltyOptions.clear();
        ConfigManager.registerConfigsLate();
        Minecraft.getInstance().setScreen(next.screen(prev));
    }

    public enum Style {
        SODIUM, CHLORIDE, TEXTRUE, POPULI;

        /** Creates this style's settings screen; {@code prev} is restored when it closes. */
        public Screen screen(final Screen prev) {
            return switch (this) {
                case SODIUM -> VideoSettingsScreen.createScreen(prev);
                case CHLORIDE -> new ChlorideSettingsScreen(prev);
                case TEXTRUE -> new ReeseSettingsScreen(prev);
                case POPULI -> new PopuliSettingsScreen(prev);
            };
        }

        public Component title() {
            return Component.translatable("chloride.settings.design." + this.name().toLowerCase(Locale.ROOT));
        }
    }
}
