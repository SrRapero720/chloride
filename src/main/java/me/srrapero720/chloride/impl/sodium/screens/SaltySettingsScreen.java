package me.srrapero720.chloride.impl.sodium.screens;

import com.mojang.blaze3d.platform.InputConstants;
import me.srrapero720.chloride.impl.SettingsScreens;
import me.srrapero720.chloride.impl.sodium.controls.*;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOptions;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.config.structure.*;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Dimensioned;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.options.control.ExternalButtonControl;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.KeyBoundButtonWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.ToIntFunction;

/**
 * Creates the shared shell and modern option list, owns their interactions and config lifecycle, and exposes
 * the neutral mod/page model from which subclasses build isolated navigation designs.
 */
public abstract class SaltySettingsScreen extends Screen implements Dimensioned {
    protected static final int CONTENT_MIN_WIDTH = 505;
    protected static final int CONTENT_MAX_WIDTH = 555;
    protected static final int FRAME_MARGIN = Layout.CONTENT_BORDER_HEIGHT / 2;
    private static final int TOOLTIP_PADDING = 4;
    private static final int TOOLTIP_CURSOR_GAP = 12;

    private final SettingsScreens.Style style;
    private final Screen prev;
    private final List<ModOptions> mods;
    /** Identifier list options currently showing their entries. */
    private final Set<ResourceLocation> expanded = new HashSet<>();

    private Dim2i dim = new Dim2i(0, 0, 0, 0);
    /** Floating choice list of the open dropdown, drawn over the body and outside the layout. */
    private DropdownOverlay dropdown;
    private ModOptions mod;
    private Page page;
    private String query = "";
    private List<SearchHit> hits = List.of();
    private SettingsView list;
    private EnhancedSearchBar bar;
    private KeyBoundButtonWidget undo;
    private KeyBoundButtonWidget apply;
    private KeyBoundButtonWidget done;
    private List<KeyBoundButtonWidget> shortcuts = List.of();
    private boolean relayout;
    private boolean pending;
    private boolean peeking;

    protected SaltySettingsScreen(final Component title, final SettingsScreens.Style style, final Screen prev) {
        super(title);
        this.style = style;
        this.prev = prev;
        this.mods = ConfigManager.CONFIG.getModOptions().stream().filter(mod -> !mod.pages().isEmpty()).toList();
        // SODIUM ALWAYS LEADS THE LIST AND IS WHAT PEOPLE COME LOOKING FOR, SO ITS FOLDER OPENS FIRST
        this.mod = this.mods.getFirst();
        this.page = firstPageOf(this.mod);

        // SODIUM DOES THE SAME BEFORE SHOWING ITS SCREEN: PULL EVERY OPTION BACK FROM ITS BINDING
        ConfigManager.CONFIG.resetAllOptionsFromBindings();
        SaltyOptions.undoAll();
    }

    // EXTERNAL PAGES HAVE NOTHING TO SHOW INLINE, SO A MOD OPENS ON ITS FIRST PAGE WITH ACTUAL GROUPS
    private static Page firstPageOf(final ModOptions mod) {
        for (final Page page: mod.pages()) {
            if (!(page instanceof ExternalPage)) return page;
        }
        return mod.pages().getFirst();
    }

    @Override
    protected final void init() {
        ConfigManager.CONFIG.invalidateGlobalRebuildDependents();
        this.list = null;
        this.bar = null;
        this.undo = null;
        this.apply = null;
        this.done = null;
        this.shortcuts = List.of();

        this.initDimensions();
        final int contentY = this.addHeader();
        final int footerY = this.addFooter();
        this.initContent(new Dim2i(this.getX(), contentY, this.getWidth(), Math.max(0, footerY - Layout.INNER_MARGIN - contentY)));
        if (this.list != null) this.refresh();
    }

    /** Applies the shared centered dimensions. Styles may override this when their frame genuinely differs. */
    protected void initDimensions() {
        this.centeredDimensions(CONTENT_MIN_WIDTH, CONTENT_MAX_WIDTH, FRAME_MARGIN);
    }

    /** Places the style-specific content between the shared header and footer. */
    protected abstract void initContent(Dim2i area);

    @Override
    public final void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        if (this.relayout) {
            // SEARCH AND STYLE-SPECIFIC NAVIGATION MAY CHANGE THE BODY GEOMETRY, SO RELAYOUT HAPPENS OUTSIDE ITS EVENT
            this.relayout = false;
            // THE BAR IS RECREATED BY THE RELAYOUT, SO THE FOCUS IS HANDED BACK AND TYPING KEEPS GOING
            final boolean typing = this.searchFocused();
            this.rebuildWidgets();
            if (typing) this.focusSearch();
        }
        // HIDE UI IS HELD DOWN: DRAW NOTHING AT ALL, NOT EVEN THE DIMMED BACKGROUND
        if (this.peeking) return;

        this.pending = this.hasPendingChanges();
        this.updateActionState(this.pending);

        this.renderBackgroundLayout(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderLayout(graphics, mouseX, mouseY, delta);
        if (this.dropdown != null) this.dropdown.render(graphics, mouseX, mouseY, delta);

        // WHATEVER THE FLOATING LIST COVERS HAS NO BUSINESS EXPLAINING ITSELF THROUGH A TOOLTIP
        if (this.dropdown != null && this.dropdown.isMouseOver(mouseX, mouseY)) return;
        final Tip tooltip = this.tooltipAt(mouseX, mouseY);
        if (tooltip != null) this.drawTooltip(graphics, tooltip, mouseX, mouseY);
    }

    protected void renderBackgroundLayout(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {}

    protected void renderLayout(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {}

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        // ESCAPE DISMISSES THE FLOATING LIST BEFORE IT GETS ANYWHERE NEAR CLOSING THE SCREEN
        if (keyCode == InputConstants.KEY_ESCAPE && this.dropdown != null) {
            this.dropdown = null;
            return true;
        }
        // THE BAR EATS ESCAPE WHILE IT HOLDS A QUERY, SO CLEARING THE SEARCH DOES NOT CLOSE THE SCREEN
        return this.handleSearchKey(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (this.dropdown == null) return super.mouseClicked(mouseX, mouseY, button);
        // PICKING A CHOICE OR CLICKING ANYWHERE ELSE BOTH DISMISS THE LIST, AND NEITHER REACHES THE BODY
        if (!this.dropdown.mouseClicked(mouseX, mouseY, button)) this.dropdown = null;
        return true;
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        if (this.dropdown != null) return this.dropdown.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double horizontal, final double vertical) {
        // THE LIST SCROLLS ON ITS OWN; SCROLLING ANYWHERE ELSE DISMISSES IT, SINCE IT IS PINNED TO ITS ROW
        if (this.dropdown != null && this.dropdown.mouseScrolled(mouseX, mouseY, horizontal, vertical)) return true;
        this.dropdown = null;
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyReleased(final int keyCode, final int scanCode, final int modifiers) {
        for (final KeyBoundButtonWidget button: this.shortcuts) {
            if (button.tryActivateShortcut(keyCode, modifiers)) return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        this.peeking = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onFilesDrop(final List<Path> paths) {
        // WHATEVER IS ON SHOW DECIDES WHAT TO DO WITH THE FILES; THE LIST IS REBUILT AROUND THE RESULT
        if (this.list != null && this.list.dropFiles(paths)) this.refresh();
    }

    @Override
    public void onClose() {
        // NOTHING IS COMMITTED UNLESS APPLY WAS PRESSED, SO LEAVING ALWAYS DROPS WHATEVER WAS PENDING
        this.undo();
        this.minecraft.setScreen(this.prev);
    }

    @Override
    public Dim2i getDimensions() {
        return this.dim;
    }

    public int accent() {
        return this.mod.theme().theme;
    }

    public final SettingsScreens.Style style() {
        return this.style;
    }

    public final Screen previousScreen() {
        return this.prev;
    }

    @Nullable
    public final DropdownOverlay dropdown() {
        return this.dropdown;
    }

    @Nullable
    public final SettingsList settingsList() {
        return this.list instanceof SettingsList value ? value : null;
    }

    @Nullable
    public final EnhancedSearchBar searchBar() {
        return this.bar;
    }

    @Nullable
    public final KeyBoundButtonWidget undoButton() {
        return this.undo;
    }

    @Nullable
    public final KeyBoundButtonWidget applyButton() {
        return this.apply;
    }

    @Nullable
    public final KeyBoundButtonWidget doneButton() {
        return this.done;
    }

    public boolean expanded(final ResourceLocation id) {
        return this.expanded.contains(id);
    }

    public final Set<ResourceLocation> expandedOptions() {
        return Set.copyOf(this.expanded);
    }

    public boolean dropdownOpen(final ResourceLocation id) {
        return this.dropdown != null && this.dropdown.id().equals(id);
    }

    /** Opens the floating choice list of {@code row}, or closes it when that row already owns it. */
    public void openDropdown(final OptionRows.Row row, final ResourceLocation id, final OptionRows.Choices choices) {
        this.dropdown = this.dropdownOpen(id) ? null : new DropdownOverlay(row, id, choices, this.dim, this.mod.theme());
    }

    public void toggleExpanded(final ResourceLocation id) {
        if (!this.expanded.remove(id)) this.expanded.add(id);
        this.refresh();
    }

    public final List<ModOptions> mods() {
        return this.mods;
    }

    public final ModOptions mod() {
        return this.mod;
    }

    public final Page page() {
        return this.page;
    }

    public final boolean searching() {
        return !this.query.isEmpty();
    }

    public final String query() {
        return this.query;
    }

    public final List<SearchHit> searchHits() {
        return this.hits;
    }

    public final boolean hasPendingChanges() {
        return ConfigManager.CONFIG.anyOptionChanged() || SaltyOptions.anyChanged();
    }

    protected final void centeredDimensions(final int minWidth, final int maxWidth, final int margin) {
        final int start = minWidth + Layout.CONTENT_BORDER_MIN_WIDTH;
        final int end = maxWidth + Layout.CONTENT_BORDER_MIN_WIDTH;
        int contentWidth = this.width;
        contentWidth = this.width > start // IF
                ? this.width < end // USE: IF
                    ? minWidth + (this.width - start) * (maxWidth - minWidth) / (end - start) // USE
                    : maxWidth // ELSE
                : Math.min(contentWidth, this.width - margin * 2); // ELSE

        final boolean tall = this.height > Layout.CONTENT_MIN_HEIGHT + Layout.CONTENT_BORDER_HEIGHT;
        final int contentHeight = this.height - (tall ? Layout.CONTENT_BORDER_HEIGHT : margin * 2);
        this.dimensions(new Dim2i((this.width - contentWidth) / 2, (this.height - contentHeight) / 2, contentWidth, contentHeight));
    }

    protected final void dimensions(final Dim2i dim) {
        this.dim = dim;
    }

    // THE COMMON FRAME OWNS HEADER AND FOOTER PLACEMENT; STYLES ONLY RECEIVE THE SPACE BETWEEN THEM
    protected int addHeader() {
        final int x = this.getX();
        final int y = this.getY();
        this.addStyleSwitch(new Dim2i(x, y, Layout.BUTTON_SHORT, Layout.BUTTON_SHORT));
        final int barX = x + Layout.BUTTON_SHORT + Layout.INNER_MARGIN;
        this.addSearchBar(new Dim2i(barX, y, this.getLimitX() - barX, Layout.BUTTON_SHORT));
        return y + Layout.BUTTON_SHORT + Layout.INNER_MARGIN;
    }

    private void addStyleSwitch(final Dim2i dim) {
        this.addRenderableWidget(new StyleSwitchButton(dim, this.style, this.prev));
    }

    private void addSearchBar(final Dim2i dim) {
        this.bar = this.addRenderableWidget(new EnhancedSearchBar(dim, this.query, this::onQuery));
    }

    protected int addFooter() {
        final int footerY = this.getLimitY() - Layout.BUTTON_SHORT;
        final int step = Layout.BUTTON_LONG + Layout.INNER_MARGIN;
        final int doneX = this.getLimitX() - Layout.BUTTON_LONG;
        this.createFooter(
                new Dim2i(doneX - step * 2, footerY, Layout.BUTTON_LONG, Layout.BUTTON_SHORT),
                new Dim2i(doneX - step, footerY, Layout.BUTTON_LONG, Layout.BUTTON_SHORT),
                new Dim2i(doneX, footerY, Layout.BUTTON_LONG, Layout.BUTTON_SHORT));
        // HOLD TO PEEK AT THE WORLD BEHIND; POINTLESS OUTSIDE A GAME, WHERE THERE IS NOTHING TO LOOK AT
        this.addHideButton(new Dim2i(this.getX(), footerY, Layout.BUTTON_LONG, Layout.BUTTON_SHORT));
        return footerY;
    }

    private void createFooter(final Dim2i undo, final Dim2i apply, final Dim2i done) {
        this.done = this.addRenderableWidget(new KeyBoundButtonWidget(done, CommonComponents.GUI_DONE, this::onClose, true, false, InputConstants.KEY_D));
        this.apply = this.addRenderableWidget(new KeyBoundButtonWidget(apply, Component.translatable("sodium.options.buttons.apply"), this::apply, true, false, InputConstants.KEY_A));
        this.undo = this.addRenderableWidget(new KeyBoundButtonWidget(undo, Component.translatable("sodium.options.buttons.undo"), this::undo, true, false, InputConstants.KEY_U));
        this.shortcuts = List.of(this.done, this.apply, this.undo);
    }

    private void addHideButton(final Dim2i dim) {
        if (this.minecraft.level != null) {
            this.addRenderableWidget(new FlatButtonWidget(dim, Component.translatable("chloride.settings.hide_ui"), () -> this.peeking = true, true, false));
        }
    }

    protected final void addSettingsList(final Dim2i dim) {
        final SettingsList list = this.addRenderableWidget(new SettingsList(this, dim));
        this.setSettingsView(list);
    }

    /** Registers the option body used by a style after that body has been added as a screen widget. */
    protected final void setSettingsView(final SettingsView view) {
        this.list = view;
    }

    protected final void showPage(final ModOptions mod, final Page page) {
        if (this.list != null) this.list.showPage(mod, page);
    }

    protected final void showSearchResults() {
        if (this.list != null) this.list.showResults(this.searchSections());
    }

    protected final void showPageSearchResults(final ModOptions mod, final Page page) {
        if (this.list != null) this.list.showPageResults(mod, page, this.searchOptions(page));
    }

    public final void refresh() {
        if (this.list == null) return;
        this.dropdown = null;
        this.refreshContent();
    }

    protected void refreshContent() {
        if (this.searching()) this.showSearchResults();
        else this.showPage(this.mod, this.page);
    }

    protected final List<Option> searchOptions(final Page page) {
        return this.hits.stream().filter(hit -> hit.page() == page).map(SearchHit::option).toList();
    }

    protected final boolean hasSearchResults(final Page page) {
        for (final SearchHit hit: this.hits) {
            if (hit.page() == page) return true;
        }
        return false;
    }

    /** Selects an internal page, or opens an external page without changing the current selection. */
    protected final boolean selectPage(final ModOptions mod, final Page page) {
        if (page instanceof ExternalPage external) {
            external.currentScreenConsumer().accept(this);
            return false;
        }
        if (this.mod == mod && this.page == page) return false;
        this.mod = mod;
        this.page = page;
        return true;
    }

    protected final Page firstPage(final ModOptions mod) {
        return firstPageOf(mod);
    }

    protected final void clearSearch() {
        if (this.bar != null) this.bar.clear();
    }

    protected final void requestRelayout() {
        this.relayout = true;
    }

    /** Hides the complete settings UI until the current mouse press is released. */
    protected final void startPeek() {
        this.peeking = true;
    }

    /** Packs mods in encounter order without allowing any row to exceed {@code width}. */
    @Nullable
    protected final List<List<ModOptions>> packMods(final int width, final int rows, final ToIntFunction<ModOptions> widthOf) {
        final List<List<ModOptions>> packed = new ArrayList<>();
        List<ModOptions> row = new ArrayList<>();
        int used = 0;
        for (final ModOptions mod: this.mods) {
            final int tabWidth = widthOf.applyAsInt(mod);
            if (tabWidth > width) return null;
            if (!row.isEmpty() && used + tabWidth > width) {
                packed.add(row);
                row = new ArrayList<>();
                used = 0;
            }
            row.add(mod);
            used += tabWidth;
        }
        packed.add(row);
        return packed.size() <= rows ? packed : null;
    }

    /** Splits mods as evenly as possible across at most {@code rows} rows. */
    protected final List<List<ModOptions>> splitMods(final int rows) {
        final int count = Math.min(Math.max(1, rows), this.mods.size());
        final List<List<ModOptions>> split = new ArrayList<>(count);
        int start = 0;
        for (int row = 0; row < count; row++) {
            final int size = Math.ceilDiv(this.mods.size() - start, count - row);
            split.add(this.mods.subList(start, start + size));
            start += size;
        }
        return split;
    }

    public static Component pageLabel(final Page page) {
        return page instanceof ExternalPage
                ? Component.literal(ExternalButtonControl.EXTERNAL_PAGE_PREFIX).append(page.name())
                : page.name();
    }

    @Nullable
    protected final Tip tabTooltipAt(final List<? extends TabButton> tabs, final int mouseX, final int mouseY) {
        for (final TabButton tab: tabs) {
            if (!tab.isMouseOver(mouseX, mouseY)) continue;
            final Component text = tab.tooltip();
            return text == null ? null : new Tip(text, tab.accent());
        }
        return null;
    }

    @Nullable
    protected Tip layoutTooltipAt(final int mouseX, final int mouseY) {
        return null;
    }

    private List<Section> searchSections() {
        final List<Section> sections = new ArrayList<>();
        for (final ModOptions mod: this.mods) {
            final List<Option> options = this.hits.stream().filter(hit -> hit.mod() == mod).map(SearchHit::option)
                    .sorted(Comparator.comparing(option -> option.getName().getString(), String.CASE_INSENSITIVE_ORDER)).toList();
            if (!options.isEmpty()) sections.add(new Section(mod, options));
        }
        return sections;
    }

    // MATCHES ALL QUERY WORDS AND KEEPS THE OWNING MOD AND PAGE FOR STYLE-SPECIFIC NAVIGATION
    private List<SearchHit> search(final String query) {
        final String[] words = query.trim().toLowerCase(Locale.ROOT).split(" +");
        final List<SearchHit> hits = new ArrayList<>();
        for (final ModOptions mod: this.mods) {
            for (final Page page: mod.pages()) {
                for (final OptionGroup group: page.groups()) {
                    for (final Option option: group.options()) {
                        final String text = (option.getName().getString() + " " + option.getTooltip().getString()).toLowerCase(Locale.ROOT);
                        if (matches(text, words)) {
                            hits.add(new SearchHit(mod, page, option));
                        }
                    }
                }
            }
        }
        return hits;
    }

    private static boolean matches(final String name, final String[] words) {
        for (final String word: words) {
            if (!name.contains(word)) return false;
        }
        return true;
    }

    protected final void onQuery(final String query) {
        if (query.equals(this.query)) return;
        this.query = query;
        this.hits = query.isBlank() ? List.of() : this.search(query);
        // EVERY STYLE DECIDES HOW SEARCH AFFECTS ITS NAVIGATION, SO REBUILD ITS ISOLATED CONTENT ON EACH CHANGE
        this.relayout = true;
    }

    public final void apply() {
        SaltyOptions.applyAll();
        ConfigManager.CONFIG.applyAllOptions();
    }

    public final void undo() {
        ConfigManager.CONFIG.resetAllOptionsFromBindings();
        SaltyOptions.undoAll();
        this.refresh();
    }

    /** Updates the default Sodium-like footer buttons. */
    protected void updateActionState(final boolean pending) {
        if (this.apply != null) this.apply.setEnabled(pending);
        if (this.undo != null) this.undo.setVisible(pending);
        if (this.done != null) this.done.setEnabled(!pending);
    }

    /** True while the active search control should regain focus after a relayout. */
    protected boolean searchFocused() {
        return this.bar != null && this.bar.typing();
    }

    /** Restores focus to the style's search control after its widgets are rebuilt. */
    protected void focusSearch() {
        if (this.bar != null) this.setFocused(this.bar);
    }

    /** Gives a style's search control first chance to consume a key. */
    protected boolean handleSearchKey(final int keyCode, final int scanCode, final int modifiers) {
        return this.bar != null && this.bar.keyPressed(keyCode, scanCode, modifiers);
    }

    @Nullable
    private Tip tooltipAt(final int mouseX, final int mouseY) {
        final Tip layout = this.layoutTooltipAt(mouseX, mouseY);
        if (layout != null) return layout;
        return this.list == null ? null : this.list.tooltipAt(mouseX, mouseY);
    }

    private void drawTooltip(final GuiGraphics graphics, final Tip tip, final int mouseX, final int mouseY) {
        final List<FormattedCharSequence> lines = this.font.split(tip.text(), Math.min(Layout.MAX_TOOLTIP_WIDTH, this.getWidth() - Layout.TOOLTIP_OUTER_MARGIN * 2));
        final int lineHeight = this.font.lineHeight + Layout.TEXT_LINE_SPACING;
        int width = 0;
        for (final FormattedCharSequence line: lines) width = Math.max(width, this.font.width(line));

        final int boxWidth = width + TOOLTIP_PADDING * 2;
        final int boxHeight = lines.size() * lineHeight - Layout.TEXT_LINE_SPACING + TOOLTIP_PADDING * 2;
        // THE BOX FOLLOWS THE CURSOR AND FLIPS TO ITS OTHER SIDE WHENEVER IT WOULD LEAVE THE FRAME
        int boxX = mouseX + TOOLTIP_CURSOR_GAP;
        if (boxX + boxWidth > this.getLimitX() - Layout.TOOLTIP_OUTER_MARGIN) boxX = mouseX - TOOLTIP_CURSOR_GAP - boxWidth;
        boxX = Math.max(this.getX() + Layout.TOOLTIP_OUTER_MARGIN, boxX);
        int boxY = mouseY + TOOLTIP_CURSOR_GAP;
        if (boxY + boxHeight > this.getLimitY() - Layout.TOOLTIP_OUTER_MARGIN) boxY = mouseY - TOOLTIP_PADDING - boxHeight;
        boxY = Math.max(this.getY() + Layout.TOOLTIP_OUTER_MARGIN, boxY);

        graphics.pose().pushPose();
        // SAME LIFT SODIUM'S TOOLTIPS USE SO THE BOX DRAWS OVER EVERY WIDGET
        graphics.pose().translate(0.0f, 0.0f, 400.0f);
        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, Colors.BACKGROUND_OVERLAY);
        // ACCENT BAR ALONG THE BOTTOM CONNECTS THE TOOLTIP TO ITS SOURCE THEME
        graphics.fill(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, tip.accent());
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(this.font, lines.get(i), boxX + TOOLTIP_PADDING, boxY + TOOLTIP_PADDING + i * lineHeight, Colors.FOREGROUND);
        }
        graphics.pose().popPose();
    }

    /** Tooltip text plus the accent of whatever it belongs to. */
    public record Tip(Component text, int accent) {}

    /** Search hits of a single mod, already sorted. */
    public record Section(ModOptions mod, List<Option> options) {}

    /** A matching option with both navigation owners retained. */
    public record SearchHit(ModOptions mod, Page page, Option option) {}
}
