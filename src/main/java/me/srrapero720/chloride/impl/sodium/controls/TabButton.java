package me.srrapero720.chloride.impl.sodium.controls;

import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/** Shared state and interaction of Chloride's navigation tabs. */
public abstract class TabButton extends AbstractWidget {
    /** Amount of mod identity kept when fitting mod tabs into a row. */
    public enum Detail {
        FULL, NAME, ICON
    }

    protected final ColorTheme theme;
    private Component tooltip;
    private final IntSupplier y;
    private final BooleanSupplier selected;
    private final Runnable action;

    protected TabButton(final Dim2i dim, final ColorTheme theme, @Nullable final Component tooltip,
                        @Nullable final IntSupplier y, final BooleanSupplier selected, final Runnable action) {
        super(dim);
        this.theme = theme;
        this.tooltip = tooltip;
        this.y = y;
        this.selected = selected;
        this.action = action;
    }

    /** Width a mod tab needs at the requested detail level. */
    public static int modWidth(final Font font, final ModOptions mod, final Detail detail) {
        return switch (detail) {
            case FULL -> Layout.BUTTON_SHORT + font.width(mod.name()) + Layout.OPTION_TEXT_SIDE_PADDING * 2 + font.width(mod.version());
            case NAME -> Layout.BUTTON_SHORT + font.width(mod.name()) + Layout.OPTION_TEXT_SIDE_PADDING;
            case ICON -> Layout.BUTTON_SHORT;
        };
    }

    /** Common display data for a mod tab. */
    public static ModContent modContent(final ModOptions mod, final Detail detail) {
        final boolean iconOnly = detail == Detail.ICON;
        final String name = mod.name();
        final Component label = iconOnly
                ? mod.icon() == null ? Component.literal(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT)) : null
                : Component.literal(name);
        final Component version = detail == Detail.FULL ? Component.literal(mod.version()) : null;
        final Component tooltip = Component.literal(name).append("\n")
                .append(Component.literal(mod.version()).withStyle(ChatFormatting.GRAY));
        return new ModContent(label, version, tooltip, iconOnly);
    }

    @Nullable
    public final Component tooltip() {
        return this.tooltip;
    }

    protected final void tooltip(final Component tooltip) {
        this.tooltip = tooltip;
    }

    public final int accent() {
        return this.theme.theme;
    }

    public final ColorTheme theme() {
        return this.theme;
    }

    public final boolean selected() {
        return this.selected.getAsBoolean();
    }

    @Override
    public int getY() {
        return this.y == null ? super.getY() : this.y.getAsInt();
    }

    protected final void drawFocus(final GuiGraphics graphics) {
        if (this.isFocused()) {
            this.drawBorder(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), Colors.BUTTON_BORDER);
        }
    }

    @Override
    public final boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button != 0 || !this.isMouseOver(mouseX, mouseY)) return false;
        this.activate();
        return true;
    }

    @Override
    public final boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (!this.isFocused() || !CommonInputs.selected(keyCode)) return false;
        this.activate();
        return true;
    }

    private void activate() {
        this.action.run();
        this.playClickSound();
    }

    public record ModContent(@Nullable Component label, @Nullable Component version, Component tooltip, boolean iconOnly) {}
}
