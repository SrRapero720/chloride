package me.srrapero720.chloride.impl.sodium.controls;

import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/** Mod header or page entry of TexTrue's grouped vertical tab rail. */
public final class ReeseTabButton extends TabButton {
    private static final int HEADER_BACKGROUND = 0xBE000000;
    private static final int HEADER_HOVERED = 0xE0000000;
    private static final int PAGE_TEXT_X = 10;
    private static final int SELECTION_WIDTH = 2;

    private final ModOptions mod;
    private final Component label;
    private final boolean header;

    public static ReeseTabButton header(final Dim2i dim, final ModOptions mod, final IntSupplier y,
                                        final BooleanSupplier selected, final Runnable action) {
        final Component tooltip = Component.literal(mod.name()).append("\n")
                .append(Component.literal(mod.version()).withStyle(ChatFormatting.GRAY));
        return new ReeseTabButton(dim, mod, Component.literal(mod.name()), tooltip, y, selected, action, true);
    }

    public static ReeseTabButton page(final Dim2i dim, final ModOptions mod, final Component label, final IntSupplier y,
                                      final BooleanSupplier selected, final Runnable action) {
        return new ReeseTabButton(dim, mod, label, label, y, selected, action, false);
    }

    private ReeseTabButton(final Dim2i dim, final ModOptions mod, final Component label, final Component tooltip,
                           final IntSupplier y, final BooleanSupplier selected, final Runnable action, final boolean header) {
        super(dim, mod.theme(), tooltip, y, selected, action);
        this.mod = mod;
        this.label = label;
        this.header = header;
    }

    public ModOptions mod() {
        return this.mod;
    }

    public Component label() {
        return this.label;
    }

    public boolean header() {
        return this.header;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        this.hovered = this.isMouseOver(mouseX, mouseY);
        final boolean selected = this.selected();
        this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), this.header
                ? this.hovered ? HEADER_HOVERED : HEADER_BACKGROUND
                : this.hovered ? Colors.BACKGROUND_HOVER : Colors.BACKGROUND_LIGHT);

        if (selected) {
            this.drawRect(graphics, this.getX(), this.getY(), this.getX() + SELECTION_WIDTH, this.getLimitY(), this.accent());
        }

        if (this.header) this.renderHeader(graphics, selected);
        else this.renderPage(graphics, selected);
        this.drawFocus(graphics);
    }

    private void renderHeader(final GuiGraphics graphics, final boolean selected) {
        final int color = selected ? this.mod.theme().themeLighter : Colors.FOREGROUND;
        final int offset = this.mod.icon() == null ? Layout.OPTION_TEXT_SIDE_PADDING
                : VideoSettingsScreen.renderIconWithSpacing(graphics, this.mod.icon(), color, this.mod.iconMonochrome(),
                        this.getX(), this.getY(), this.getHeight(), Layout.ICON_MARGIN);
        final int textX = this.getX() + offset;
        final int available = this.getLimitX() - textX - Layout.OPTION_TEXT_SIDE_PADDING;
        this.drawString(graphics, this.truncateTextToFit(this.mod.name(), available), textX, this.getY() + 2, color);
        this.drawString(graphics, this.truncateTextToFit(this.mod.version(), available), textX, this.getY() + 12,
                this.mod.theme().themeDarker);
    }

    private void renderPage(final GuiGraphics graphics, final boolean selected) {
        final int color = selected ? this.mod.theme().themeLighter
                : this.hovered ? this.mod.theme().theme : this.mod.theme().themeDarker;
        final String text = this.truncateTextToFit(this.label.getString(), this.getWidth() - PAGE_TEXT_X - Layout.OPTION_TEXT_SIDE_PADDING);
        this.drawString(graphics, text, this.getX() + PAGE_TEXT_X,
                this.getCenterY() + Layout.REGULAR_TEXT_BASELINE_OFFSET, color);
    }

}
