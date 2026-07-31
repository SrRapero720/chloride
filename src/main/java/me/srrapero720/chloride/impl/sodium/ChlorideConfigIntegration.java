package me.srrapero720.chloride.impl.sodium;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.Window;
import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.api.config.SaltyConfigBuilder;
import me.srrapero720.chloride.impl.AdaptiveSync;
import me.srrapero720.chloride.impl.Borderless;
import me.srrapero720.chloride.impl.SettingsScreens;
import me.srrapero720.chloride.impl.sodium.pages.*;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.EnumOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.srrapero720.chloride.Chloride.LOGGER;
import static me.srrapero720.chloride.impl.sodium.SodiumFeatures.STORAGE;
import static me.srrapero720.chloride.Chloride.id;

@ConfigEntryPointForge("chloride")
public class ChlorideConfigIntegration implements ConfigEntryPoint {

    private static final ResourceLocation SODIUM_FULLSCREEN = ResourceLocation.parse("sodium:general.fullscreen");
    private static final ResourceLocation SODIUM_FULLSCREEN_RESOLUTION = ResourceLocation.parse("sodium:general.fullscreen_resolution");
    private static final ResourceLocation SODIUM_VSYNC = ResourceLocation.parse("sodium:general.vsync");
    private static final ResourceLocation FULL_SCREEN = id("fullScreen");
    private static final int NEON_GREEN = 0x39FF14;

    @Override
    public void registerConfigLate(final ConfigBuilder builder) {
        // setIcon (not setNonTintedIcon) tints the white logo to the group's theme colour set below.
        final ModOptionsBuilder mod = builder.registerOwnModOptions()
                .setIcon(SodiumFeatures.LOGO)
                .setColorTheme(builder.createColorTheme().setBaseThemeRGB(NEON_GREEN));

        // Replace Sodium's vanilla "fullscreen" toggle with Chloride's three-way borderless mode selector.
        mod.registerOptionReplacement(SODIUM_FULLSCREEN, builder.createEnumOption(FULL_SCREEN, Borderless.Mode.class)
                .setName(Component.translatable("options.fullscreen"))
                .setTooltip(Component.translatable("chloride.general.screen.desc"))
                .setElementNameProvider(EnumOptionBuilder.nameProviderFrom(
                        Component.translatable("chloride.general.screen.windowed"),
                        Component.translatable("chloride.general.screen.borderless"),
                        Component.translatable("chloride.general.screen.fullscreen")))
                .setStorageHandler(STORAGE)
                .setDefaultValue(Borderless.Mode.WINDOWED)
                .setBinding(Borderless::setFullScreenMode, () -> ChlorideConfig.fullscreen.mode));

        // The fullscreen-resolution option originally depended on sodium:general.fullscreen (a boolean). Re-point it
        // at our enum so it (a) keeps a valid dependency and (b) is only usable in true FULLSCREEN mode (borderless
        // uses the desktop resolution). On Chloride's screens the whole option is swapped for a dropdown listing the
        // video modes; everywhere else an overlay keeps Sodium's slider with only the dependency changed.
        if (SaltyConfigBuilder.available()) {
            final List<String> resolutions = resolutions();
            mod.registerOptionReplacement(SODIUM_FULLSCREEN_RESOLUTION, SaltyConfigBuilder.of(builder)
                    .createStringListOption(SODIUM_FULLSCREEN_RESOLUTION)
                    .setName(Component.translatable("options.fullscreen.resolution"))
                    .setTooltip(Component.translatable("sodium.options.fullscreen_resolution.tooltip"))
                    .setStorageHandler(SodiumFeatures.VANILLA_STORAGE)
                    .setEntries(ChlorideConfigIntegration::resolutions)
                    .setDefaultValue(resolutions.isEmpty() ? "" : resolutions.getFirst())
                    .setBinding(ChlorideConfigIntegration::applyResolution, ChlorideConfigIntegration::currentResolution)
                    .setEnabledProvider(ChlorideConfigIntegration::fullscreenResolutionUsable, FULL_SCREEN));
        } else {
            mod.registerOptionOverlay(SODIUM_FULLSCREEN_RESOLUTION, builder.createIntegerOption(SODIUM_FULLSCREEN_RESOLUTION)
                    .setEnabledProvider(ChlorideConfigIntegration::fullscreenResolutionUsable, FULL_SCREEN));
        }

        // Replace Sodium's V-Sync toggle with a three-way selector adding adaptive sync (EXT_swap_control_tear).
        // On unsupported drivers the ADAPTIVE value gracefully behaves as regular V-Sync (see AdaptiveSyncMixin).
        mod.registerOptionReplacement(SODIUM_VSYNC, builder.createEnumOption(id("vSync"), AdaptiveSync.Mode.class)
                .setName(Component.translatable("options.vsync"))
                .setTooltip(Component.translatable("chloride.general.vsync.desc"))
                .setElementNameProvider(EnumOptionBuilder.nameProviderFrom(
                        Component.translatable("options.off"),
                        Component.translatable("options.on"),
                        Component.translatable("chloride.general.vsync.adaptive")))
                .setStorageHandler(() -> {
                    ChlorideConfig.write();
                    Minecraft.getInstance().options.save();
                })
                .setDefaultValue(AdaptiveSync.Mode.ON)
                .setBinding(AdaptiveSync::mode, AdaptiveSync::mode));

        mod.addPage(OverlayPage.build(builder));
        mod.addPage(InterfacePage.build(builder));
        mod.addPage(WorldPage.build(builder));
        if (!ChlorideConfig.modpackMode) mod.addPage(DarknessPage.build(builder));
        mod.addPage(ParticlesPage.build(builder));
        // SODIUM'S SCREEN CANNOT AFFORD THE PER-PARTICLE TOGGLES INLINE (ISSUE #174), SO IT GETS THEM AS AN
        // EXTERNAL SCREEN INSTEAD; CHLORIDE'S SCREENS LIST THEM INSIDE THE PARTICLE PAGE.
        if (!SettingsScreens.extended()) {
            mod.addPage(builder.createExternalPage()
                    .setName(Component.translatable("chloride.particles.list"))
                    .setScreenConsumer(current -> Minecraft.getInstance().setScreen(new ParticleListScreen(current))));
        }
        mod.addPage(EntitiesPage.build(builder));
        mod.addPage(PlayerPage.build(builder));
        if (!ChlorideConfig.modpackMode) mod.addPage(ZoomPage.build(builder));

        if (ChlorideConfig.modpackMode) {
            LOGGER.info("Modpack Mode is enabled, skipping chloride True Darkness and Zoom page registration");
        }
    }

    // ONLY TRUE FULLSCREEN PICKS A VIDEO MODE, AND ONLY WHERE THE PLATFORM LETS THE GAME CHANGE IT
    private static boolean fullscreenResolutionUsable(final ConfigState state) {
        final Monitor monitor = Minecraft.getInstance().getWindow().findBestMonitor();
        if (monitor == null || monitor.getModeCount() <= 0) return false;
        final Util.OS os = Util.getPlatform();
        if (os != Util.OS.WINDOWS && os != Util.OS.OSX) return false;
        return state.readEnumOption(FULL_SCREEN, Borderless.Mode.class) == Borderless.Mode.FULLSCREEN;
    }

    // THE DROPDOWN ENTRIES ARE SODIUM'S OWN LABELS: THE CURRENT MODE FIRST, THEN EVERY MODE OF THE MONITOR
    private static List<String> resolutions() {
        final Monitor monitor = Minecraft.getInstance().getWindow().findBestMonitor();
        if (monitor == null) return List.of();
        final ControlValueFormatter formatter = ControlValueFormatterImpls.resolution();
        final List<String> entries = new ArrayList<>(monitor.getModeCount() + 1);
        for (int mode = 0; mode <= monitor.getModeCount(); mode++) entries.add(formatter.format(mode).getString());
        return entries;
    }

    private static void applyResolution(final String value) {
        final Window window = Minecraft.getInstance().getWindow();
        final Monitor monitor = window.findBestMonitor();
        if (monitor == null) return;
        final int index = resolutions().indexOf(value);
        window.setPreferredFullscreenVideoMode(index <= 0 ? Optional.empty() : Optional.of(monitor.getMode(index - 1)));
        // SODIUM RELIES ON ITS REQUIRES_VIDEOMODE_RELOAD FLAG FOR THIS, WHICH ONLY ITS OWN OPTIONS CARRY
        window.changeFullscreenVideoMode();
    }

    private static String currentResolution() {
        final List<String> entries = resolutions();
        if (entries.isEmpty()) return "";
        final Window window = Minecraft.getInstance().getWindow();
        final Monitor monitor = window.findBestMonitor();
        final int index = window.getPreferredFullscreenVideoMode().map(mode -> monitor.getVideoModeIndex(mode) + 1).orElse(0);
        return entries.get(Math.min(index, entries.size() - 1));
    }
}
