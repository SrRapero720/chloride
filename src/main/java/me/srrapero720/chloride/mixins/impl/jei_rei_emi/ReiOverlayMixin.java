package me.srrapero720.chloride.mixins.impl.jei_rei_emi;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget;
import me.srrapero720.chloride.ChlorideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ScreenOverlayImpl.class, priority = 500, remap = false)
@Pseudo
public class ReiOverlayMixin {
    @WrapOperation(method = "renderWidgets", at = @At(value = "INVOKE", target = "Lme/shedaniel/rei/api/client/gui/widgets/Widget;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private void redirect$rendering(final Widget instance, final GuiGraphics graphics, final int mouseX, final int mouseY, final float deltaTick, final Operation<Void> original) {
        if (instance instanceof final EntryListWidget widget) {
            if (!ChlorideConfig.ui.hideJREMI || !REIRuntimeImpl.getSearchField().getText().isEmpty()) {
                original.call(instance, graphics, mouseX, mouseY, deltaTick);
            } else {
                if (!ChlorideConfig.ui.hideJREMIHint) {
                    final Rectangle rectangle = widget.getBounds();
                    graphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("chloride.jei.message"), rectangle.getCenterX(), rectangle.getCenterY(), 0xFFFFFF);
                }
            }
        } else {
            original.call(instance, graphics, mouseX, mouseY, deltaTick);
        }
    }
}