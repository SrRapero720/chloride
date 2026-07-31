package me.srrapero720.chloride.impl.sodium.controls;

import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.widgets.ScrollbarWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.IntConsumer;

/** Scrollbar shared by every Salty layout, with one neutral color scheme. */
public class SaltyScrollbar extends ScrollbarWidget {
    private static final int TRACK_COLOR = Colors.BACKGROUND_MEDIUM;
    private static final int THUMB_COLOR = Colors.FOREGROUND_DISABLED;
    private static final int THUMB_HOVERED_COLOR = Colors.FOREGROUND;

    private final boolean horizontal;
    private int visible;
    private int total;

    public SaltyScrollbar(final Dim2i dim) {
        this(dim, false, null);
    }

    public SaltyScrollbar(final Dim2i dim, final boolean horizontal, final IntConsumer onScrollChange) {
        super(dim, horizontal, true, onScrollChange);
        this.horizontal = horizontal;
    }

    @Override
    public void setScrollbarContext(final int visible, final int total) {
        this.visible = visible;
        this.total = total;
        super.setScrollbarContext(visible, total);
    }

    @Override
    public void setScrollbarContext(final int total) {
        this.setScrollbarContext(this.horizontal ? this.getWidth() : this.getHeight(), total);
    }

    public int visibleAmount() {
        return this.visible;
    }

    public int totalAmount() {
        return this.total;
    }

    public boolean horizontal() {
        return this.horizontal;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        if (!this.canScroll()) return;

        this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), TRACK_COLOR);
        final int length = this.horizontal ? this.getWidth() : this.getHeight();
        final int start = (int) Math.round((double) this.getScrollAmount() / this.total * length);
        final int thumb = Math.max(1, (int) Math.round((double) this.visible / this.total * length));
        final int end = Math.min(length, start + thumb);
        final int color = this.isMouseOver(mouseX, mouseY) ? THUMB_HOVERED_COLOR : THUMB_COLOR;
        if (this.horizontal) {
            this.drawRect(graphics, this.getX() + start, this.getY(), this.getX() + end, this.getLimitY(), color);
        } else {
            this.drawRect(graphics, this.getX(), this.getY() + start, this.getLimitX(), this.getY() + end, color);
        }
    }
}
