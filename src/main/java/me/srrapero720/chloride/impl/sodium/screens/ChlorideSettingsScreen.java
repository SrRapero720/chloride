package me.srrapero720.chloride.impl.sodium.screens;

import me.srrapero720.chloride.impl.SettingsScreens;
import me.srrapero720.chloride.impl.sodium.controls.ChlorideTabButton;
import me.srrapero720.chloride.impl.sodium.controls.TabButton;
import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.config.structure.Page;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Chloride's default Sodium-like video settings screen. */
public class ChlorideSettingsScreen extends SaltySettingsScreen {
    private final List<ChlorideTabButton> tabs = new ArrayList<>();
    private final List<TabRow> modRows = new ArrayList<>();
    private final List<TabRow> pageRows = new ArrayList<>();
    private int modRibbonBottom = -1;

    public ChlorideSettingsScreen(final Screen prev) {
        super(Component.translatable("chloride.settings.chloride"), SettingsScreens.Style.CHLORIDE, prev);
    }

    public List<ChlorideTabButton> tabs() {
        return List.copyOf(this.tabs);
    }

    @Override
    protected void initContent(final Dim2i area) {
        this.tabs.clear();
        this.pageRows.clear();
        this.modRibbonBottom = -1;

        int top = area.y();
        if (!this.searching()) {
            top += this.addModTabs(area.x(), top, area.width());
            this.modRibbonBottom = top;
            top += this.addPageTabs(area.x(), top, area.width());
        }

        this.addSettingsList(new Dim2i(area.x(), top, area.width(), Math.max(0, area.getLimitY() - top)));
    }

    @Override
    protected void renderLayout(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        if (this.modRibbonBottom > 0) {
            graphics.fill(this.getX(), this.modRibbonBottom - 1, this.getLimitX(), this.modRibbonBottom, this.accent());
        }
    }

    @Nullable
    @Override
    protected Tip layoutTooltipAt(final int mouseX, final int mouseY) {
        return this.tabTooltipAt(this.tabs, mouseX, mouseY);
    }

    private int addModTabs(final int x, final int y, final int width) {
        final ModRibbon layout = this.modRibbon(width);
        final int rows = layout.rows().size();
        int activeRow = 0;
        for (int row = 0; row < rows; row++) {
            for (final ModOptions mod: layout.rows().get(row)) {
                if (mod == this.mod()) {
                    activeRow = row;
                    break;
                }
            }
        }

        // ROWS SURVIVE A RELAYOUT SO THE SWAP ANIMATES; A DIFFERENT ROW COUNT STARTS THEM ALREADY IN PLACE
        final boolean reused = this.modRows.size() == rows;
        if (!reused) {
            this.modRows.clear();
            for (int row = 0; row < rows; row++) {
                this.modRows.add(new TabRow(row == activeRow ? rows - 1 : row == rows - 1 ? activeRow : row));
            }
        } else {
            pullDown(this.modRows, this.modRows.get(activeRow));
        }

        for (int row = 0; row < rows; row++) {
            final List<ModOptions> entries = layout.rows().get(row);
            final TabRow tabRow = this.modRows.get(row);

            int desired = 0;
            for (final ModOptions mod: entries) desired += TabButton.modWidth(this.font, mod, layout.detail());
            final int extra = Math.max(0, width - desired);
            int tabX = x;
            for (int column = 0; column < entries.size(); column++) {
                final ModOptions mod = entries.get(column);
                final int tabWidth = TabButton.modWidth(this.font, mod, layout.detail()) + extra / entries.size()
                        + (column < extra % entries.size() ? 1 : 0);
                this.tabs.add(this.addRenderableWidget(ChlorideTabButton.mod(new Dim2i(tabX, y, tabWidth, Layout.BUTTON_SHORT), mod, layout.detail(),
                        () -> y + Math.round(tabRow.position() * Layout.BUTTON_SHORT), () -> this.mod() == mod, () -> this.selectMod(mod))));
                tabX += tabWidth;
            }
        }
        return rows * Layout.BUTTON_SHORT;
    }

    private int addPageTabs(final int x, final int y, final int width) {
        final List<Page> pages = this.mod().pages();
        final int columns = Math.clamp(pages.size(), 1, width / Layout.BUTTON_LONG);
        final int rows = (pages.size() + columns - 1) / columns;
        final int[] counts = new int[rows];
        for (int row = 0, placed = 0; row < rows; row++) {
            counts[row] = (pages.size() - placed + rows - row - 1) / (rows - row);
            placed += counts[row];
        }

        int selectedRow = 0;
        for (int row = 0, placed = 0; row < rows; row++) {
            for (int column = 0; column < counts[row]; column++) {
                if (pages.get(placed + column) == this.page()) selectedRow = row;
            }
            placed += counts[row];
        }

        for (int row = 0, placed = 0; row < rows; row++) {
            final int slot = row == selectedRow ? rows - 1 : row == rows - 1 ? selectedRow : row;
            final TabRow tabRow = new TabRow(slot);
            this.pageRows.add(tabRow);
            int pageX = x;
            for (int column = 0; column < counts[row]; column++) {
                final Page page = pages.get(placed++);
                final Component label = pageLabel(page);
                final int tabWidth = width / counts[row] + (column < width % counts[row] ? 1 : 0);
                this.tabs.add(this.addRenderableWidget(ChlorideTabButton.page(new Dim2i(pageX, y, tabWidth, Layout.BUTTON_SHORT), label, this.mod().theme(),
                        () -> y + Math.round(tabRow.position() * Layout.BUTTON_SHORT), () -> this.page() == page, () -> this.selectPage(page, tabRow))));
                pageX += tabWidth;
            }
        }
        return rows * Layout.BUTTON_SHORT;
    }

    private void selectMod(final ModOptions mod) {
        if (!this.selectPage(mod, this.firstPage(mod))) return;
        this.clearSearch();
        this.requestRelayout();
    }

    private void selectPage(final Page page, final TabRow row) {
        if (!this.selectPage(this.mod(), page)) return;
        pullDown(this.pageRows, row);
        if (this.searching()) this.clearSearch();
        else this.refresh();
    }

    private ModRibbon modRibbon(final int width) {
        for (int rows = 1; rows <= this.mods().size(); rows++) {
            for (final TabButton.Detail detail: TabButton.Detail.values()) {
                final List<List<ModOptions>> layout = this.packMods(width, rows,
                        mod -> TabButton.modWidth(this.font, mod, detail));
                if (layout != null) return new ModRibbon(detail, layout);
            }
        }
        return new ModRibbon(TabButton.Detail.ICON, this.splitMods(this.mods().size()));
    }

    private static void pullDown(final List<TabRow> rows, final TabRow row) {
        final int last = rows.size() - 1;
        if (row.slot == last) return;
        for (final TabRow other: rows) {
            if (other.slot == last) {
                other.slideTo(row.slot);
                break;
            }
        }
        row.slideTo(last);
    }

    private record ModRibbon(TabButton.Detail detail, List<List<ModOptions>> rows) {}

    private static final class TabRow {
        private static final float SLIDE_MILLIS = 250.0f;

        private int slot;
        private float from;
        private long start;

        private TabRow(final int slot) {
            this.slot = slot;
            this.from = slot;
        }

        private void slideTo(final int slot) {
            this.from = this.position();
            this.slot = slot;
            this.start = Util.getMillis();
        }

        private float position() {
            final float time = Math.min(1.0f, (Util.getMillis() - this.start) / SLIDE_MILLIS);
            final float eased = time < 0.5f ? 2.0f * time * time : 1.0f - 2.0f * (1.0f - time) * (1.0f - time);
            return this.from + (this.slot - this.from) * eased;
        }
    }
}
