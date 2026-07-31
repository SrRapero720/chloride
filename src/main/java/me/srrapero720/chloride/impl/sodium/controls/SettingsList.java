package me.srrapero720.chloride.impl.sodium.controls;

import me.srrapero720.chloride.impl.sodium.salty.SaltyAction;
import me.srrapero720.chloride.impl.sodium.salty.SaltyEntry;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOption;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOptions;
import me.srrapero720.chloride.impl.sodium.screens.SaltySettingsScreen;
import me.srrapero720.chloride.mixins.impl.sodium.OptionAccessor;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.config.structure.BooleanOption;
import net.caffeinemc.mods.sodium.client.config.structure.EnumOption;
import net.caffeinemc.mods.sodium.client.config.structure.ExternalButtonOption;
import net.caffeinemc.mods.sodium.client.config.structure.IntegerOption;
import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.config.structure.Option;
import net.caffeinemc.mods.sodium.client.config.structure.OptionGroup;
import net.caffeinemc.mods.sodium.client.config.structure.Page;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.caffeinemc.mods.sodium.client.gui.options.control.AbstractOptionList;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.ScrollbarWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Body of {@link SaltySettingsScreen}: a single scrollable column of rows, either the groups of the
 * selected page or the hits of a search. Rows outside the viewport are skipped while rendering, so pages
 * with hundreds of entries (the per-particle toggles) cost the same as short ones.
 */
public class SettingsList extends AbstractOptionList {
    private static final Component CHEVRON = Component.literal(">");
    /** Barely there: the body box is only meant to tell the list apart from the screen behind it. */
    private static final int BODY_SHADE = 0x30000000;

    private final SaltySettingsScreen screen;
    private final Map<String, Component> modNames = new HashMap<>();
    private final List<Row> pending = new ArrayList<>();
    private final List<AbstractWidget> rows = new ArrayList<>();
    private final int rowHeight;
    private final int chevronWidth;
    private int[] tops = new int[1];
    private int[] accents = new int[0];
    private Component[] notes = new Component[0];
    private int rowWidth;
    private int accent;

    public SettingsList(final SaltySettingsScreen screen, final Dim2i dim) {
        super(dim);
        this.screen = screen;
        this.rowHeight = Layout.entryHeight(this.font);
        this.chevronWidth = Layout.OPTION_TEXT_SIDE_PADDING + this.font.width(CHEVRON);
    }

    /** Shows every group of {@code page}, separated by blank filler rows. */
    public void showPage(final ModOptions mod, final Page page) {
        this.showPage(mod, page, null);
    }

    /** Shows only the matching options of {@code page}, preserving their original groups and order. */
    public void showPageResults(final ModOptions mod, final Page page, final List<Option> options) {
        final Set<Option> matches = Collections.newSetFromMap(new IdentityHashMap<>());
        matches.addAll(options);

        this.showPage(mod, page, matches);
    }

    private void showPage(final ModOptions mod, final Page page, @Nullable final Set<Option> matches) {
        final ColorTheme theme = mod.theme();
        this.reset(theme.theme);
        for (final OptionGroup group: page.groups()) {
            final List<Option> groupOptions = matches == null ? group.options() : group.options().stream().filter(matches::contains).toList();
            if (groupOptions.isEmpty()) continue;
            if (!this.pending.isEmpty()) this.addSpacer(theme.theme);
            final Component name = group.name();
            if (name != null) {
                this.add(theme.theme, null, this.rowHeight, dim -> new Header(dim, name, Colors.FOREGROUND, Colors.BACKGROUND_MEDIUM, null, false));
            }
            for (final Option option: groupOptions) this.addOption(mod, option, theme);
        }
        this.finish();
    }

    /** Shows search hits, one section per mod in ribbon order and alphabetical inside each one. */
    public void showResults(final List<SaltySettingsScreen.Section> sections) {
        this.reset(this.screen.accent());
        for (final SaltySettingsScreen.Section section: sections) {
            final ModOptions mod = section.mod();
            final ColorTheme theme = mod.theme();
            // BLANK ROW BETWEEN MODS SO EACH SECTION READS APART FROM THE PREVIOUS ONE
            if (!this.pending.isEmpty()) this.addSpacer(theme.theme);
            this.add(theme.theme, null, this.rowHeight, dim -> new Header(dim, Component.literal(mod.name()), theme.themeLighter,
                    Colors.BACKGROUND_DARKER, mod.icon(), mod.iconMonochrome()));
            for (final Option option: section.options()) this.addOption(mod, option, theme);
        }
        this.finish();
    }

    /** Tooltip of the row under the cursor, or {@code null} when it has none. */
    @Nullable
    public SaltySettingsScreen.Tip tooltipAt(final int mouseX, final int mouseY) {
        final int index = this.rowIndexAt(mouseX, mouseY);
        if (index < 0) return null;

        final Option option = optionOf(this.rows.get(index));
        if (option == null) return null;

        // SAME EXTRA LINES SODIUM APPENDS TO ITS OWN TOOLTIPS, PLUS THE MOD THE OPTION REALLY COMES FROM
        final OptionImpact impact = option.getImpact();
        final Component note = this.notes[index];
        if (impact == null && note == null) return new SaltySettingsScreen.Tip(option.getTooltip(), this.accents[index]);

        final var text = option.getTooltip().copy();
        if (impact != null) {
            text.append("\n").append(Component.translatable("sodium.options.performance_impact_string", impact.getName()).withStyle(ChatFormatting.GRAY));
        }
        if (note != null) text.append("\n").append(note);
        return new SaltySettingsScreen.Tip(text, this.accents[index]);
    }

    public SaltySettingsScreen screen() {
        return this.screen;
    }

    public int rowHeight() {
        return this.rowHeight;
    }

    public List<AbstractWidget> rows() {
        return List.copyOf(this.rows);
    }

    public int accent() {
        return this.accent;
    }

    public ScrollbarWidget scrollbar() {
        return this.scrollbar;
    }

    /** Hands dropped files to the first option on show that takes them; false when none does. */
    public boolean dropFiles(final List<Path> paths) {
        for (final AbstractWidget row: this.rows) {
            final Option option = optionOf(row);
            if (option == null) continue;
            final SaltyEntry entry = SaltyOptions.get(idOf(option));
            if (entry != null && entry.drop(paths)) return true;
        }
        return false;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), BODY_SHADE);

        // CULLING: THE FIRST VISIBLE ROW IS LOOKED UP BY OFFSET AND THE LOOP STOPS AT THE BOTTOM OF THE VIEWPORT
        final int scroll = this.getScrollAmount();
        final int limit = scroll + this.getHeight();
        final int hovered = this.rowIndexAt(mouseX, mouseY);
        graphics.enableScissor(this.getX(), this.getY(), this.getLimitX(), this.getLimitY());
        for (int i = this.rows.isEmpty() ? 0 : this.rowAt(scroll); i < this.rows.size() && this.tops[i] < limit; i++) {
            final AbstractWidget row = this.rows.get(i);
            // OPTION ROWS ARE INSET TO LEAVE ROOM FOR THE CHEVRON, WHICH ONLY SHOWS UP ON THE HOVERED ONE
            final Option option = optionOf(row);
            if (option != null) {
                this.drawRect(graphics, this.getX(), row.getY(), this.getX() + this.chevronWidth, row.getLimitY(),
                        i == hovered ? Colors.BACKGROUND_HOVER : Colors.BACKGROUND_LIGHT);
                // DISABLED OPTIONS KEEP THEIR STRUCK-THROUGH LOOK, SO THEY GET NO ACCENT EITHER
                if (i == hovered && option.isEnabled()) {
                    this.drawString(graphics, CHEVRON, this.getX() + Layout.OPTION_TEXT_SIDE_PADDING,
                            row.getCenterY() + Layout.REGULAR_TEXT_BASELINE_OFFSET, this.accents[i]);
                }
            }
            row.render(graphics, mouseX, mouseY, delta);
        }
        graphics.disableScissor();

        this.scrollbar.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        // ROWS SCROLLED PAST THE VIEWPORT STILL COVER SCREEN SPACE, SO CLICKS ARE GATED BY THE BODY BOX
        return this.isMouseOver(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double horizontal, final double vertical) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;
        // TWO ROWS PER NOTCH: SODIUM'S FLAT 10px STEP CRAWLS ON LISTS AS LONG AS THE PER-PARTICLE TOGGLES
        this.scrollbar.scroll((int) (-vertical * this.rowHeight * 2));
        return true;
    }

    // SODIUM KEEPS THE OPTION ID PACKAGE-PRIVATE, SO IT COMES THROUGH THE ACCESSOR MIXIN
    private static ResourceLocation idOf(final Option option) {
        return ((OptionAccessor) (Object) option).chloride$id();
    }

    @Nullable
    private static Option optionOf(final AbstractWidget row) {
        if (row instanceof OptionRows.Row value) return value.option();
        return row instanceof ControlElement control ? control.getOption() : null;
    }

    // A FRESH SCROLLBAR PER BUILD ALSO RESETS THE SCROLL, SO EVERY PAGE OPENS AT THE TOP
    private void reset(final int accent) {
        this.clearChildren();
        this.rows.clear();
        this.pending.clear();
        this.accent = accent;
        this.scrollbar = this.addChild(new SaltyScrollbar(new Dim2i(this.getLimitX() - Layout.SCROLLBAR_WIDTH, this.getY(),
                Layout.SCROLLBAR_WIDTH, this.getHeight())));
    }

    // WIDGETS ARE BUILT ONLY HERE BECAUSE THE ROW WIDTH DEPENDS ON WHETHER THE FINAL HEIGHT OVERFLOWS THE BODY
    private void finish() {
        final int count = this.pending.size();
        this.tops = new int[count + 1];
        int offset = 0;
        for (int i = 0; i < count; i++) {
            this.tops[i] = offset;
            offset += this.pending.get(i).height();
        }
        this.tops[count] = offset;

        this.rowWidth = this.getWidth() - (offset > this.getHeight() ? Layout.SCROLLBAR_WIDTH : 0);
        this.accents = new int[count];
        this.notes = new Component[count];
        for (int i = 0; i < count; i++) {
            final Row row = this.pending.get(i);
            this.accents[i] = row.accent();
            this.notes[i] = row.note();
            this.rows.add(this.addChild(row.factory().apply(new Dim2i(this.getX(), this.getY() + this.tops[i], this.rowWidth, row.height()))));
        }
        this.pending.clear();
        this.scrollbar.setScrollbarContext(offset);
    }

    private void add(final int accent, @Nullable final Component note, final int height, final Function<Dim2i, AbstractWidget> factory) {
        this.pending.add(new Row(accent, note, height, factory));
    }

    // GROUP SPACING IS JUST AN EMPTY HALF-HEIGHT SLOT, WHICH KEEPS THE LAYOUT FREE OF SPECIAL CASES
    private void addSpacer(final int accent) {
        this.add(accent, null, this.rowHeight / 2, Spacer::new);
    }

    private void addOption(final ModOptions owner, final Option option, final ColorTheme theme) {
        final ResourceLocation id = idOf(option);
        final Component note = this.note(owner, id);
        final int accent = theme.theme;
        final int inset = this.chevronWidth;
        // THE ROWS THAT UNFOLD UNDER AN OPTION ARE INDENTED ONE STEP FURTHER
        final int nested = this.chevronWidth * 2;

        switch (SaltyOptions.get(id)) {
            case SaltyAction action -> {
                this.add(accent, note, this.rowHeight, dim -> new OptionRows.Action(this, dim.insetLeft(inset), option, action, theme));
                return;
            }
            case SaltyOption.Text text -> {
                this.add(accent, note, this.rowHeight, dim -> new OptionRows.Text(this, dim.insetLeft(inset), option, text, theme));
                return;
            }
            case SaltyOption.Identifiers list -> {
                this.add(accent, note, this.rowHeight, dim -> new OptionRows.Identifiers(this, dim.insetLeft(inset), option, id, list, theme));
                if (!this.screen.expanded(id) || !option.isEnabled()) return;
                for (final ResourceLocation entry: list.value()) {
                    this.add(accent, null, this.rowHeight, dim -> new OptionRows.Entry(this, dim.insetLeft(nested), list, entry, theme));
                }
                this.add(accent, null, this.rowHeight, dim -> new OptionRows.Input(this, dim.insetLeft(nested), list, theme));
                return;
            }
            case SaltyOption.Choices strings -> {
                final List<String> entries = strings.entries();
                // A SINGLE-CHOICE LIST BECOMES ONE FLAG ROW PER VALUE; THE REST FLOAT THEIR CHOICES INSTEAD
                if (strings.radio()) {
                    for (final String entry: entries) {
                        this.add(accent, note, this.rowHeight, dim -> new OptionRows.Flag(this, dim.insetLeft(inset), option, strings, entry, theme));
                    }
                    return;
                }
                this.addDropdown(option, id, theme, note, new OptionRows.Choices(
                        entries.stream().map(Component::literal).map(Component.class::cast).toList(),
                        () -> entries.indexOf(strings.value()), index -> strings.value(entries.get(index))),
                        OptionRows.state(strings));
                return;
            }
            case null, default -> { }
        }

        switch (option) {
            case BooleanOption flag ->
                    this.add(accent, note, this.rowHeight, dim -> new OptionRows.Toggle(this, dim.insetLeft(inset), flag, theme));
            case IntegerOption number ->
                    this.add(accent, note, this.rowHeight, dim -> new OptionRows.Slider(this, dim.insetLeft(inset), number, theme));
            case EnumOption<?> choice -> {
                final OptionRows.Choices choices = choices(choice);
                if (SaltyOptions.dropdown(id)) this.addDropdown(option, id, theme, note, choices, OptionRows.state(choice));
                else this.add(accent, note, this.rowHeight, dim -> new OptionRows.Cycler(this, dim.insetLeft(inset), choice, choices, theme));
            }
            case ExternalButtonOption external ->
                    this.add(accent, note, this.rowHeight, dim -> new OptionRows.External(this, dim.insetLeft(inset), external, this.screen, theme));
            // ANY TYPE CHLORIDE DOES NOT KNOW STILL GETS SODIUM'S OWN CONTROL
            default -> this.add(accent, note, this.rowHeight, dim -> option.getControl().createElement(this.screen, this, dim.insetLeft(inset), theme));
        }
    }

    // THE CHOICES ARE NOT PART OF THE LAYOUT: THE SCREEN FLOATS THEM OVER THE BODY WHEN THE ROW IS CLICKED
    private void addDropdown(final Option option, final ResourceLocation id, final ColorTheme theme, @Nullable final Component note,
                             final OptionRows.Choices choices, final OptionRows.OptionState state) {
        this.add(theme.theme, note, this.rowHeight, dim -> new OptionRows.Dropdown(this, dim.insetLeft(this.chevronWidth),
                option, id, choices, state, theme));
    }

    private static <E extends Enum<E>> OptionRows.Choices choices(final EnumOption<E> option) {
        final List<E> values = Arrays.stream(option.enumClass.getEnumConstants()).filter(option::isValueAllowed).toList();
        return new OptionRows.Choices(values.stream().map(option::getElementName).toList(),
                () -> values.indexOf(option.getValidatedValue()), index -> option.modifyValue(values.get(index)));
    }

    // OPTIONS INJECTED BY ANOTHER MOD KEEP THEIR OWN NAMESPACE, WHICH IS HOW THEIR OWNER IS TOLD APART
    @Nullable
    private Component note(final ModOptions owner, final ResourceLocation id) {
        if (id.getNamespace().equals(owner.configId())) return null;
        return Component.translatable("chloride.settings.modified_by", this.modName(id.getNamespace())).withStyle(ChatFormatting.GRAY);
    }

    // THE OWNER IS NAMED IN ITS OWN ACCENT, SO THE LINE READS LIKE THE RIBBON TAB IT BELONGS TO
    private Component modName(final String namespace) {
        return this.modNames.computeIfAbsent(namespace, key -> {
            for (final ModOptions mod: ConfigManager.CONFIG.getModOptions()) {
                if (mod.configId().equals(key)) {
                    return Component.literal(mod.name()).withStyle(Style.EMPTY.withColor(mod.theme().theme & 0xFFFFFF));
                }
            }
            final var mod = ModList.get().getModFileById(key);
            return Component.literal(mod == null ? I18n.get("chloride.settings.modified_by.unknown") : mod.getMods().getFirst().getDisplayName());
        });
    }

    // THE SCROLLBAR GUTTER IS EXCLUDED SO HOVERING IT DOES NOT LIGHT UP THE ROW BEHIND
    private int rowIndexAt(final int mouseX, final int mouseY) {
        if (this.rows.isEmpty() || !this.isMouseOver(mouseX, mouseY) || mouseX >= this.getX() + this.rowWidth) return -1;
        final int offset = this.getScrollAmount() + mouseY - this.getY();
        return offset < this.tops[this.rows.size()] ? this.rowAt(offset) : -1;
    }

    // BINARY SEARCH OVER THE ROW OFFSETS, SINCE GROUP SPACERS ARE HALF THE HEIGHT OF A REGULAR ROW
    private int rowAt(final int offset) {
        int low = 0;
        int high = this.rows.size() - 1;
        while (low < high) {
            final int middle = (low + high + 1) >>> 1;
            if (this.tops[middle] <= offset) low = middle;
            else high = middle - 1;
        }
        return low;
    }

    private record Row(int accent, @Nullable Component note, int height, Function<Dim2i, AbstractWidget> factory) {}

    // BLANK FILLER ROW: IT ONLY TAKES UP SPACE, WITH NO RENDER, NO HOVER AND NO PLACE IN THE TAB ORDER
    private static final class Spacer extends AbstractWidget {
        private Spacer(final Dim2i dim) {
            super(dim);
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {}

        @Nullable
        @Override
        public ComponentPath nextFocusPath(final FocusNavigationEvent event) {
            return null;
        }
    }

    // MOD AND GROUP TITLES; SODIUM DRAWS THE SAME FLAVOURS AS PLAIN LABELS OVER A FLAT FILL
    private class Header extends AbstractWidget {
        private final Component label;
        private final int textColor;
        private final int background;
        private final ResourceLocation icon;
        private final boolean monochrome;

        Header(final Dim2i dim, final Component label, final int textColor, final int background,
               @Nullable final ResourceLocation icon, final boolean monochrome) {
            super(dim);
            this.label = label.copy().withStyle(ChatFormatting.BOLD);
            this.textColor = textColor;
            this.background = background;
            this.icon = icon;
            this.monochrome = monochrome;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), this.background);
            int offset = Layout.OPTION_TEXT_SIDE_PADDING;
            int baseline = Layout.REGULAR_TEXT_BASELINE_OFFSET;
            if (this.icon != null) {
                offset = VideoSettingsScreen.renderIconWithSpacing(graphics, this.icon, this.textColor, this.monochrome,
                        this.getX(), this.getY(), this.getHeight(), Layout.ICON_MARGIN);
                baseline = Layout.ICON_TEXT_BASELINE_OFFSET;
            }
            this.drawString(graphics, this.truncateTextToFit(this.label.getString(), this.getWidth() - offset - Layout.OPTION_TEXT_SIDE_PADDING),
                    this.getX() + offset, this.getCenterY() + baseline, this.textColor);
        }

        @Override
        public int getY() {
            return super.getY() - SettingsList.this.getScrollAmount();
        }

        @Nullable
        @Override
        public ComponentPath nextFocusPath(final FocusNavigationEvent event) {
            return null;
        }
    }
}
