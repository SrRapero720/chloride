package me.srrapero720.chloride.impl.sodium.screens;

import me.srrapero720.chloride.impl.SettingsScreens;
import me.srrapero720.chloride.impl.sodium.controls.PopuliTabButton;
import me.srrapero720.chloride.impl.sodium.controls.TabButton;
import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.config.structure.Page;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Game-menu settings design with horizontal mod categories and vertical page navigation. */
public class PopuliSettingsScreen extends SaltySettingsScreen {
    private static final int PAGE_RAIL_WIDTH = 120;

    private final List<PopuliTabButton> tabs = new ArrayList<>();

    public PopuliSettingsScreen(final Screen prev) {
        super(Component.translatable("chloride.settings.populi"), SettingsScreens.Style.POPULI, prev);
    }

    public List<PopuliTabButton> tabs() {
        return List.copyOf(this.tabs);
    }

    @Override
    protected void initContent(final Dim2i area) {
        this.tabs.clear();
        if (this.searching()) {
            this.addSettingsList(area);
            return;
        }

        final int top = this.addModBar(area);
        final int rows = Math.max(1, (area.getLimitY() - top) / Layout.BUTTON_SHORT);
        final int columns = Math.max(1, Math.ceilDiv(this.mod().pages().size(), rows));
        final int railWidth = Math.clamp(area.width() / 2, 1, PAGE_RAIL_WIDTH * columns);
        this.addPageRail(new Dim2i(area.x(), top, railWidth, Math.max(0, area.getLimitY() - top)));
        final int listX = area.x() + railWidth + Layout.INNER_MARGIN;
        this.addSettingsList(new Dim2i(listX, top, Math.max(0, area.getLimitX() - listX), Math.max(0, area.getLimitY() - top)));
    }

    @Nullable
    @Override
    protected Tip layoutTooltipAt(final int mouseX, final int mouseY) {
        return this.tabTooltipAt(this.tabs, mouseX, mouseY);
    }

    private int addModBar(final Dim2i area) {
        final ModBar layout = this.modBar(area.width());
        for (int row = 0; row < layout.rows().size(); row++) {
            final List<ModOptions> entries = layout.rows().get(row);
            int desired = 0;
            for (final ModOptions mod: entries) desired += TabButton.modWidth(this.font, mod, layout.detail());
            final int extra = Math.max(0, area.width() - desired);
            int x = area.x();
            for (int column = 0; column < entries.size(); column++) {
                final ModOptions mod = entries.get(column);
                final int width = TabButton.modWidth(this.font, mod, layout.detail()) + extra / entries.size()
                        + (column < extra % entries.size() ? 1 : 0);
                this.tabs.add(this.addRenderableWidget(PopuliTabButton.mod(
                        new Dim2i(x, area.y() + row * Layout.BUTTON_SHORT, width, Layout.BUTTON_SHORT), mod, layout.detail(),
                        () -> this.mod() == mod, () -> this.selectMod(mod))));
                x += width;
            }
        }
        return area.y() + layout.rows().size() * Layout.BUTTON_SHORT + Layout.INNER_MARGIN;
    }

    private void addPageRail(final Dim2i area) {
        final List<Page> pages = this.mod().pages();
        final int rows = Math.max(1, area.height() / Layout.BUTTON_SHORT);
        final int columns = Math.max(1, Math.ceilDiv(pages.size(), rows));
        final int columnWidth = area.width() / columns;
        for (int index = 0; index < pages.size(); index++) {
            final Page page = pages.get(index);
            final int column = index / rows;
            final int row = index % rows;
            final Component label = pageLabel(page);
            this.tabs.add(this.addRenderableWidget(PopuliTabButton.page(new Dim2i(area.x() + column * columnWidth,
                    area.y() + row * Layout.BUTTON_SHORT,
                    column == columns - 1 ? area.width() - column * columnWidth : columnWidth, Layout.BUTTON_SHORT),
                    this.mod(), label, () -> this.page() == page, () -> this.selectPage(page))));
        }
    }

    private ModBar modBar(final int width) {
        for (int rows = 1; rows <= 2; rows++) {
            for (final TabButton.Detail detail: TabButton.Detail.values()) {
                final List<List<ModOptions>> packed = this.packMods(width, rows,
                        mod -> TabButton.modWidth(this.font, mod, detail));
                if (packed != null) return new ModBar(detail, packed);
            }
        }
        return new ModBar(TabButton.Detail.ICON, this.splitMods(2));
    }

    private void selectMod(final ModOptions mod) {
        if (this.selectPage(mod, this.firstPage(mod))) this.requestRelayout();
    }

    private void selectPage(final Page page) {
        if (this.selectPage(this.mod(), page)) this.requestRelayout();
    }

    private record ModBar(TabButton.Detail detail, List<List<ModOptions>> rows) {}
}
