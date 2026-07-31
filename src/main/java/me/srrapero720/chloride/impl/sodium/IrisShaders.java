package me.srrapero720.chloride.impl.sodium;

import me.srrapero720.chloride.Chloride;
import me.srrapero720.chloride.api.config.SaltyConfigBuilder;
import me.srrapero720.chloride.mixins.impl.iris.ShaderPackScreenAccessor;
import me.srrapero720.chloride.api.config.StringListOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Brings Iris' shader pack selection into the settings screen. Iris registers it as an external page that
 * jumps into its own screen; here that page is swapped for one listing the packs as single-choice flags,
 * with the buttons of its screen on top and support for dropping packs onto the window.
 */
public final class IrisShaders {
    private static final String DOWNLOAD_URL = "https://www.curseforge.com/minecraft/search?class=shaders&page=1&pageSize=50&sortBy=popularity";
    private static final ResourceLocation SHADER_PACK_LIST = ResourceLocation.fromNamespaceAndPath("iris", "settings");
    private static final ResourceLocation SHADERS_ENABLED = Chloride.id("shadersEnabled");
    private static final Component PAGE_NAME = Component.translatable("options.iris.shaderPackSelection.title");

    /**
     * Sodium requires one on every stateful option. Iris already writes its properties file from the bindings
     * themselves, so this only makes sure the file is on disk once everything has been applied.
     */
    private static final StorageEventHandler STORAGE = () -> {
        try {
            Iris.getIrisConfig().save();
        } catch (final IOException e) {
            Chloride.LOGGER.warn("Failed to save the Iris config", e);
        }
    };

    private IrisShaders() {}

    /** Called while Iris registers its own options, so the swap is in place before its pages are built. */
    public static void integrate(final ConfigBuilder builder) {
        if (!SaltyConfigBuilder.available()) return;
        final SaltyConfigBuilder salty = SaltyConfigBuilder.of(builder);

        // THE BUTTON THAT JUMPS INTO IRIS' SCREEN HAS NOTHING LEFT TO OPEN ONCE THE PAGE LIVES HERE
        salty.hideOption(SHADER_PACK_LIST);
        salty.replacePage("iris", PAGE_NAME, builder.createOptionPage()
                .setName(PAGE_NAME)
                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(SHADERS_ENABLED)
                                .setName(Component.translatable("chloride.iris.enabled.title"))
                                .setTooltip(Component.translatable("chloride.iris.enabled.desc"))
                                .setStorageHandler(STORAGE)
                                .setDefaultValue(false)
                                .setBinding(value -> IrisApi.getInstance().getConfig().setShadersEnabledAndApply(value),
                                        () -> Iris.getIrisConfig().areShadersEnabled()))
                        .addOption(salty.createActionOption(Chloride.id("openShaderFolder"))
                                .setName(Component.translatable("options.iris.openShaderPackFolder"))
                                .setTooltip(Component.translatable("chloride.iris.folder.desc"))
                                .setLabel(Component.translatable("chloride.iris.folder.button"))
                                .setAction(() -> Util.getPlatform().openUri(Iris.getShaderpacksDirectoryManager().getDirectoryUri())))
                        .addOption(salty.createActionOption(Chloride.id("downloadShaders"))
                                .setName(Component.translatable("chloride.iris.download.title"))
                                .setTooltip(Component.translatable("chloride.iris.download.desc"))
                                .setLabel(Component.translatable("chloride.iris.download.button"))
                                .setAction(() -> Util.getPlatform().openUri(DOWNLOAD_URL))))
                .addOptionGroup(builder.createOptionGroup()
                        .setName(Component.translatable("chloride.iris.packs"))
                        // THE PACK OPTIONS ARE IRIS' OWN SCREEN, AND THERE IS NOTHING TO TUNE UNTIL A PACK IS LOADED
                        .addOption(builder.createExternalButtonOption(Chloride.id("shaderSettings"))
                                .setName(Component.translatable("options.iris.shaderPackSettings"))
                                .setTooltip(Component.translatable("chloride.iris.settings.desc"))
                                .setEnabledProvider(state -> Iris.getCurrentPack().isPresent(), SHADERS_ENABLED, ConfigState.UPDATE_ON_REBUILD)
                                .setScreenConsumer(IrisShaders::openPackSettings))
                        .addOption(salty.createStringListOption(Chloride.id("shaderPack"))
                                .setStyle(StringListOptionBuilder.Style.RADIO)
                                .setName(Component.translatable("chloride.iris.packs"))
                                .setTooltip(Component.translatable("chloride.iris.packs.desc"))
                                .setStorageHandler(STORAGE)
                                .setEntries(IrisShaders::packs)
                                .setFileDrop(IrisShaders::install)
                                .setDefaultValue("")
                                .setBinding(IrisShaders::select, () -> Iris.getIrisConfig().getShaderPackName().orElse("")))));
    }

    /**
     * Opens Iris' screen straight on the options of the active pack. Iris has no entry point for that view:
     * its own button flips a private flag and rebuilds, and the flag is only honoured once the navigation
     * controller exists, which this public refresh sets up before the screen is ever laid out.
     */
    private static void openPackSettings(final Screen parent) {
        final ShaderPackScreen screen = new ShaderPackScreen(parent);
        screen.refreshForChangedPack();
        ((ShaderPackScreenAccessor) screen).chloride$optionMenuOpen(true);
        Minecraft.getInstance().setScreen(screen);
    }

    private static List<String> packs() {
        try {
            return Iris.getShaderpacksDirectoryManager().enumerate();
        } catch (final IOException e) {
            Chloride.LOGGER.warn("Failed to list the shaderpacks directory", e);
            return List.of();
        }
    }

    // SAME SEQUENCE IRIS' OWN SCREEN APPLIES: NAME FIRST, THEN A RELOAD THROUGH THE ENABLE TOGGLE
    private static void select(final String pack) {
        if (pack.isEmpty() || pack.equals(Iris.getIrisConfig().getShaderPackName().orElse(""))) return;
        Iris.clearShaderPackOptionQueue();
        Iris.getIrisConfig().setShaderPackName(pack);
        IrisApi.getInstance().getConfig().setShadersEnabledAndApply(Iris.getIrisConfig().areShadersEnabled());
    }

    private static void install(final List<Path> paths) {
        for (final Path pack: paths) {
            if (!Iris.isValidShaderpack(pack)) continue;
            try {
                Iris.getShaderpacksDirectoryManager().copyPackIntoDirectory(pack.getFileName().toString(), pack);
            } catch (final IOException e) {
                Chloride.LOGGER.warn("Failed to install the dropped shaderpack {}", pack, e);
            }
        }
    }
}
