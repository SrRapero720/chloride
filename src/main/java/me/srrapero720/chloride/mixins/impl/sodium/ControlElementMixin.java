package me.srrapero720.chloride.mixins.impl.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.srrapero720.chloride.impl.sodium.controls.SettingsList;
import net.caffeinemc.mods.sodium.client.config.structure.Option;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.options.control.AbstractOptionList;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Paints the label of the hovered option in the mod's accent colour, which Chloride's settings screen pairs
 * with the chevron it draws on the same row. Sodium's own screen keeps its plain white labels.
 */
@Mixin(ControlElement.class)
public abstract class ControlElementMixin {
    @Shadow @Final protected AbstractOptionList list;
    @Shadow @Final protected ColorTheme theme;

    @Shadow public abstract Option getOption();

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/gui/options/control/ControlElement;drawString(Lnet/minecraft/client/gui/GuiGraphics;Ljava/lang/String;III)V"))
    private void chloride$accentHoveredLabel(final ControlElement self, final GuiGraphics graphics, final String label, final int x, final int y,
                                             final int color, final Operation<Void> original) {
        if (!(this.list instanceof SettingsList) || !self.isHovered() || !this.getOption().isEnabled()) {
            original.call(self, graphics, label, x, y, color);
            return;
        }
        // THE LABEL CARRIES ITS OWN WHITE COLOUR CODE, WHICH WOULD WIN OVER THE COLOUR ARGUMENT
        original.call(self, graphics, label.replace(ChatFormatting.WHITE.toString(), ""), x, y, this.theme.theme);
    }
}
