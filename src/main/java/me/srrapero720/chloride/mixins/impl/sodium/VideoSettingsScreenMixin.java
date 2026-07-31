package me.srrapero720.chloride.mixins.impl.sodium;

import me.srrapero720.chloride.ChlorideConfig;
import me.srrapero720.chloride.impl.SettingsScreens;
import me.srrapero720.chloride.impl.sodium.controls.StyleSwitchButton;
import net.caffeinemc.mods.sodium.client.config.structure.OptionPage;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VideoSettingsScreen.class)
public class VideoSettingsScreenMixin {
    @Shadow @Final private Screen prevScreen;

    // CEDE A SQUARE SLOT PLUS A GAP ON THE LEFT OF THE SEARCH BAR SO THE BUTTON READS AS ITS OWN ELEMENT
    @ModifyArg(method = "rebuild", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gui/widgets/SearchWidget;<init>(Ljava/util/function/Consumer;Lnet/caffeinemc/mods/sodium/client/util/Dim2i;)V"), index = 1)
    private Dim2i chloride$shiftSearchBar(final Dim2i dim) {
        final int inset = Layout.BUTTON_SHORT + Layout.INNER_MARGIN;
        return new Dim2i(dim.x() + inset, dim.y(), dim.width() - inset, dim.height());
    }

    // THE BAR'S FILL WIDTH IS TRACKED APART FROM ITS DIM; SHRINK IT BY THE SAME AMOUNT
    @ModifyArg(method = "updateSearchWidgetWidth", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gui/widgets/SearchWidget;updateWidgetWidth(I)V"))
    private int chloride$shrinkSearchBar(final int width) {
        return width - Layout.BUTTON_SHORT - Layout.INNER_MARGIN;
    }

    // THE PERSISTED STYLE DECIDES WHICH SCREEN THE VIDEO SETTINGS BUTTON OPENS; SODIUM'S OWN IS THE FALLBACK
    @Inject(method = "createScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/caffeinemc/mods/sodium/client/config/structure/OptionPage;)Lnet/minecraft/client/gui/screens/Screen;",
            at = @At("HEAD"), cancellable = true)
    private static void chloride$openStyledScreen(final Screen current, final OptionPage page, final CallbackInfoReturnable<Screen> cir) {
        final SettingsScreens.Style style = ChlorideConfig.ui.settingsScreen;
        if (style != SettingsScreens.Style.SODIUM) cir.setReturnValue(style.screen(current));
    }

    // ADDED AT TAIL SO THE BUTTON (AND ITS TOOLTIP) RENDERS ABOVE EVERY OTHER WIDGET
    @Inject(method = "rebuild", at = @At("TAIL"))
    private void chloride$styleButton(final CallbackInfo ci) {
        final VideoSettingsScreen self = (VideoSettingsScreen) (Object) this;
        final Dim2i slot = new Dim2i(self.getX(), self.getY(), Layout.BUTTON_SHORT, Layout.BUTTON_SHORT);
        self.addRenderableWidget(new StyleSwitchButton(slot, SettingsScreens.Style.SODIUM, this.prevScreen));
    }
}
