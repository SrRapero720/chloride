package me.srrapero720.chloride.impl.sodium.controls;

import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/**
 * Chloride's header-ribbon tab: one per mod that exposes options and one per page of the selected mod. Tabs
 * sit flush against each other, share the frame width and are tinted with the mod's theme.
 */
public class ChlorideTabButton extends TabButton {
    // IDLE TABS ARE FADED AND THE VERSION EVEN MORE, SO A NEON THEME NEVER OUTSHINES THE ACTIVE FOLDER
    private static final int HOVER_ALPHA = 0xCC000000;
    private static final int IDLE_ALPHA = 0x99000000;
    private static final int VERSION_ALPHA = 0x59000000;

    private final Component label;
    private final Component version;
    private final ResourceLocation icon;
    private final boolean monochrome;
    private final boolean centered;
    /** Tab for a mod: icon, name and version, trimmed down to whatever the ribbon has room for. */
    public static ChlorideTabButton mod(final Dim2i dim, final ModOptions mod, final Detail detail, final IntSupplier y,
                                         final BooleanSupplier selected, final Runnable action) {
        final ModContent content = modContent(mod, detail);
        return new ChlorideTabButton(dim, content.label(), content.version(), mod.icon(), mod.iconMonochrome(), content.iconOnly(),
                mod.theme(), content.tooltip(), y, selected, action);
    }

    /**
     * Wide tab for a page, showing its name centered and truncated to the tab width. Its vertical position is
     * polled every frame because ribbon rows slide around to keep the selected one against the option list.
     */
    public static ChlorideTabButton page(final Dim2i dim, final Component label, final ColorTheme theme, final IntSupplier y,
                                 final BooleanSupplier selected, final Runnable action) {
        return new ChlorideTabButton(dim, label, null, null, false, true, theme, null, y, selected, action);
    }

    private ChlorideTabButton(final Dim2i dim, @Nullable final Component label, @Nullable final Component version, @Nullable final ResourceLocation icon,
                      final boolean monochrome, final boolean centered, final ColorTheme theme, @Nullable final Component tooltip,
                      @Nullable final IntSupplier y, final BooleanSupplier selected, final Runnable action) {
        super(dim, theme, tooltip, y, selected, action);
        this.version = version;
        this.icon = icon;
        this.monochrome = monochrome;
        this.centered = centered;

        // TABS SHARE THE ROW WIDTH, SO LONG NAMES GET CUT AND FALL BACK TO A TOOLTIP WITH THE FULL TEXT
        final String text = label == null ? null : label.getString();
        int reserved = (icon == null ? Layout.OPTION_TEXT_SIDE_PADDING : Layout.BUTTON_SHORT) + Layout.OPTION_TEXT_SIDE_PADDING;
        if (version != null) reserved += this.font.width(version) + Layout.OPTION_TEXT_SIDE_PADDING;
        final String fitted = text == null ? null : this.truncateTextToFit(text, dim.width() - reserved);
        this.label = fitted == null ? null : Component.literal(fitted);
        if (tooltip == null && fitted != null && !fitted.equals(text)) this.tooltip(label);
    }

    private static int fade(final int color, final int alpha) {
        return color & 0xFFFFFF | alpha;
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

    public boolean monochrome() {
        return this.monochrome;
    }

    public boolean centered() {
        return this.centered;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        this.hovered = this.isMouseOver(mouseX, mouseY);
        final boolean selected = this.selected();

        // SELECTING A TAB MAKES ITS TRANSLUCENT BLACK FILL HEAVIER; HOVERING PUSHES IT FURTHER, LIKE SODIUM'S ENTRIES
        this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(),
                this.hovered ? Colors.BACKGROUND_HOVER : selected ? Colors.BACKGROUND_DARKER : Colors.BACKGROUND_LIGHT);

        final int color = selected ? this.theme.themeLighter
                : this.hovered ? fade(this.theme.theme, HOVER_ALPHA) : fade(this.theme.themeDarker, IDLE_ALPHA);
        int offset = Layout.OPTION_TEXT_SIDE_PADDING;
        if (this.icon != null) {
            offset = VideoSettingsScreen.renderIconWithSpacing(graphics, this.icon, color, this.monochrome, this.getX(), this.getY(),
                    this.getHeight(), Layout.ICON_MARGIN);
        }
        final int baseline = this.getCenterY() + (this.icon == null ? Layout.REGULAR_TEXT_BASELINE_OFFSET : Layout.ICON_TEXT_BASELINE_OFFSET);
        if (this.label != null) {
            if (this.centered) this.drawCenteredString(graphics, this.label, this.getCenterX(), baseline, color);
            else this.drawString(graphics, this.label, this.getX() + offset, baseline, color);
        }
        // THE VERSION TRAILS ON THE RIGHT EDGE, FAR DIMMER THAN THE NAME
        if (this.version != null) {
            this.drawString(graphics, this.version, this.getLimitX() - Layout.OPTION_TEXT_SIDE_PADDING - this.font.width(this.version),
                    baseline, fade(selected ? this.theme.theme : this.theme.themeDarker, VERSION_ALPHA));
        }

        // THE SELECTED TAB ALWAYS SITS ON THE BOTTOM ROW, SO ITS BAR MERGES WITH THE RIBBON'S EDGE
        if (selected) {
            this.drawRect(graphics, this.getX(), this.getLimitY() - 1, this.getLimitX(), this.getLimitY(), this.theme.theme);
        }
        this.drawFocus(graphics);
    }
}
