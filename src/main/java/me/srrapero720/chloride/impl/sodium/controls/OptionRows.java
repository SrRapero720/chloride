package me.srrapero720.chloride.impl.sodium.controls;

import me.srrapero720.chloride.impl.sodium.salty.SaltyAction;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOption;
import net.caffeinemc.mods.sodium.api.config.option.SteppedValidator;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.config.structure.*;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.options.control.ExternalButtonControl;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractParentWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Rows that draw one option each. Chloride replaces Sodium's controls with its own so every value lands on
 * the same right-hand field, and so its extended option types share the look.
 */
public final class OptionRows {
    /** Darkens the field over whatever the row painted, on both the idle and the hovered fill. */
    private static final int FIELD_SHADE = 0x60000000;
    private static final int IDLE_BAR_ALPHA = 0x40000000;
    private static final int INVALID_INPUT_COLOR = 0xFFFF5555;
    private static final Component ARROW = Component.literal("▼");
    private static final Component ARROW_UP = Component.literal("▲");
    /** Diamonds every row that unfolds under an option is marked with, hollow until it is hovered or active. */
    static final Component ENTRY = Component.literal("◇");
    static final Component ENTRY_HOVERED = Component.literal("◆");
    private static final Component REMOVE = Component.literal("×");
    private static final Component ADD = Component.literal("+");
    private static final int BAR_GAP = 1;
    private static final int BAR_HEIGHT = 2;
    private static final int BAR_BOTTOM_MARGIN = 3;

    private OptionRows() {}

    /** Values a selector row shows, where the index is polled so it follows the pending value. */
    public record Choices(List<Component> labels, IntSupplier index, IntConsumer select) {}

    /** Pending and loaded state used by the two action buttons of a value row. */
    public record OptionState(BooleanSupplier changed, BooleanSupplier appliedNonDefault, Runnable restore, Runnable reset) {
        /** Runs {@code refresh} after either action, for controls whose child value or structure must be rebuilt. */
        public OptionState refreshing(final Runnable refresh) {
            return new OptionState(this.changed, this.appliedNonDefault,
                    () -> {
                        this.restore.run();
                        refresh.run();
                    }, () -> {
                        this.reset.run();
                        refresh.run();
                    });
        }
    }

    /** Creates row actions for a native Sodium option. */
    public static OptionState state(final StatefulOption<?> option) {
        return stateOf(option);
    }

    private static <V> OptionState stateOf(final StatefulOption<V> option) {
        return new OptionState(
                option::hasChanged,
                () -> !Objects.equals(option.getAppliedValue(), option.getDefaultValue().get(ConfigManager.CONFIG)),
                () -> option.modifyValue(option.getAppliedValue()),
                option::resetToDefault
        );
    }

    /** Creates row actions for one of Chloride's extended values. */
    public static OptionState state(final SaltyOption<?> option) {
        return new OptionState(option::changed, () -> !option.appliedDefault(), option::restore, option::reset);
    }

    /** Shared frame of every option row: hover fill, label on the left and the shaded field on the right. */
    public abstract static class Row extends AbstractParentWidget {
        protected final SettingsList list;
        protected final ColorTheme theme;

        protected Row(final SettingsList list, final Dim2i dim, final ColorTheme theme) {
            super(dim);
            this.list = list;
            this.theme = theme;
        }

        /** The option this row stands for, or {@code null} for the rows that unfold under one. */
        @Nullable
        public Option option() {
            return null;
        }

        public SettingsList list() {
            return this.list;
        }

        public ColorTheme theme() {
            return this.theme;
        }

        // HALF THE ROW, 15% OFF, THEN ANOTHER 25% OFF, SNAPPED DOWN TO A ROUND NUMBER OF PIXELS
        public int fieldX() {
            final int width = this.contentLimitX() - this.getX();
            return this.contentLimitX() - width / 2 * 85 / 100 * 75 / 100 / 10 * 10;
        }

        /** Right edge of the option field, excluding trailing action slots. */
        public int contentLimitX() {
            return this.getLimitX();
        }

        protected void drawBackground(final GuiGraphics graphics, final int mouseX, final int mouseY, final boolean field) {
            this.hovered = this.isMouseOver(mouseX, mouseY);
            this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(),
                    this.hovered ? Colors.BACKGROUND_HOVER : Colors.BACKGROUND_LIGHT);
            if (field) this.drawRect(graphics, this.fieldX(), this.getY(), this.contentLimitX(), this.getLimitY(), FIELD_SHADE);
        }

        // SAME LABEL FORMATTING SODIUM GIVES ITS CONTROLS: STRUCK THROUGH WHILE DISABLED, ITALIC WHILE MODIFIED
        protected void drawLabel(final GuiGraphics graphics, final Component name, final boolean enabled, final boolean modified) {
            String label = name.getString();
            if (modified) label = label + " *";
            final String fitted = this.truncateTextToFit(label, this.fieldX() - this.getX() - Layout.OPTION_TEXT_SIDE_PADDING * 2);
            final String formatted = !enabled ? "" + ChatFormatting.GRAY + ChatFormatting.STRIKETHROUGH + fitted
                    : modified ? ChatFormatting.ITALIC + fitted : fitted;
            this.drawString(graphics, formatted, this.getX() + Layout.OPTION_TEXT_SIDE_PADDING, this.baseline(),
                    !enabled ? Colors.FOREGROUND_DISABLED : this.hovered ? this.theme.theme : Colors.FOREGROUND);
        }

        protected void drawValue(final GuiGraphics graphics, final Component value, final boolean enabled, final int reserved) {
            final int color = enabled ? Colors.FOREGROUND : Colors.FOREGROUND_DISABLED;
            final int available = this.contentLimitX() - this.fieldX() - reserved;
            final int width = this.font.width(value);
            // TRUNCATING GOES THROUGH PLAIN TEXT AND LOSES THE STYLING, SO IT ONLY KICKS IN WHEN IT HAS TO
            if (width > available) {
                final String fitted = this.truncateTextToFit(value.getString(), available);
                this.drawString(graphics, fitted, (this.fieldX() + this.contentLimitX() - reserved - this.font.width(fitted)) / 2,
                        this.baseline(), color);
                return;
            }
            this.drawString(graphics, value, (this.fieldX() + this.contentLimitX() - reserved - width) / 2, this.baseline(), color);
        }

        protected int baseline() {
            return this.getCenterY() + Layout.REGULAR_TEXT_BASELINE_OFFSET;
        }

        protected int trackX() {
            return this.fieldX() + Layout.OPTION_TEXT_SIDE_PADDING;
        }

        protected int trackWidth() {
            return this.contentLimitX() - Layout.OPTION_TEXT_SIDE_PADDING - this.trackX();
        }

        protected int trackY() {
            return this.getLimitY() - BAR_BOTTOM_MARGIN;
        }

        // DIAMOND PLUS TEXT, THE SHARED LOOK OF EVERY ROW THAT UNFOLDS UNDER AN OPTION
        protected void drawEntry(final GuiGraphics graphics, final Component marker, final String text, final int textColor, final int reserved) {
            final int markerX = this.getX() + Layout.OPTION_TEXT_SIDE_PADDING;
            final int textX = markerX + this.font.width(marker) + Layout.OPTION_GROUP_MARGIN;
            this.drawString(graphics, marker, markerX, this.baseline(), this.theme.theme);
            this.drawString(graphics, this.truncateTextToFit(text, this.getLimitX() - textX - reserved - Layout.OPTION_TEXT_SIDE_PADDING),
                    textX, this.baseline(), textColor);
        }

        @Override
        public int getY() {
            return super.getY() - this.list.getScrollAmount();
        }

        // THE EDIT BOXES ARE TALLER THAN THE ROW, SO CLICKS ARE GATED BY THE ROW ITSELF
        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            return this.isMouseOver(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, button);
        }
    }

    /** Row that stands for an option and reserves two square action slots on its right edge. */
    public abstract static class ValueRow extends Row {
        protected final Option option;
        private final OptionState state;
        private final OptionActionButton undo;
        private final OptionActionButton reset;

        protected ValueRow(final SettingsList list, final Dim2i dim, final Option option, final OptionState state,
                           final ColorTheme theme) {
            this(list, dim, option, state, () -> true, theme);
        }

        protected ValueRow(final SettingsList list, final Dim2i dim, final Option option, final OptionState state,
                           final BooleanSupplier actionsShown, final ColorTheme theme) {
            super(list, dim, theme);
            this.option = option;
            this.state = state;
            this.undo = new OptionActionButton(this, OptionActionButton.Type.UNDO, () -> actionsShown.getAsBoolean() && state.changed().getAsBoolean(), state.restore());
            this.reset = new OptionActionButton(this, OptionActionButton.Type.RESET, () -> actionsShown.getAsBoolean() && state.appliedNonDefault().getAsBoolean(), state.reset());
            this.addRenderableChild(this.undo);
            this.addRenderableChild(this.reset);
        }

        @Override
        public Option option() {
            return this.option;
        }

        public final OptionState state() {
            return this.state;
        }

        public final OptionActionButton undoButton() {
            return this.undo;
        }

        public final OptionActionButton resetButton() {
            return this.reset;
        }

        protected final void drawCheck(final GuiGraphics graphics, final boolean ticked) {
            final boolean enabled = this.option.isEnabled();
            final int x = (this.fieldX() + this.contentLimitX() - Layout.CONTROL_ICON_SIZE) / 2;
            final int y = this.getCenterY() - Layout.CONTROL_ICON_SIZE / 2;
            final int limitX = x + Layout.CONTROL_ICON_SIZE;
            final int limitY = y + Layout.CONTROL_ICON_SIZE;
            final int color = !enabled ? Colors.FOREGROUND_DISABLED : ticked ? this.theme.theme : Colors.FOREGROUND;
            if (ticked) this.drawRect(graphics, x + 2, y + 2, limitX - 2, limitY - 2, color);
            this.drawBorder(graphics, x, y, limitX, limitY, color);
        }

        @Override
        public int contentLimitX() {
            return this.getLimitX() - this.getHeight() * 2;
        }

        protected void drawRow(final GuiGraphics graphics, final int mouseX, final int mouseY) {
            this.drawRow(graphics, mouseX, mouseY, this.option.getName());
        }

        // ROWS THAT STAND FOR ONE VALUE OF AN OPTION LABEL THEMSELVES WITH THE VALUE, NOT THE OPTION NAME
        protected void drawRow(final GuiGraphics graphics, final int mouseX, final int mouseY, final Component name) {
            this.drawBackground(graphics, mouseX, mouseY, true);
            this.drawLabel(graphics, name, this.option.isEnabled(), this.state.changed().getAsBoolean());
            this.undo.render(graphics, mouseX, mouseY, 0.0f);
            this.reset.render(graphics, mouseX, mouseY, 0.0f);
        }

        // ONLY STATEFUL OPTIONS CAN HIDE THEIR CONTROL WHILE DISABLED; THE REST ALWAYS SHOW IT
        protected boolean showControl() {
            return !(this.option instanceof StatefulOption<?> stateful) || stateful.showControl();
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (!this.isMouseOver(mouseX, mouseY)) return false;
            if (this.undo.mouseClicked(mouseX, mouseY, button) || this.reset.mouseClicked(mouseX, mouseY, button)) return true;
            return mouseX >= this.contentLimitX() || super.mouseClicked(mouseX, mouseY, button);
        }
    }

    /** Boolean option, drawn as Sodium's tick box centered on the field. */
    public static final class Toggle extends ValueRow {
        private final BooleanOption option;

        public Toggle(final SettingsList list, final Dim2i dim, final BooleanOption option, final ColorTheme theme) {
            super(list, dim, option, OptionRows.state(option), theme);
            this.option = option;
        }

        public BooleanOption booleanOption() {
            return this.option;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            this.drawRow(graphics, mouseX, mouseY);
            if (!this.showControl()) return;

            this.drawCheck(graphics, this.option.getValidatedValue());
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (!super.mouseClicked(mouseX, mouseY, button) && this.option.isEnabled() && button == 0 && this.isMouseOver(mouseX, mouseY)) {
                this.playClickSound();
                this.option.modifyValue(!this.option.getValidatedValue());
            }
            return this.isMouseOver(mouseX, mouseY);
        }
    }

    /** Integer option, drawn as the value over a track that fills the bottom of the field. */
    public static final class Slider extends ValueRow {
        private final IntegerOption option;
        private boolean held;

        public Slider(final SettingsList list, final Dim2i dim, final IntegerOption option, final ColorTheme theme) {
            super(list, dim, option, OptionRows.state(option), theme);
            this.option = option;
        }

        public IntegerOption integerOption() {
            return this.option;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            this.drawRow(graphics, mouseX, mouseY);
            if (!this.showControl()) return;

            final boolean enabled = this.option.isEnabled();
            final int value = this.option.getValidatedValue();
            this.drawValue(graphics, this.option.formatValue(value), enabled, 0);

            // THE TRACK IS AS THICK AS THE BARS AN ENUM DRAWS, SO BOTH CONTROLS READ THE SAME WEIGHT
            final int width = this.trackWidth();
            final int filled = (int) (width * this.progress(value));
            final int y = this.trackY();
            final int color = enabled ? this.theme.theme : Colors.FOREGROUND_DISABLED;
            this.drawRect(graphics, this.trackX(), y, this.trackX() + width, y + BAR_HEIGHT, fade(color));
            this.drawRect(graphics, this.trackX(), y, this.trackX() + filled, y + BAR_HEIGHT, color);
            this.drawRect(graphics, this.trackX() + filled - 1, y - 2, this.trackX() + filled + 1, y + BAR_HEIGHT + 2,
                    enabled ? Colors.FOREGROUND : Colors.FOREGROUND_DISABLED);
        }

        private double progress(final int value) {
            final SteppedValidator range = this.option.getSteppedValidator();
            return Mth.clamp((double) (value - range.min()) / Math.max(1, range.max() - range.min()), 0.0, 1.0);
        }

        private void applyMouse(final double mouseX) {
            final SteppedValidator range = this.option.getSteppedValidator();
            final double progress = Mth.clamp((mouseX - this.trackX()) / this.trackWidth(), 0.0, 1.0);
            final int step = range.step();
            this.option.modifyValue(range.min() + step * (int) Math.round(progress * (range.max() - range.min()) / step));
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            this.held = false;
            if (super.mouseClicked(mouseX, mouseY, button)) return true;
            if (!this.option.isEnabled() || button != 0 || !this.isMouseOver(mouseX, mouseY)) return false;
            if (mouseX >= this.fieldX()) {
                this.applyMouse(mouseX);
                this.held = true;
            }
            return true;
        }

        @Override
        public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
            if (!this.held) return false;
            this.applyMouse(mouseX);
            return true;
        }

        @Override
        public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
            if (!this.held) return false;
            this.held = false;
            this.playClickSound();
            return true;
        }

        @Override
        public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
            if (!this.isFocused() || !this.option.isEnabled()) return false;
            final SteppedValidator range = this.option.getSteppedValidator();
            if (keyCode == 263) this.option.modifyValue(this.option.getValidatedValue() - range.step());
            else if (keyCode == 262) this.option.modifyValue(this.option.getValidatedValue() + range.step());
            else return false;
            return true;
        }
    }

    /** Selector drawn Sodium-style: the value on the field plus one bar per choice along its bottom edge. */
    public static final class Cycler extends ValueRow {
        private final Choices choices;

        public Cycler(final SettingsList list, final Dim2i dim, final StatefulOption<?> option, final Choices choices, final ColorTheme theme) {
            super(list, dim, option, OptionRows.state(option), theme);
            this.choices = choices;
        }

        public Choices choices() {
            return this.choices;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            this.drawRow(graphics, mouseX, mouseY);
            if (!this.showControl()) return;

            final boolean enabled = this.option.isEnabled();
            final int index = this.choices.index().getAsInt();
            if (index >= 0) this.drawValue(graphics, this.choices.labels().get(index), enabled, 0);

            // ONE BAR PER CHOICE, ALL FADED OUT BUT THE ACTIVE ONE
            final int count = this.choices.labels().size();
            if (count == 0) return;
            final int width = (this.trackWidth() - (count - 1) * BAR_GAP) / count;
            final int color = enabled ? this.theme.theme : Colors.FOREGROUND_DISABLED;
            for (int i = 0; i < count; i++) {
                final int x = this.trackX() + i * (width + BAR_GAP);
                this.drawRect(graphics, x, this.trackY(), x + width, this.trackY() + BAR_HEIGHT, i == index ? color : fade(color));
            }
        }

        private void cycle(final boolean reverse) {
            final int count = this.choices.labels().size();
            final int index = this.choices.index().getAsInt();
            this.playClickSound();
            this.choices.select().accept(Math.floorMod(index + (reverse ? -1 : 1), count));
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (!super.mouseClicked(mouseX, mouseY, button) && this.option.isEnabled() && button == 0 && this.isMouseOver(mouseX, mouseY)) {
                this.cycle(Screen.hasShiftDown());
            }
            return this.isMouseOver(mouseX, mouseY);
        }

        @Override
        public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
            if (!this.isFocused() || !CommonInputs.selected(keyCode) || !this.option.isEnabled()) return false;
            this.cycle(Screen.hasShiftDown());
            return true;
        }
    }

    /** Selector drawn as a dropdown: the value plus an arrow, unfolding its choices as rows underneath. */
    public static final class Dropdown extends ValueRow {
        private final ResourceLocation id;
        private final Choices choices;

        public Dropdown(final SettingsList list, final Dim2i dim, final Option option, final ResourceLocation id, final Choices choices,
                        final OptionState state, final ColorTheme theme) {
            super(list, dim, option, state, theme);
            this.id = id;
            this.choices = choices;
        }

        public ResourceLocation id() {
            return this.id;
        }

        public Choices choices() {
            return this.choices;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            this.drawRow(graphics, mouseX, mouseY);
            if (!this.showControl()) return;

            final boolean enabled = this.option.isEnabled();
            // THE ARROW FLIPS WHILE THE FLOATING LIST IS OPEN, THE WAY EVERY DROPDOWN DOES
            final Component arrow = this.list.screen().dropdownOpen(this.id) ? ARROW_UP : ARROW;
            final int reserved = this.font.width(arrow) + Layout.OPTION_TEXT_SIDE_PADDING;
            final int index = this.choices.index().getAsInt();
            if (index >= 0) this.drawValue(graphics, this.choices.labels().get(index), enabled, reserved);
            this.drawString(graphics, arrow, this.contentLimitX() - Layout.OPTION_TEXT_SIDE_PADDING - this.font.width(arrow), this.baseline(),
                    enabled ? this.theme.theme : Colors.FOREGROUND_DISABLED);
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (!super.mouseClicked(mouseX, mouseY, button) && this.option.isEnabled() && button == 0 && this.isMouseOver(mouseX, mouseY)) {
                this.playClickSound();
                this.list.screen().openDropdown(this, this.id, this.choices);
            }
            return this.isMouseOver(mouseX, mouseY);
        }
    }

    /** Option that opens another screen, drawn as Sodium's underlined button text on the field. */
    public static final class External extends Row {
        private final ExternalButtonOption option;
        private final Screen screen;

        public External(final SettingsList list, final Dim2i dim, final ExternalButtonOption option, final Screen screen, final ColorTheme theme) {
            super(list, dim, theme);
            this.option = option;
            this.screen = screen;
        }

        public Screen screen() {
            return this.screen;
        }

        @Override
        public Option option() {
            return this.option;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            final boolean enabled = this.option.isEnabled();
            this.drawBackground(graphics, mouseX, mouseY, true);
            this.drawLabel(graphics, this.option.getName(), enabled, false);
            this.drawValue(graphics, ExternalButtonControl.formatExternalButtonText(enabled, this.theme), enabled, 0);
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (button != 0 || !this.option.isEnabled() || !this.isMouseOver(mouseX, mouseY)) return false;
            this.playClickSound();
            this.option.getCurrentScreenConsumer().accept(this.screen);
            return true;
        }
    }

    /** Chloride's action option: a button on the field that runs something instead of holding a value. */
    public static final class Action extends Row {
        private final Option option;
        private final SaltyAction action;

        public Action(final SettingsList list, final Dim2i dim, final Option option, final SaltyAction action, final ColorTheme theme) {
            super(list, dim, theme);
            this.option = option;
            this.action = action;
        }

        public SaltyAction action() {
            return this.action;
        }

        @Override
        public Option option() {
            return this.option;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            final boolean enabled = this.option.isEnabled();
            this.drawBackground(graphics, mouseX, mouseY, true);
            this.drawLabel(graphics, this.option.getName(), enabled, false);
            // SAME ARROW SODIUM PUTS ON THE PAGES THAT LEAVE THE SETTINGS, LEADING THE BUTTON TEXT
            final Component arrow = Component.literal(ExternalButtonControl.EXTERNAL_PAGE_PREFIX)
                    .withStyle(Style.EMPTY.withColor(this.theme.theme & 0xFFFFFF));
            this.drawValue(graphics, arrow.copy().append(this.action.label().copy()
                    .withStyle(this.hovered && enabled ? ChatFormatting.UNDERLINE : ChatFormatting.RESET)), enabled, 0);
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (button != 0 || !this.option.isEnabled() || !this.isMouseOver(mouseX, mouseY)) return false;
            this.playClickSound();
            this.action.run();
            return true;
        }
    }

    /** One value of a single-choice list, drawn as a flag row: ticking it unticks every other one. */
    public static final class Flag extends ValueRow {
        private final SaltyOption.Choices value;
        private final String entry;

        public Flag(final SettingsList list, final Dim2i dim, final Option option, final SaltyOption.Choices value, final String entry,
                    final ColorTheme theme) {
            // ONLY THE PICKED ROW CARRIES THE PENDING MARK, OR EVERY FLAG OF THE LIST WOULD WEAR IT
            super(list, dim, option, OptionRows.state(value), () -> entry.equals(value.value()), theme);
            this.value = value;
            this.entry = entry;
        }

        public SaltyOption.Choices value() {
            return this.value;
        }

        public String entry() {
            return this.entry;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            this.drawRow(graphics, mouseX, mouseY, Component.literal(this.entry));
            if (!this.showControl()) return;

            this.drawCheck(graphics, this.entry.equals(this.value.value()));
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (!super.mouseClicked(mouseX, mouseY, button) && this.option.isEnabled() && button == 0 && this.isMouseOver(mouseX, mouseY)) {
                // SINGLE CHOICE: PICKING ONE IS THE SAME AS DROPPING EVERY OTHER FLAG
                this.playClickSound();
                this.value.value(this.entry);
            }
            return this.isMouseOver(mouseX, mouseY);
        }
    }

    /** Chloride's text option: an edit box filling the field. */
    public static final class Text extends ValueRow {
        private final SaltyOption.Text value;
        private final EditBox box;

        public Text(final SettingsList list, final Dim2i dim, final Option option, final SaltyOption.Text value, final ColorTheme theme) {
            // RESETTING REBUILDS THE ROW, WHICH IS WHAT PUTS THE RESTORED VALUE BACK INTO THE EDIT BOX
            super(list, dim, option, OptionRows.state(value).refreshing(list.screen()::refresh), theme);
            this.value = value;
            this.box = new EditBox(this.font, this.fieldX() + Layout.OPTION_TEXT_SIDE_PADDING, dim.getCenterY() - this.font.lineHeight / 2,
                    this.contentLimitX() - this.fieldX() - Layout.OPTION_TEXT_SIDE_PADDING * 2, dim.height(), option.getName());
            this.box.setMaxLength(value.maxLength);
            this.box.setBordered(false);
            this.box.setValue(value.value());
            this.box.setResponder(value::value);
            this.addRenderableChild(this.box);
        }

        public SaltyOption.Text value() {
            return this.value;
        }

        public EditBox box() {
            return this.box;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            this.drawRow(graphics, mouseX, mouseY);
            if (!this.showControl()) return;
            this.box.setEditable(this.option.isEnabled());
            this.box.setY(this.getCenterY() - this.font.lineHeight / 2);
            this.box.render(graphics, mouseX, mouseY, delta);
        }

        @Override
        public void setFocused(final boolean focused) {
            super.setFocused(focused);
            this.setFocused(focused ? this.box : null);
        }
    }

    /** Chloride's identifier list: the entry count on the field, unfolding its entries as rows underneath. */
    public static final class Identifiers extends ValueRow {
        private static final Component COLLAPSED = Component.literal("▶");

        private final ResourceLocation id;
        private final SaltyOption.Identifiers value;

        public Identifiers(final SettingsList list, final Dim2i dim, final Option option, final ResourceLocation id,
                           final SaltyOption.Identifiers value, final ColorTheme theme) {
            super(list, dim, option, OptionRows.state(value).refreshing(list.screen()::refresh), theme);
            this.id = id;
            this.value = value;
        }

        public ResourceLocation id() {
            return this.id;
        }

        public SaltyOption.Identifiers value() {
            return this.value;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            this.drawRow(graphics, mouseX, mouseY);
            if (!this.showControl()) return;

            final boolean enabled = this.option.isEnabled();
            final Component marker = this.list.screen().expanded(this.id) ? ARROW : COLLAPSED;
            final int arrow = this.font.width(marker) + Layout.OPTION_TEXT_SIDE_PADDING;
            this.drawValue(graphics, Component.literal(String.valueOf(this.value.value().size())), enabled, arrow);
            this.drawString(graphics, marker, this.contentLimitX() - Layout.OPTION_TEXT_SIDE_PADDING - this.font.width(marker), this.baseline(),
                    enabled ? this.theme.theme : Colors.FOREGROUND_DISABLED);
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) return true;
            if (button != 0 || !this.option.isEnabled() || !this.isMouseOver(mouseX, mouseY)) return false;
            this.playClickSound();
            this.list.screen().toggleExpanded(this.id);
            return true;
        }
    }

    /** One entry of an identifier list, removed by the cross on its right. */
    public static final class Entry extends Row {
        private final SaltyOption.Identifiers value;
        private final ResourceLocation entry;

        public Entry(final SettingsList list, final Dim2i dim, final SaltyOption.Identifiers value, final ResourceLocation entry,
                     final ColorTheme theme) {
            super(list, dim, theme);
            this.value = value;
            this.entry = entry;
        }

        public SaltyOption.Identifiers value() {
            return this.value;
        }

        public ResourceLocation entry() {
            return this.entry;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            this.drawBackground(graphics, mouseX, mouseY, false);
            final int removeWidth = this.font.width(REMOVE) + Layout.OPTION_TEXT_SIDE_PADDING;
            this.drawEntry(graphics, this.hovered ? ENTRY_HOVERED : ENTRY, this.entry.toString(),
                    this.hovered ? this.theme.theme : Colors.FOREGROUND, removeWidth);
            this.drawString(graphics, REMOVE, this.removeX(), this.baseline(), this.hovered ? this.theme.theme : Colors.FOREGROUND_DISABLED);
        }

        private int removeX() {
            return this.getLimitX() - Layout.OPTION_TEXT_SIDE_PADDING - this.font.width(REMOVE);
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (button != 0 || mouseX < this.removeX() - Layout.OPTION_TEXT_SIDE_PADDING || !this.isMouseOver(mouseX, mouseY)) return false;
            this.playClickSound();
            this.value.value().remove(this.entry);
            this.list.screen().refresh();
            return true;
        }
    }

    /** Trailing row of an identifier list: type an identifier and append it. */
    public static final class Input extends Row {
        private final SaltyOption.Identifiers value;
        private final EditBox box;

        public Input(final SettingsList list, final Dim2i dim, final SaltyOption.Identifiers value, final ColorTheme theme) {
            super(list, dim, theme);
            this.value = value;
            this.box = new EditBox(this.font, dim.x() + Layout.OPTION_TEXT_SIDE_PADDING, dim.getCenterY() - this.font.lineHeight / 2,
                    dim.width() - Layout.OPTION_LABEL_END_PADDING - Layout.OPTION_TEXT_SIDE_PADDING, dim.height(),
                    Component.translatable("chloride.settings.list.add"));
            this.box.setBordered(false);
            this.box.setHint(Component.translatable("chloride.settings.list.add").withStyle(ChatFormatting.GRAY));
            // RED WHILE THE TEXT IS NOT A VALID IDENTIFIER, SO TYPOS SHOW UP BEFORE THE ENTRY IS ADDED
            this.box.setResponder(text -> this.box.setTextColor(text.isEmpty() || ResourceLocation.tryParse(text) != null
                    ? Colors.FOREGROUND : INVALID_INPUT_COLOR));
            this.addRenderableChild(this.box);
        }

        public SaltyOption.Identifiers value() {
            return this.value;
        }

        public EditBox box() {
            return this.box;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            this.drawBackground(graphics, mouseX, mouseY, false);
            this.box.setY(this.getCenterY() - this.font.lineHeight / 2);
            this.drawString(graphics, ADD, this.addX(), this.baseline(), this.hovered ? this.theme.theme : Colors.FOREGROUND);
            super.render(graphics, mouseX, mouseY, delta);
        }

        private int addX() {
            return this.getLimitX() - Layout.OPTION_TEXT_SIDE_PADDING - this.font.width(ADD);
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (button == 0 && mouseX >= this.addX() - Layout.OPTION_TEXT_SIDE_PADDING && this.isMouseOver(mouseX, mouseY)) {
                this.commit();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
            if (CommonInputs.selected(keyCode) && this.box.isFocused()) {
                this.commit();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        private void commit() {
            final ResourceLocation entry = ResourceLocation.tryParse(this.box.getValue());
            if (entry == null || this.value.value().contains(entry)) return;
            this.playClickSound();
            this.value.value().add(entry);
            this.list.screen().refresh();
        }

        @Override
        public void setFocused(final boolean focused) {
            super.setFocused(focused);
            this.setFocused(focused ? this.box : null);
        }
    }

    private static int fade(final int color) {
        return color & 0xFFFFFF | IDLE_BAR_ALPHA;
    }
}
