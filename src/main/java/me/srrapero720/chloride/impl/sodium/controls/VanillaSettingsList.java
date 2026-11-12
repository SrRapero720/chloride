package me.srrapero720.chloride.impl.sodium.controls;

import me.srrapero720.chloride.impl.sodium.salty.SaltyAction;
import me.srrapero720.chloride.impl.sodium.salty.SaltyEntry;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOption;
import me.srrapero720.chloride.impl.sodium.salty.SaltyOptions;
import me.srrapero720.chloride.impl.sodium.screens.SaltySettingsScreen;
import me.srrapero720.chloride.mixins.impl.sodium.OptionAccessor;
import net.caffeinemc.mods.sodium.api.config.option.SteppedValidator;
import net.caffeinemc.mods.sodium.client.config.structure.*;
import net.caffeinemc.mods.sodium.client.gui.options.control.ExternalButtonControl;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

/** Vanilla option list built exclusively from Minecraft's regular menu widgets. */
public final class VanillaSettingsList extends ContainerObjectSelectionList<VanillaSettingsList.Entry> implements SettingsView {
    private static final int ROW_WIDTH = 310;
    private static final int ROW_HEIGHT = 25;
    private static final int CONTROL_GAP = 10;

    private final SaltySettingsScreen screen;
    private final List<Option> shown = new ArrayList<>();
    @Nullable
    private Entry pending;

    public VanillaSettingsList(final SaltySettingsScreen screen, final Dim2i dim) {
        super(Minecraft.getInstance(), dim.width(), dim.height(), dim.y(), ROW_HEIGHT);
        this.screen = screen;
        this.setX(dim.x());
        this.centerListVertically = false;
    }

    @Override
    public int getRowWidth() {
        return Math.clamp(this.getWidth() - 20, 1, ROW_WIDTH);
    }

    @Override
    public void showPage(final ModOptions mod, final Page page) {
        this.showPage(mod, page, null);
    }

    @Override
    public void showPageResults(final ModOptions mod, final Page page, final List<Option> options) {
        final Set<Option> matches = Collections.newSetFromMap(new IdentityHashMap<>());
        matches.addAll(options);
        this.showPage(mod, page, matches);
    }

    private void showPage(final ModOptions mod, final Page page, @Nullable final Set<Option> matches) {
        this.clearEntries();
        this.shown.clear();
        this.pending = null;
        for (final OptionGroup group: page.groups()) {
            final List<Option> options = matches == null ? group.options() : group.options().stream().filter(matches::contains).toList();
            if (options.isEmpty()) continue;
            this.pending = null;
            if (group.name() != null) this.addEntry(Entry.header(group.name()));
            for (final Option option: options) this.addOption(option);
        }
        this.setScrollAmount(0.0);
    }

    @Override
    public void showResults(final List<SaltySettingsScreen.Section> sections) {
        this.clearEntries();
        this.shown.clear();
        this.pending = null;
        for (final SaltySettingsScreen.Section section: sections) {
            this.pending = null;
            this.addEntry(Entry.header(Component.literal(section.mod().name())));
            for (final Option option: section.options()) this.addOption(option);
        }
        this.setScrollAmount(0.0);
    }

    @Override
    public boolean dropFiles(final List<Path> paths) {
        for (final Option option: this.shown) {
            final SaltyEntry entry = SaltyOptions.get(idOf(option));
            if (entry != null && entry.drop(paths)) return true;
        }
        return false;
    }

    @Nullable
    @Override
    public SaltySettingsScreen.Tip tooltipAt(final int mouseX, final int mouseY) {
        return null;
    }

    private void addOption(final Option option) {
        this.shown.add(option);
        final SaltyEntry salty = SaltyOptions.get(idOf(option));
        final AbstractWidget control;
        switch (salty) {
            case final SaltyAction action -> control = Button.builder(CommonComponents.optionNameValue(option.getName(), action.label()),
                    button -> action.run()).build();
            case final SaltyOption.Text text -> {
                final EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, 150, 20, option.getName());
                box.setMaxLength(text.maxLength);
                box.setHint(option.getName().copy().withStyle(ChatFormatting.GRAY));
                box.setValue(text.value());
                box.setResponder(text::value);
                control = box;
            }
            case final SaltyOption.Choices choices -> {
                final List<String> values = choices.entries();
                if (values.isEmpty()) {
                    final Button empty = Button.builder(CommonComponents.optionNameValue(option.getName(),
                            Component.translatable("chloride.settings.empty")), button -> {}).build();
                    empty.active = false;
                    control = empty;
                } else {
                    control = CycleButton.builder(Component::literal)
                            .withValues(values)
                            .withInitialValue(choices.value())
                            .create(0, 0, 150, 20, option.getName(), (button, value) -> choices.value(value));
                }
            }
            case final SaltyOption.Identifiers identifiers -> {
                final ResourceLocation id = idOf(option);
                final Component value = Component.translatable("chloride.settings.list.entries", identifiers.value().size());
                control = Button.builder(CommonComponents.optionNameValue(option.getName(), value),
                        button -> this.screen.toggleExpanded(id)).build();
                this.addControl(option, control);
                if (this.screen.expanded(id) && option.isEnabled()) {
                    this.pending = null;
                    for (final ResourceLocation entry: List.copyOf(identifiers.value())) {
                        this.addControl(option, Button.builder(Component.literal(entry + "  ×"), button -> {
                                    identifiers.value().remove(entry);
                                    this.screen.refresh();
                                }).build());
                    }
                    this.addControl(option, new IdentifierInput(identifiers));
                    this.pending = null;
                }
                return;
            }
            case null, default -> {
                control = switch (option) {
                    case final BooleanOption flag -> CycleButton.onOffBuilder(flag.getValidatedValue()).create(0, 0, 150, 20, option.getName(), (button, value) -> flag.modifyValue(value));
                    case final IntegerOption number -> new IntegerSlider(number);
                    case final EnumOption<?> choice -> this.enumControl(choice);
                    case final ExternalButtonOption external -> Button.builder(CommonComponents.optionNameValue(option.getName(),
                                    ExternalButtonControl.BASE_BUTTON_TEXT),
                            button -> external.getCurrentScreenConsumer().accept(this.screen)).build();
                    default -> {
                        final Button unsupported = Button.builder(CommonComponents.optionNameValue(option.getName(),
                                Component.translatable("chloride.settings.unsupported")), button -> {}).build();
                        unsupported.active = false;
                        yield unsupported;
                    }
                };
            }
        }

        this.addControl(option, control);
    }

    private void addControl(final Option option, final AbstractWidget control) {
        if (this.pending == null || this.pending.full()) {
            this.pending = new Entry();
            this.addEntry(this.pending);
        }
        this.pending.add(option, control);
    }

    private <E extends Enum<E>> AbstractWidget enumControl(final EnumOption<E> option) {
        final List<E> values = Arrays.stream(option.enumClass.getEnumConstants()).filter(option::isValueAllowed).toList();
        if (values.isEmpty()) {
            final Button empty = Button.builder(CommonComponents.optionNameValue(option.getName(),
                    Component.translatable("chloride.settings.empty")), button -> {}).build();
            empty.active = false;
            return empty;
        }
        return CycleButton.builder(option::getElementName)
                .withValues(values)
                .withInitialValue(option.getValidatedValue())
                .create(0, 0, 150, 20, option.getName(), (button, value) -> option.modifyValue(value));
    }

    private static ResourceLocation idOf(final Option option) {
        return ((OptionAccessor) option).chloride$id();
    }

    /** Identifier input committed with Enter, matching vanilla text-field keyboard behavior. */
    private final class IdentifierInput extends EditBox {
        private final SaltyOption.Identifiers identifiers;

        private IdentifierInput(final SaltyOption.Identifiers identifiers) {
            super(Minecraft.getInstance().font, 0, 0, 150, 20, Component.translatable("chloride.settings.list.add"));
            this.identifiers = identifiers;
            this.setHint(Component.translatable("chloride.settings.list.add").withStyle(ChatFormatting.GRAY));
        }

        @Override
        public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
            if (!CommonInputs.selected(keyCode)) return super.keyPressed(keyCode, scanCode, modifiers);
            final ResourceLocation entry = ResourceLocation.tryParse(this.getValue());
            if (entry == null || this.identifiers.value().contains(entry)) return true;
            this.identifiers.value().add(entry);
            VanillaSettingsList.this.screen.refresh();
            return true;
        }
    }

    /** A regular vanilla slider which resolves Sodium's dynamic range whenever it is used. */
    private static final class IntegerSlider extends AbstractSliderButton {
        private final IntegerOption option;

        private IntegerSlider(final IntegerOption option) {
            super(0, 0, 150, 20, CommonComponents.EMPTY, normalized(option));
            this.option = option;
            this.updateMessage();
        }

        private static double normalized(final IntegerOption option) {
            final SteppedValidator range = option.getSteppedValidator();
            final int steps = Math.max(1, (range.max() - range.min()) / range.step());
            return (double) ((option.getValidatedValue() - range.min()) / range.step()) / steps;
        }

        private void sync() {
            this.value = normalized(this.option);
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(this.option == null ? CommonComponents.EMPTY
                    : CommonComponents.optionNameValue(this.option.getName(), this.option.formatValue(this.option.getValidatedValue())));
        }

        @Override
        protected void applyValue() {
            final SteppedValidator range = this.option.getSteppedValidator();
            final int steps = Math.max(1, (range.max() - range.min()) / range.step());
            this.option.modifyValue(Math.min(range.max(), range.min() + (int) Math.round(this.value * steps) * range.step()));
        }
    }

    static final class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        @Nullable
        private final Component header;
        private final List<Option> options = new ArrayList<>(2);
        private final List<AbstractWidget> children = new ArrayList<>(2);
        private final List<Boolean> active = new ArrayList<>(2);

        private Entry() {
            this.header = null;
        }

        private Entry(final Component header) {
            this.header = header;
        }

        private static Entry header(final Component label) {
            return new Entry(label.copy().withStyle(ChatFormatting.BOLD));
        }

        private void add(final Option option, final AbstractWidget child) {
            this.options.add(option);
            this.children.add(child);
            this.active.add(child.active);
            child.setTooltip(Tooltip.create(option.getTooltip()));
        }

        private boolean full() {
            return this.children.size() == 2;
        }

        @Override
        public void render(final GuiGraphics graphics, final int index, final int top, final int left, final int width, final int height,
                           final int mouseX, final int mouseY, final boolean hovering, final float delta) {
            final var font = Minecraft.getInstance().font;
            if (this.header != null) {
                graphics.drawCenteredString(font, this.header, left + width / 2, top + (height - font.lineHeight) / 2, 0xFFFFFFFF);
                return;
            }

            final int childWidth = Math.max(1, (width - CONTROL_GAP) / 2);
            for (int childIndex = 0; childIndex < this.children.size(); childIndex++) {
                final AbstractWidget child = this.children.get(childIndex);
                child.active = this.options.get(childIndex).isEnabled() && this.active.get(childIndex);
                child.setPosition(left + childIndex * (childWidth + CONTROL_GAP), top);
                child.setWidth(childWidth);
                if (child instanceof final IntegerSlider slider) slider.sync();
                child.render(graphics, mouseX, mouseY, delta);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }
}
