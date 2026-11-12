package me.srrapero720.chloride.impl;

import me.srrapero720.chloride.Chloride;
import me.srrapero720.chloride.api.events.FastModelSettingsUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;
import java.util.Optional;

@EventBusSubscriber(value = Dist.CLIENT, modid = Chloride.ID)
public class FastBlocks {
    public static Pack SOLID_BEDS_PACK;
    public static Pack SOLID_CHESTS_PACK;

    public static boolean canUseOnChests() {
        return !Chloride.installed("enhancedblockentities");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChestSettingUpdateEvent(FastModelSettingsUpdate.ChestEvent e) {
        if (e.isEnabled()) {
            Minecraft.getInstance().getResourcePackRepository().addPack(SOLID_CHESTS_PACK.getId());
        } else {
            Minecraft.getInstance().getResourcePackRepository().removePack(SOLID_CHESTS_PACK.getId());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBedSettingUpdateEvent(FastModelSettingsUpdate.BedEvent e) {
        if (e.isEnabled()) {
            Minecraft.getInstance().getResourcePackRepository().addPack(SOLID_BEDS_PACK.getId());
        } else {
            Minecraft.getInstance().getResourcePackRepository().removePack(SOLID_BEDS_PACK.getId());
        }
    }

    @SubscribeEvent
    public static void registerResourcePacks(AddPackFindersEvent e) {
        Chloride.LOGGER.info("Registering CHLORIDE built-in packs");
        if (e.getPackType() == PackType.CLIENT_RESOURCES) {
            final ModContainer modFile = ModList.get().getModContainerById(Chloride.ID).get();

            final Path bedsPath = modFile.getModInfo().getOwningFile().getFile().findResource("custom_packs/solid_beds");
            SOLID_BEDS_PACK = Pack.readMetaAndCreate(
                    new PackLocationInfo(Chloride.ID + "_solid_beds", Component.literal("Chloride: Solid Beds"), PackSource.BUILT_IN, Optional.of(new KnownPack(Chloride.ID, "solid_beds", "1.0.0"))),
                    BuiltInPackSource.fromName((path) -> getPathResources(path, bedsPath)),
                    PackType.CLIENT_RESOURCES,
                    new PackSelectionConfig(false, Pack.Position.TOP, true)
            );

            final Path chestsPath = modFile.getModInfo().getOwningFile().getFile().findResource("custom_packs/solid_chests");
            SOLID_CHESTS_PACK = Pack.readMetaAndCreate(
                    new PackLocationInfo(Chloride.ID + "_solid_chests", Component.literal("Chloride: Solid Chests"), PackSource.BUILT_IN, Optional.of(new KnownPack(Chloride.ID, "solid_chests", "1.0.0"))),
                    BuiltInPackSource.fromName((path) -> getPathResources(path, chestsPath)),
                    PackType.CLIENT_RESOURCES,
                    new PackSelectionConfig(false, Pack.Position.TOP, true)
            );

            e.addRepositorySource(consumer -> {
                consumer.accept(SOLID_BEDS_PACK);
                consumer.accept(SOLID_CHESTS_PACK);
            });
        }
    }

    private static PathPackResources getPathResources(PackLocationInfo info, Path path) {
        return new PathPackResources(info, path) {

            @Override public boolean isHidden() {
                return false;
            }
        };
    }
}
