package me.srrapero720.chloride;

import me.srrapero720.chloride.impl.Borderless;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@Mod(Chloride.ID)
@EventBusSubscriber(value = Dist.CLIENT)
public class Chloride {
    public static final String ID = "chloride";
    public static final Logger LOGGER = LogManager.getLogger("chloride");
    public static final Marker IT = MarkerManager.getMarker("Main");

    public Chloride() {
        if (FMLLoader.getDist().isClient()) {
            LOGGER.info(IT, "Chloride is here, lets make your experience taste-able");
        } else {
            LOGGER.info(IT, "Chloride is not intended to be on servers, loaded in inner mode");
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void load(final FMLClientSetupEvent event) {
        LOGGER.info("LOADED CHLORIDE");

        // RECONCILE CHLORIDE CONFIG WITH VANILLA OPTIONS.FULLSCREEN AT BOOT. THE WINDOW IS CONSTRUCTED
        // USING OPTIONS.FULLSCREEN AS THE INITIAL STATE; IF IT DESYNCS FROM CHLORIDECONFIG.FULLSCREEN
        // (E.G. MANUAL CONFIG EDIT, MOD INSTALLED OVER EXISTING OPTIONS.TXT) THE WINDOW STARTS IN THE
        // WRONG MODE. ENQUEUE ON THE MAIN THREAD SO setMode() RUNS AFTER THE WINDOW IS READY.
        event.enqueueWork(() -> {
            final Minecraft mc = Minecraft.getInstance();
            final boolean optsFullscreen = mc.options.fullscreen().get();
            final boolean configFullscreen = ChlorideConfig.fullscreen.mode != Borderless.Mode.WINDOWED;
            if (optsFullscreen != configFullscreen) {
                Borderless.setFullScreenMode(ChlorideConfig.fullscreen.mode);
            }

            // MC NEVER PERSISTS ADVANCED TOOLTIPS (F3+H IS SESSION-ONLY).
            mc.options.advancedItemTooltips = ChlorideConfig.ui.advancedTooltips;
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void earlyLoad() {
        ChlorideConfig.load(FMLLoader.getGamePath().resolve("config"));
    }

    public static boolean installed(final String modid) {
        return FMLLoader.getLoadingModList().getModFileById(modid) != null;
    }

    /** Builds a stable, unique option id from a config field name (camelCase -&gt; chloride:snake_case). */
    public static ResourceLocation id(final String field) {
        final String path = field.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    /** Builds a unique option id for a per-registry-entry option (e.g. one toggle per particle type). */
    public static ResourceLocation id(final String prefix, final ResourceLocation key) {
        return ResourceLocation.fromNamespaceAndPath(ID, prefix + "/" + key.getNamespace() + "/" + key.getPath());
    }
}