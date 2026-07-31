package me.srrapero720.chloride.impl.sodium.screens;

import me.srrapero720.chloride.impl.SettingsScreens;
import me.srrapero720.chloride.impl.sodium.controls.ReeseTabRail;
import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.config.structure.Page;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reese-inspired settings design with grouped vertical mod and page navigation. */
public class ReeseSettingsScreen extends SaltySettingsScreen {
    private static final int RAIL_MIN_WIDTH = 120;
    private static final int RAIL_MAX_WIDTH = 220;
    private static final double RAIL_MAX_RATIO = 0.35;

    private final Set<String> collapsed = new HashSet<>();
    private ReeseTabRail rail;
    private int railScroll;

    public ReeseSettingsScreen(final Screen prev) {
        super(Component.translatable("chloride.settings.reese"), SettingsScreens.Style.TEXTRUE, prev);
    }

    public Set<String> collapsedGroups() {
        return Set.copyOf(this.collapsed);
    }

    @Nullable
    public ReeseTabRail rail() {
        return this.rail;
    }

    @Override
    protected void initContent(final Dim2i area) {
        if (this.rail != null) this.railScroll = this.rail.scrollAmount();
        final List<ReeseTabRail.Group> groups = this.visibleGroups();
        if (this.searching() && !this.hasSearchResults(this.page()) && !groups.isEmpty()) {
            final ReeseTabRail.Group first = groups.getFirst();
            this.selectPage(first.mod(), first.pages().getFirst());
            this.railScroll = 0;
        }

        final int railWidth = this.railWidth(area, groups);
        final Set<String> collapsed = this.searching() ? Set.of() : this.collapsed;
        this.rail = this.addRenderableWidget(new ReeseTabRail(new Dim2i(area.x(), area.y(), railWidth, area.height()),
                groups, collapsed, this.mod(), this.page(), this.railScroll, this::toggleGroup, this::onPageSelected));

        final int listX = area.x() + railWidth + Layout.INNER_MARGIN;
        this.addSettingsList(new Dim2i(listX, area.y(), Math.max(0, area.getLimitX() - listX), area.height()));
    }

    @Override
    protected void refreshContent() {
        if (this.searching()) this.showPageSearchResults(this.mod(), this.page());
        else this.showPage(this.mod(), this.page());
    }

    @Nullable
    @Override
    protected Tip layoutTooltipAt(final int mouseX, final int mouseY) {
        if (this.rail == null) return null;
        final ReeseTabRail.Tooltip tooltip = this.rail.tooltipAt(mouseX, mouseY);
        return tooltip == null ? null : new Tip(tooltip.text(), tooltip.accent());
    }

    private List<ReeseTabRail.Group> visibleGroups() {
        final List<ReeseTabRail.Group> groups = new ArrayList<>();
        for (final ModOptions mod: this.mods()) {
            final List<Page> pages = this.searching()
                    ? mod.pages().stream().filter(this::hasSearchResults).toList()
                    : mod.pages();
            if (!pages.isEmpty()) groups.add(new ReeseTabRail.Group(mod, pages));
        }
        return groups;
    }

    private int railWidth(final Dim2i area, final List<ReeseTabRail.Group> groups) {
        int textWidth = 0;
        for (final ReeseTabRail.Group group: groups) {
            textWidth = Math.max(textWidth, this.font.width(group.mod().name()));
            textWidth = Math.max(textWidth, this.font.width(group.mod().version()));
            for (final Page page: group.pages()) textWidth = Math.max(textWidth, this.font.width(page.name()));
        }

        final int maximum = Math.max(1, Math.min(RAIL_MAX_WIDTH, (int) (area.width() * RAIL_MAX_RATIO)));
        final int minimum = Math.min(RAIL_MIN_WIDTH, maximum);
        return Math.clamp(textWidth + 24, minimum, maximum);
    }

    private void toggleGroup(final ModOptions mod) {
        if (this.searching()) {
            for (final Page page: mod.pages()) {
                if (this.hasSearchResults(page)) {
                    this.onPageSelected(mod, page);
                    break;
                }
            }
            return;
        }
        if (!this.collapsed.remove(mod.configId())) this.collapsed.add(mod.configId());
        this.requestRelayout();
    }

    private void onPageSelected(final ModOptions mod, final Page page) {
        if (this.selectPage(mod, page)) this.requestRelayout();
    }
}
