package me.srrapero720.chloride.impl.sodium.controls;

import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

/** Mod category or page entry used by Populi's independent navigation layout. */
public final class PopuliTabButton extends TabButton {
    private static final int PAGE_TEXT_X = 10;
    private static final int SELECTION_WIDTH = 2;

    private final ModOptions mod;
    private final Component label;
    private final Component version;
    private final ResourceLocation icon;
    private final boolean page;
    private final boolean iconOnly;

    /** Creates an adaptive mod category with icon, name and optional version. */
    public static PopuliTabButton mod(final Dim2i dim, final ModOptions mod, final Detail detail,
                                      final BooleanSupplier selected, final Runnable action) {
        final ModContent content = modContent(mod, detail);
        return new PopuliTabButton(dim, mod, content.label(), content.version(), mod.icon(), content.tooltip(), selected, action,
                false, content.iconOnly());
    }

    /** Creates a page entry with Populi's selected accent thumb. */
    public static PopuliTabButton page(final Dim2i dim, final ModOptions mod, final Component label,
                                       final BooleanSupplier selected, final Runnable action) {
        return new PopuliTabButton(dim, mod, label, null, null, label, selected, action, true, false);
    }

    private PopuliTabButton(final Dim2i dim, final ModOptions mod, @Nullable final Component label,
                            @Nullable final Component version, @Nullable final ResourceLocation icon,
                            final Component tooltip, final BooleanSupplier selected, final Runnable action,
                            final boolean page, final boolean iconOnly) {
        super(dim, mod.theme(), tooltip, null, selected, action);
        this.mod = mod;
        this.version = version;
        this.icon = icon;
        this.page = page;
        this.iconOnly = iconOnly;

        final String text = label == null ? null : label.getString();
        final int reserved = page ? PAGE_TEXT_X + Layout.OPTION_TEXT_SIDE_PADDING
                : (icon == null ? Layout.OPTION_TEXT_SIDE_PADDING : Layout.BUTTON_SHORT) + Layout.OPTION_TEXT_SIDE_PADDING
                + (version == null ? 0 : this.font.width(version) + Layout.OPTION_TEXT_SIDE_PADDING);
        final String fitted = text == null ? null : this.truncateTextToFit(text, dim.width() - reserved);
        this.label = fitted == null ? null : Component.literal(fitted);
    }

    public ModOptions mod() {
        return this.mod;
    }

    @Nullable
    public Component label() {
        return this.label;
    }

    @Nullable
    public Component version() {
        return this.version;
    }

    @Nullable
    public ResourceLocation icon() {
        return this.icon;
    }

    public boolean page() {
        return this.page;
    }

    public boolean iconOnly() {
        return this.iconOnly;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        this.hovered = this.isMouseOver(mouseX, mouseY);
        final boolean selected = this.selected();
        this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(),
                this.hovered ? Colors.BACKGROUND_HOVER : selected ? Colors.BACKGROUND_DARKER : Colors.BACKGROUND_LIGHT);

        if (this.page) {
            if (selected) {
                this.drawRect(graphics, this.getX(), this.getY(), this.getX() + SELECTION_WIDTH, this.getLimitY(), this.accent());
            }
            final int color = selected ? this.mod.theme().themeLighter
                    : this.hovered ? this.mod.theme().theme : this.mod.theme().themeDarker;
            this.drawString(graphics, this.label, this.getX() + PAGE_TEXT_X,
                    this.getCenterY() + Layout.REGULAR_TEXT_BASELINE_OFFSET, color);
        } else {
            this.renderMod(graphics, selected);
        }

        this.drawFocus(graphics);
    }

    private void renderMod(final GuiGraphics graphics, final boolean selected) {
        final int color = selected ? this.mod.theme().themeLighter
                : this.hovered ? this.mod.theme().theme : this.mod.theme().themeDarker;
        int offset = Layout.OPTION_TEXT_SIDE_PADDING;
        if (this.icon != null) {
            offset = VideoSettingsScreen.renderIconWithSpacing(graphics, this.icon, color, this.mod.iconMonochrome(), this.getX(), this.getY(), this.getHeight(), Layout.ICON_MARGIN);
        }
        final int baseline = this.getCenterY() + (this.icon == null ? Layout.REGULAR_TEXT_BASELINE_OFFSET : Layout.ICON_TEXT_BASELINE_OFFSET);
        if (this.label != null) {
            if (this.iconOnly) {
                this.drawCenteredString(graphics, this.label, this.getCenterX(), baseline, color);
            } else {
                this.drawString(graphics, this.label, this.getX() + offset, baseline, color);
            }
        }
        if (this.version != null) {
            this.drawString(graphics, this.version,
                    this.getLimitX() - Layout.OPTION_TEXT_SIDE_PADDING - this.font.width(this.version), baseline,
                    this.mod.theme().themeDarker);
        }
    }

}
