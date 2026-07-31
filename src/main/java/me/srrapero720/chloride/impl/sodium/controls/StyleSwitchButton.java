package me.srrapero720.chloride.impl.sodium.controls;

import me.srrapero720.chloride.impl.SettingsScreens;
import me.srrapero720.chloride.impl.sodium.SodiumFeatures;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StyleSwitchButton extends FlatButtonWidget {
    private final Component tooltip;

    public StyleSwitchButton(final Dim2i dim, final SettingsScreens.Style current, final Screen prev) {
        // NULL LABEL: THE DEFAULT THEME ALREADY MATCHES THE SEARCH BAR'S TRANSLUCENT BLACK FILL
        super(dim, null, () -> SettingsScreens.next(current, prev), true, false);
        this.tooltip = Component.translatable("chloride.settings.design", current.title());
    }

    public Component tooltip() {
        return this.tooltip;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        if (!this.isVisible()) return;
        super.render(graphics, mouseX, mouseY, delta);
        // MONOCHROME MASK: THE WHITE LOGO IS TINTED LIKE LABEL TEXT (WHITE, GRAYED OUT WHEN DISABLED)
        VideoSettingsScreen.renderIconWithSpacing(graphics, SodiumFeatures.LOGO, this.getTextColor(), true, this.getX(), this.getY(), this.getHeight(), 2);

        if (this.isHovered()) {
            // SAME FLAT LOOK AS SODIUM'S TOOLTIPS, LIFTED +400 SO IT DRAWS OVER SIBLING WIDGETS
            final int x = this.getX();
            final int y = this.getLimitY() + 3;
            graphics.pose().pushPose();
            graphics.pose().translate(0.0f, 0.0f, 400.0f);
            graphics.fill(x, y, x + this.font.width(this.tooltip) + 8, y + this.font.lineHeight + 8, Colors.BACKGROUND_OVERLAY);
            this.drawString(graphics, this.tooltip, x + 4, y + 4, Colors.FOREGROUND);
            graphics.pose().popPose();
        }
    }
}
