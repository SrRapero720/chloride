package me.srrapero720.chloride.impl.sodium.pages;

import me.srrapero720.chloride.Chloride;
import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.impl.SettingsScreens;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static me.srrapero720.chloride.impl.sodium.SodiumFeatures.STORAGE;

public class ParticlesPage {
    private ParticlesPage() {}

    public static OptionPageBuilder build(final ConfigBuilder b) {
        final OptionPageBuilder page = b.createOptionPage().setName(Component.translatable("chloride.particles"));

        page.addOptionGroup(b.createOptionGroup()
                .addOption(b.createBooleanOption(Chloride.id("rainParticles"))
                        .setName(Component.translatable("chloride.particles.rain.title"))
                        .setTooltip(Component.translatable("chloride.particles.rain.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.particles.rain = v, () -> ChlorideConfig.particles.rain))
                .addOption(b.createBooleanOption(Chloride.id("rainDropParticles"))
                        .setName(Component.translatable("chloride.particles.rain.drop.title"))
                        .setTooltip(Component.translatable("chloride.particles.rain.drop.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.particles.rainDrops = v, () -> ChlorideConfig.particles.rainDrops))
                .addOption(b.createBooleanOption(Chloride.id("crackingBlockParticles"))
                        .setName(Component.translatable("chloride.particles.block.cracking.title"))
                        .setTooltip(Component.translatable("chloride.particles.block.cracking.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.particles.blockCracking = v, () -> ChlorideConfig.particles.blockCracking))
                .addOption(b.createBooleanOption(Chloride.id("destroyedBlockParticles"))
                        .setName(Component.translatable("chloride.particles.block.destroy.title"))
                        .setTooltip(Component.translatable("chloride.particles.block.destroy.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.particles.blockDestroyed = v, () -> ChlorideConfig.particles.blockDestroyed))
        );

        // ONE TOGGLE PER REGISTERED PARTICLE. SODIUM'S SCREEN DRAWS EVERY ROW AND CHOKES ON MODPACKS WITH
        // HUNDREDS OF THEM (ISSUE #174), SO IT KEEPS THE EXTERNAL LIST SCREEN AND ONLY CHLORIDE'S SCREENS,
        // WHICH CULL OFF-SCREEN ROWS, GET THEM INLINE.
        if (SettingsScreens.extended()) {
            final OptionGroupBuilder group = b.createOptionGroup().setName(Component.translatable("chloride.particles.list"));
            final List<ResourceLocation> disabled = ChlorideConfig.particles.disabled;
            for (final ResourceLocation id: BuiltInRegistries.PARTICLE_TYPE.keySet().stream().sorted().toList()) {
                group.addOption(b.createBooleanOption(id)
                        .setName(Component.literal(id.toString()))
                        .setTooltip(Component.translatable("chloride.particles.toggle.desc"))
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(true)
                        .setBinding(v -> {
                            if (v) disabled.remove(id);
                            else if (!disabled.contains(id)) disabled.add(id);
                        }, () -> !disabled.contains(id)));
            }
            page.addOptionGroup(group);
        }

        return page;
    }
}
