package me.srrapero720.chloride.impl.sodium.controls;

import me.srrapero720.chloride.impl.sodium.screens.SaltySettingsScreen;
import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.config.structure.Page;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractParentWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.ScrollbarWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Grouped and scrollable vertical navigation used by TexTrue's Reese-inspired design. */
public final class ReeseTabRail extends AbstractParentWidget {
    public static final int HEADER_HEIGHT = 22;
    public static final int PAGE_HEIGHT = 18;
    public static final int GROUP_PADDING = 4;

    private final List<ReeseTabButton> buttons = new ArrayList<>();
    private final ScrollbarWidget scrollbar;

    public ReeseTabRail(final Dim2i dim, final List<Group> groups, final Set<String> collapsed, final ModOptions selectedMod,
                        final Page selectedPage, final int scroll, final Consumer<ModOptions> toggleGroup,
                        final BiConsumer<ModOptions, Page> selectPage) {
        super(dim);
        final boolean headers = groups.size() > 1;
        final int contentHeight = contentHeight(groups, collapsed, headers);
        this.scrollbar = new SaltyScrollbar(new Dim2i(dim.getLimitX() - Layout.SCROLLBAR_WIDTH, dim.y(),
                Layout.SCROLLBAR_WIDTH, dim.height()));
        this.scrollbar.setScrollbarContext(contentHeight);
        this.scrollbar.scrollTo(scroll);

        final int width = dim.width() - (this.scrollbar.canScroll() ? Layout.SCROLLBAR_WIDTH : 0);
        int offset = 0;
        for (final Group group: groups) {
            final ModOptions mod = group.mod();
            if (headers) {
                final int row = offset;
                this.add(ReeseTabButton.header(new Dim2i(dim.x(), dim.y(), width, HEADER_HEIGHT), mod,
                        () -> dim.y() + row - this.scrollbar.getScrollAmount(), () -> selectedMod == mod,
                        () -> toggleGroup.accept(mod)));
                offset += HEADER_HEIGHT;
            }
            if (headers && collapsed.contains(mod.configId())) {
                offset += GROUP_PADDING;
                continue;
            }
            for (final Page page: group.pages()) {
                final int row = offset;
                final Component label = SaltySettingsScreen.pageLabel(page);
                this.add(ReeseTabButton.page(new Dim2i(dim.x(), dim.y(), width, PAGE_HEIGHT), mod, label,
                        () -> dim.y() + row - this.scrollbar.getScrollAmount(),
                        () -> selectedMod == mod && selectedPage == page, () -> selectPage.accept(mod, page)));
                offset += PAGE_HEIGHT;
            }
            offset += GROUP_PADDING;
        }
    }

    public int scrollAmount() {
        return this.scrollbar.getScrollAmount();
    }

    public List<ReeseTabButton> buttons() {
        return List.copyOf(this.buttons);
    }

    public ScrollbarWidget scrollbar() {
        return this.scrollbar;
    }

    @Nullable
    public Tooltip tooltipAt(final int mouseX, final int mouseY) {
        if (!this.isMouseOver(mouseX, mouseY)) return null;
        for (final ReeseTabButton button: this.buttons) {
            if (button.isMouseOver(mouseX, mouseY)) return new Tooltip(button.tooltip(), button.accent());
        }
        return null;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), 0x30000000);
        graphics.enableScissor(this.getX(), this.getY(), this.getLimitX(), this.getLimitY());
        for (final ReeseTabButton button: this.buttons) button.render(graphics, mouseX, mouseY, delta);
        graphics.disableScissor();
        if (this.scrollbar.canScroll()) this.scrollbar.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;
        return this.scrollbar.canScroll() && this.scrollbar.mouseClicked(mouseX, mouseY, button)
                || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        return this.scrollbar.canScroll() && this.scrollbar.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
                || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        return this.scrollbar.canScroll() && this.scrollbar.mouseReleased(mouseX, mouseY, button)
                || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double horizontal, final double vertical) {
        if (!this.isMouseOver(mouseX, mouseY) || !this.scrollbar.canScroll()) return false;
        this.scrollbar.scroll((int) (-vertical * PAGE_HEIGHT * 2));
        return true;
    }

    private void add(final ReeseTabButton button) {
        this.buttons.add(button);
        this.addRenderableChild(button);
    }

    private static int contentHeight(final List<Group> groups, final Set<String> collapsed, final boolean headers) {
        int height = 0;
        for (final Group group: groups) {
            if (headers) height += HEADER_HEIGHT;
            if (!headers || !collapsed.contains(group.mod().configId())) height += group.pages().size() * PAGE_HEIGHT;
            height += GROUP_PADDING;
        }
        return Math.max(0, height - GROUP_PADDING);
    }

    public record Group(ModOptions mod, List<Page> pages) {}

    public record Tooltip(Component text, int accent) {}
}
