package me.srrapero720.chloride.impl.sodium.controls;

import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.ScrollbarWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Floating list of choices a dropdown row unfolds. It sits outside the layout: the screen draws it last, over
 * whatever the body is showing, and hands it input first. Long lists cap their height and scroll instead.
 */
public class DropdownOverlay extends AbstractWidget {
    /** How many choices the list shows before it starts scrolling. */
    public static final int MAX_ROWS = 8;
    private final ResourceLocation id;
    private final OptionRows.Choices choices;
    private final ColorTheme theme;
    private final ScrollbarWidget scrollbar;
    private final int rowHeight;

    public DropdownOverlay(final OptionRows.Row row, final ResourceLocation id, final OptionRows.Choices choices, final Dim2i frame,
                           final ColorTheme theme) {
        super(dimension(row, choices, frame));
        this.id = id;
        this.choices = choices;
        this.theme = theme;
        this.rowHeight = row.getHeight();
        this.scrollbar = new SaltyScrollbar(new Dim2i(this.getLimitX() - Layout.SCROLLBAR_WIDTH, this.getY(),
                Layout.SCROLLBAR_WIDTH, this.getHeight()));
        this.scrollbar.setScrollbarContext(choices.labels().size() * this.rowHeight);
    }

    // ANCHORED TO THE FIELD OF THE ROW THAT OPENED IT, BELOW WHEN THERE IS ROOM AND ABOVE OTHERWISE
    public static Dim2i dimension(final OptionRows.Row row, final OptionRows.Choices choices, final Dim2i frame) {
        final int height = Math.min(choices.labels().size(), MAX_ROWS) * row.getHeight();
        final int x = row.fieldX();
        final int below = row.getLimitY();
        final int y = below + height <= frame.getLimitY() ? below : Math.max(frame.y(), row.getY() - height);
        return new Dim2i(x, y, row.contentLimitX() - x, height);
    }

    public ResourceLocation id() {
        return this.id;
    }

    public OptionRows.Choices choices() {
        return this.choices;
    }

    public ColorTheme theme() {
        return this.theme;
    }

    public ScrollbarWidget scrollbar() {
        return this.scrollbar;
    }

    public int rowHeight() {
        return this.rowHeight;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        graphics.pose().pushPose();
        // OVER EVERY WIDGET OF THE BODY, BUT UNDER THE TOOLTIP THE SCREEN DRAWS AFTERWARDS
        graphics.pose().translate(0.0f, 0.0f, 300.0f);
        this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), Colors.BACKGROUND_OVERLAY);
        this.drawBorder(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), this.theme.theme);

        final int hovered = this.indexAt(mouseX, mouseY);
        final int active = this.choices.index().getAsInt();
        final int scroll = this.scrollbar.getScrollAmount();
        final int textLimit = this.getLimitX() - (this.scrollbar.canScroll() ? Layout.SCROLLBAR_WIDTH : 0);
        graphics.enableScissor(this.getX(), this.getY(), this.getLimitX(), this.getLimitY());
        for (int i = scroll / this.rowHeight; i < this.choices.labels().size(); i++) {
            final int top = this.getY() + i * this.rowHeight - scroll;
            if (top >= this.getLimitY()) break;

            final boolean lit = i == hovered || i == active;
            if (i == hovered) this.drawRect(graphics, this.getX() + 1, top, textLimit, top + this.rowHeight, Colors.BACKGROUND_HOVER);
            final Component marker = lit ? OptionRows.ENTRY_HOVERED : OptionRows.ENTRY;
            final int markerX = this.getX() + Layout.OPTION_TEXT_SIDE_PADDING;
            final int textX = markerX + this.font.width(marker) + Layout.OPTION_GROUP_MARGIN;
            final int baseline = top + this.rowHeight / 2 + Layout.REGULAR_TEXT_BASELINE_OFFSET;
            this.drawString(graphics, marker, markerX, baseline, this.theme.theme);
            this.drawString(graphics, this.truncateTextToFit(this.choices.labels().get(i).getString(), textLimit - textX - Layout.OPTION_TEXT_SIDE_PADDING),
                    textX, baseline, lit ? this.theme.theme : Colors.FOREGROUND);
        }
        graphics.disableScissor();

        this.scrollbar.render(graphics, mouseX, mouseY, delta);
        graphics.pose().popPose();
    }

    /** Picks the choice under the cursor; {@code false} means the click landed outside and should dismiss it. */
    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button != 0 || !this.isMouseOver(mouseX, mouseY)) return false;
        if (this.scrollbar.mouseClicked(mouseX, mouseY, button)) return true;

        final int index = this.indexAt((int) mouseX, (int) mouseY);
        if (index < 0) return true;
        this.playClickSound();
        this.choices.select().accept(index);
        return false;
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        return this.scrollbar.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        return this.scrollbar.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double horizontal, final double vertical) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;
        this.scrollbar.scroll((int) (-vertical * this.rowHeight));
        return true;
    }

    private int indexAt(final int mouseX, final int mouseY) {
        if (!this.isMouseOver(mouseX, mouseY)) return -1;
        final int index = (this.scrollbar.getScrollAmount() + mouseY - this.getY()) / this.rowHeight;
        return index < this.choices.labels().size() ? index : -1;
    }
}
