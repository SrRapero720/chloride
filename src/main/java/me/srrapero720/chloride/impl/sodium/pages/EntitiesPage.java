package me.srrapero720.chloride.impl.sodium.pages;

import me.srrapero720.chloride.Chloride;
import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.api.config.SaltyConfigBuilder;
import me.srrapero720.chloride.impl.EntityCulling;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static me.srrapero720.chloride.impl.sodium.SodiumFeatures.*;

public class EntitiesPage {
    private EntitiesPage() {}

    private static final ResourceLocation ENTITY_CULLING = Chloride.id("entityDistanceCulling");
    private static final ResourceLocation MONSTER_CULLING = Chloride.id("monsterDistanceCulling");
    private static final ResourceLocation TILE_CULLING = Chloride.id("tileEntityDistanceCulling");

    public static OptionPageBuilder build(final ConfigBuilder b) {
        final OptionPageBuilder page = b.createOptionPage().setName(Component.translatable("chloride.entities"));
        final Component vsWarning = Component.translatable("chloride.entities.culling.vseureka.warning").withStyle(ChatFormatting.GOLD);
        final ControlValueFormatter entityLimit = n -> n >= 512
                ? Component.translatable("options.framerateLimit.max")
                : Component.literal(String.valueOf(n));
        final SaltyConfigBuilder salty = SaltyConfigBuilder.of(b);

        if (!ChlorideConfig.modpackMode) {
            page.addOptionGroup(b.createOptionGroup()
                    .addOption(b.createBooleanOption(Chloride.id("entityNametagRendering"))
                            .setName(Component.translatable("chloride.entities.nametag.entities.title"))
                            .setTooltip(Component.translatable("chloride.entities.nametag.entities.desc"))
                            .setStorageHandler(STORAGE)
                            .setDefaultValue(true)
                            .setBinding(v -> ChlorideConfig.entities.nametags.entities = v, () -> ChlorideConfig.entities.nametags.entities))
                    .addOption(b.createBooleanOption(Chloride.id("playerNametagRendering"))
                            .setName(Component.translatable("chloride.entities.nametag.players.title"))
                            .setTooltip(Component.translatable("chloride.entities.nametag.players.desc"))
                            .setStorageHandler(STORAGE)
                            .setDefaultValue(true)
                            .setBinding(v -> ChlorideConfig.entities.nametags.players = v, () -> ChlorideConfig.entities.nametags.players))
                    .addOption(b.createBooleanOption(Chloride.id("itemNametagRendering"))
                            .setName(Component.translatable("chloride.entities.nametag.items.title"))
                            .setTooltip(Component.translatable("chloride.entities.nametag.items.desc"))
                            .setStorageHandler(STORAGE)
                            .setDefaultValue(true)
                            .setBinding(v -> ChlorideConfig.entities.nametags.items = v, () -> ChlorideConfig.entities.nametags.items))
            );
        }

        final OptionGroupBuilder entities = b.createOptionGroup()
                .addOption(b.createBooleanOption(ENTITY_CULLING)
                        .setName(Component.translatable("chloride.entities.culling.title"))
                        .setTooltip(Component.translatable("chloride.entities.culling.desc"))
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.HIGH)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.entities.culling.entities = v, () -> ChlorideConfig.entities.culling.entities))
                .addOption(b.createIntegerOption(Chloride.id("entityLimit"))
                        .setName(Component.translatable("chloride.entities.culling.limit.title"))
                        .setTooltip(Component.translatable("chloride.entities.culling.limit.desc"))
                        .setValueFormatter(entityLimit)
                        .setRange(0, 512, 1)
                        .setStorageHandler(STORAGE)
                        .setDefaultValue(512)
                        .setBinding(v -> ChlorideConfig.entities.culling.entityLimit = v, () -> ChlorideConfig.entities.culling.entityLimit))
                .addOption(b.createIntegerOption(Chloride.id("entityCullingDistanceX"))
                        .setName(Component.translatable("chloride.entities.culling.distance.horizontal.title"))
                        .setTooltip(Component.translatable("chloride.entities.culling.distance.horizontal.desc"))
                        .setValueFormatter(BLOCKS)
                        .setRange(0, 128, 8)
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.HIGH)
                        .setDefaultValue(64)
                        .setBinding(v -> ChlorideConfig.entities.culling.entityDistanceX = v * v,
                                () -> Math.toIntExact(Math.round(Math.sqrt(ChlorideConfig.entities.culling.entityDistanceX)))))
                .addOption(b.createIntegerOption(Chloride.id("entityCullingDistanceY"))
                        .setName(Component.translatable("chloride.entities.culling.distance.vertical.title"))
                        .setTooltip(Component.translatable("chloride.entities.culling.distance.vertical.desc")
                                .append(EntityCulling.VS_I ? Component.literal("\n\n").append(vsWarning) : Component.empty()))
                        .setValueFormatter(BLOCKS)
                        .setRange(0, 256, 4)
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.HIGH)
                        .setEnabled(!EntityCulling.VS_I)
                        .setDefaultValue(32)
                        .setBinding(v -> ChlorideConfig.entities.culling.entityDistanceY = v, () -> ChlorideConfig.entities.culling.entityDistanceY));
        // WHITELISTS ONLY EXIST ON THE SCREENS THAT CAN DRAW A LIST CONTROL
        if (SaltyConfigBuilder.available()) {
            entities.addOption(salty.createIdentifierListOption(Chloride.id("entityCullingWhitelist"))
                    .setName(Component.translatable("chloride.entities.culling.whitelist.title"))
                    .setTooltip(Component.translatable("chloride.entities.culling.whitelist.desc"))
                    .setStorageHandler(STORAGE)
                    .setEnabledProvider(state -> state.readBooleanOption(ENTITY_CULLING), ENTITY_CULLING)
                    .setDefaultValue(ChlorideConfig.toId("minecraft:ghast", "minecraft:ender_dragon", "iceandfire:all", "create:all"))
                    .setBinding(v -> ChlorideConfig.entities.culling.entityWhitelist = v, () -> ChlorideConfig.entities.culling.entityWhitelist));
        }
        page.addOptionGroup(entities);

        final OptionGroupBuilder monsters = b.createOptionGroup()
                .addOption(b.createBooleanOption(MONSTER_CULLING)
                        .setName(Component.translatable("chloride.entities.culling.monster.title"))
                        .setTooltip(Component.translatable("chloride.entities.culling.monster.desc"))
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.HIGH)
                        .setDefaultValue(false)
                        .setBinding(v -> ChlorideConfig.entities.culling.monsters = v, () -> ChlorideConfig.entities.culling.monsters))
                .addOption(b.createIntegerOption(Chloride.id("monsterCullingDistanceX"))
                        .setName(Component.translatable("chloride.entities.culling.monster.distance.horizontal.title"))
                        .setTooltip(Component.translatable("chloride.entities.culling.monster.distance.horizontal.desc"))
                        .setValueFormatter(BLOCKS)
                        .setRange(0, 128, 8)
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.HIGH)
                        .setDefaultValue(128)
                        .setBinding(v -> ChlorideConfig.entities.culling.monsterDistanceX = v * v,
                                () -> Math.toIntExact(Math.round(Math.sqrt(ChlorideConfig.entities.culling.monsterDistanceX)))))
                .addOption(b.createIntegerOption(Chloride.id("monsterCullingDistanceY"))
                        .setName(Component.translatable("chloride.entities.culling.monster.distance.vertical.title"))
                        .setTooltip(Component.translatable("chloride.entities.culling.monster.distance.vertical.desc")
                                .append(EntityCulling.VS_I ? Component.literal("\n\n").append(vsWarning) : Component.empty()))
                        .setValueFormatter(BLOCKS)
                        .setRange(0, 256, 4)
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.HIGH)
                        .setEnabled(!EntityCulling.VS_I)
                        .setDefaultValue(64)
                        .setBinding(v -> ChlorideConfig.entities.culling.monsterDistanceY = v, () -> ChlorideConfig.entities.culling.monsterDistanceY));
        if (SaltyConfigBuilder.available()) {
            monsters.addOption(salty.createIdentifierListOption(Chloride.id("monsterCullingWhitelist"))
                    .setName(Component.translatable("chloride.entities.culling.monster.whitelist.title"))
                    .setTooltip(Component.translatable("chloride.entities.culling.monster.whitelist.desc"))
                    .setStorageHandler(STORAGE)
                    .setEnabledProvider(state -> state.readBooleanOption(MONSTER_CULLING), MONSTER_CULLING)
                    .setDefaultValue(ChlorideConfig.toId())
                    .setBinding(v -> ChlorideConfig.entities.culling.monsterWhitelist = v, () -> ChlorideConfig.entities.culling.monsterWhitelist));
        }
        page.addOptionGroup(monsters);

        final OptionGroupBuilder tiles = b.createOptionGroup()
                .addOption(b.createBooleanOption(TILE_CULLING)
                        .setName(Component.translatable("chloride.entities.culling.tiles.title"))
                        .setTooltip(Component.translatable("chloride.entities.culling.tiles.desc"))
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.HIGH)
                        .setDefaultValue(true)
                        .setBinding(v -> ChlorideConfig.entities.culling.tileEntities = v, () -> ChlorideConfig.entities.culling.tileEntities))
                .addOption(b.createIntegerOption(Chloride.id("tileEntityCullingDistanceX"))
                        .setName(Component.translatable("chloride.entities.culling.tile.distance.horizontal.title"))
                        .setTooltip(Component.translatable("chloride.entities.culling.tile.distance.horizontal.desc"))
                        .setValueFormatter(BLOCKS)
                        .setRange(0, 128, 8)
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.HIGH)
                        .setDefaultValue(64)
                        .setBinding(v -> ChlorideConfig.entities.culling.tileEntityDistanceX = v * v,
                                () -> Math.toIntExact(Math.round(Math.sqrt(ChlorideConfig.entities.culling.tileEntityDistanceX)))))
                .addOption(b.createIntegerOption(Chloride.id("tileEntityCullingDistanceY"))
                        .setName(Component.translatable("chloride.entities.culling.tile.distance.vertical.title"))
                        .setTooltip(Component.translatable("chloride.entities.culling.tile.distance.vertical.desc")
                                .append(EntityCulling.VS_I ? Component.literal("\n\n").append(vsWarning) : Component.empty()))
                        .setValueFormatter(BLOCKS)
                        .setRange(0, 256, 4)
                        .setStorageHandler(STORAGE)
                        .setImpact(OptionImpact.HIGH)
                        .setEnabled(!EntityCulling.VS_I)
                        .setDefaultValue(32)
                        .setBinding(v -> ChlorideConfig.entities.culling.tileEntityDistanceY = v, () -> ChlorideConfig.entities.culling.tileEntityDistanceY));
        if (SaltyConfigBuilder.available()) {
            tiles.addOption(salty.createIdentifierListOption(Chloride.id("tileEntityCullingWhitelist"))
                    .setName(Component.translatable("chloride.entities.culling.tile.whitelist.title"))
                    .setTooltip(Component.translatable("chloride.entities.culling.tile.whitelist.desc"))
                    .setStorageHandler(STORAGE)
                    .setEnabledProvider(state -> state.readBooleanOption(TILE_CULLING), TILE_CULLING)
                    .setDefaultValue(ChlorideConfig.toId("waterframes:all"))
                    .setBinding(v -> ChlorideConfig.entities.culling.tileEntityWhitelist = v, () -> ChlorideConfig.entities.culling.tileEntityWhitelist));
        }
        page.addOptionGroup(tiles);

        return page;
    }
}
