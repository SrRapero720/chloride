package me.srrapero720.chloride.mixins.impl;

import me.srrapero720.chloride.ChlorideConfig;
import net.minecraft.client.gui.components.toasts.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public class ToastsMixin {
    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void chloride$toasts(final Toast toast, final CallbackInfo ci) {
        final var cfg = ChlorideConfig.ui.toasts;
        if (!cfg.enabled
                || (!cfg.advancements && toast instanceof AdvancementToast)
                || (!cfg.recipes && toast instanceof RecipeToast)
                || (!cfg.system && toast instanceof SystemToast)
                || (!cfg.tutorial && toast instanceof TutorialToast)) {
            ci.cancel();
        }
    }
}
