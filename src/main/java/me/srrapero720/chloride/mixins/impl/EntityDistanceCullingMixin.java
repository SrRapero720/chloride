package me.srrapero720.chloride.mixins.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.api.IRenderableEntity;
import me.srrapero720.chloride.impl.EntityCulling;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static me.srrapero720.chloride.Chloride.LOGGER;

public class EntityDistanceCullingMixin {

    @Mixin(LevelRenderer.class)
    public static class LevelRendererEntityMixin {
        @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
        public Iterable<Entity> redirect$entitiesForRendering(final ClientLevel instance) {
            // UNLIMITED
            if (ChlorideConfig.entities.culling.entityLimit >= 512) return instance.entitiesForRendering();
            // NONE
            if (ChlorideConfig.entities.culling.entityLimit <= 0) return Collections.emptyList();

            // GET ALL ENTITIES
            final List<Entity> copy = new ArrayList<>();
            for (final Entity entity: instance.entitiesForRendering()) {
                copy.add(entity);
            }

            // SORTS TO THE NEAREST ONE
            copy.sort(EntityCulling.DISTANCE_COMPARATOR);

            // LIMIT ENTITIES
            final List<Entity> limited = new ArrayList<>(ChlorideConfig.entities.culling.entityLimit);
            for (final Entity entity: copy) {
                limited.add(entity);
                if (limited.size() >= ChlorideConfig.entities.culling.entityLimit) break;
            }

            return limited;
        }
    }

    @Mixin(EntityRenderDispatcher.class)
    public static class EntityDispatcherMixin {
        @Inject(at = @At("HEAD"), method = "shouldRender", cancellable = true)
        public <E extends Entity> void inject$shouldRender(final E entity, final Frustum clippingHelper, final double x, final double y, final double z, final CallbackInfoReturnable<Boolean> cir) {

            final int distY;
            final int distX;
            final MobCategory category = entity.getType().getCategory();
            if (category == MobCategory.MONSTER) {
                if (!ChlorideConfig.entities.culling.monsters) return;
                distY = ChlorideConfig.entities.culling.monsterDistanceY;
                distX = ChlorideConfig.entities.culling.monsterDistanceX;
            } else {
                if (!ChlorideConfig.entities.culling.entities) return;
                distY = ChlorideConfig.entities.culling.entityDistanceY;
                distX = ChlorideConfig.entities.culling.entityDistanceX;
            }

            final boolean isWhitelisted = ((IRenderableEntity) entity.getType()).chloride$whitelisted();
            // IF IT NOT WHITELISTED AND (DISTX AND DISTY IS ZERO OR THE ENTITY IS NOT IN RANGE)
            // CANCEL RENDERING
            if (!isWhitelisted && (distX + distY == 0 || !EntityCulling.isEntityInRange(entity, x, y, z, distY, distX))) {
                cir.setReturnValue(false);
            }
        }
    }

    @Mixin(EntityType.class)
    public abstract static class EntityTypeMixin implements IRenderableEntity {
        @Unique private static final Marker e$IT = MarkerManager.getMarker("EntityCulling");
        @Unique private boolean chloride$checked = false;
        @Unique private boolean chloride$whitelisted = false;

        @Shadow
        public abstract MobCategory getCategory();

        @Override
        @Unique
        public boolean chloride$whitelisted() {
            if (this.chloride$checked) return this.chloride$whitelisted;

            final var resource = this.embPlus$resourceLocation();
            if (resource == null) {
                LOGGER.warn(e$IT, "Entity Key '{}' is null, not whitelisting a broken mod entity", this.getClass().getName());
                return false;
            }

            this.chloride$whitelisted = EntityCulling.isWhitelisted(resource, this.getCategory() == MobCategory.MONSTER ? ChlorideConfig.entities.culling.monsterWhitelist : ChlorideConfig.entities.culling.entityWhitelist);
            this.chloride$checked = true;

            LOGGER.debug(e$IT,"Computed Entity whitelist with type {} for {}-{}", this.getCategory().name(), resource.toString(), this.chloride$whitelisted);
            return this.chloride$whitelisted;
        }

        @Unique
        public ResourceLocation embPlus$resourceLocation() {
            return BuiltInRegistries.ENTITY_TYPE.getKey(this.embPlus$cast());
        }

        @Unique
        private EntityType<?> embPlus$cast() {
            return (EntityType<?>) ((Object) this);
        }
    }

    @Mixin(BlockEntityRenderDispatcher.class)
    public static class TileDispatcherMixin {
        @Shadow public Camera camera;

        @Inject(at = @At("HEAD"), method = "render", cancellable = true)
        public <E extends BlockEntity> void render(final E tile, final float val, final PoseStack matrix, final MultiBufferSource bufferSource, final CallbackInfo ci) {
            if (!ChlorideConfig.entities.culling.tileEntities) return;

            final boolean isWhitelisted = ((IRenderableEntity) tile.getType()).chloride$whitelisted();
            // IF IT NOT WHITELISTED AND (DISTX AND DISTY IS ZERO OR THE TILE IS NOT IN RANGE)
            // CANCEL RENDERING
            if (!isWhitelisted && (ChlorideConfig.entities.culling.tileEntityDistanceY + ChlorideConfig.entities.culling.tileEntityDistanceX == 0 || !EntityCulling.isEntityInRange(tile, this.camera.getPosition(),
                    ChlorideConfig.entities.culling.tileEntityDistanceY,
                    ChlorideConfig.entities.culling.tileEntityDistanceX)
            )) {
                ci.cancel();
            }
        }
    }

    @Mixin(BlockEntityType.class)
    public abstract static class TileEntityTypeMixin implements IRenderableEntity {
        @Unique private static final Marker e$IT = MarkerManager.getMarker("BlockEntityCulling");
        @Unique private boolean chloride$checked = false;
        @Unique private boolean chloride$whitelisted = false;

        @Override
        public boolean chloride$whitelisted() {
            if (this.chloride$checked) return this.chloride$whitelisted;
            final ResourceLocation resource = BlockEntityType.getKey((BlockEntityType<?>) ((Object) this));
            if (resource == null) {
                LOGGER.warn(e$IT, "BlockEntity key for '{}' is null, not whitelisting a broken mod block", this.getClass().getName());
                return false;
            }
            this.chloride$whitelisted = EntityCulling.isWhitelisted(resource, ChlorideConfig.entities.culling.tileEntityWhitelist);
            this.chloride$checked = true;

            LOGGER.debug(e$IT,"Computed BlockEntity whitelist for {}-{}", resource.toString(), this.chloride$whitelisted);
            return this.chloride$whitelisted;
        }
    }
}
