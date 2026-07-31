package me.srrapero720.chloride.impl.sodium.controls;

import com.mojang.blaze3d.platform.InputConstants;
import net.caffeinemc.mods.sodium.client.gui.ButtonTheme;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractParentWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.function.Consumer;

/**
 * Search bar built like Sodium's: an unbordered edit box over a translucent fill, with a clear button that
 * only shows up while there is a query. Every text change is pushed to the given consumer.
 */
public class EnhancedSearchBar extends AbstractParentWidget {
    public static final int MAX_QUERY_LENGTH = 200;
    private static final ButtonTheme CLEAR_THEME = new ButtonTheme(Colors.FOREGROUND, Colors.FOREGROUND, Colors.FOREGROUND_DISABLED,
            Colors.BACKGROUND_MEDIUM, Colors.BACKGROUND_LIGHT, Colors.BACKGROUND_LIGHT);

    private final EditBox box;
    private final FlatButtonWidget clear;

    public EnhancedSearchBar(final Dim2i dim, final String query, final Consumer<String> onQuery) {
        super(dim);

        this.clear = new FlatButtonWidget(new Dim2i(dim.getLimitX() - Layout.BUTTON_SHORT, dim.y(), Layout.BUTTON_SHORT, Layout.BUTTON_SHORT),
                Component.literal("×"), this::clear, true, false, CLEAR_THEME);
        this.box = new EditBox(this.font, dim.x() + Layout.INNER_MARGIN, dim.getCenterY() - this.font.lineHeight / 2,
                dim.width() - Layout.BUTTON_SHORT * 2, Layout.BUTTON_SHORT, Component.translatable("sodium.options.search"));
        this.box.setMaxLength(MAX_QUERY_LENGTH);
        this.box.setBordered(false);
        this.box.setHint(Component.translatable("sodium.options.search.hint").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        // SET BEFORE THE RESPONDER SO RESTORING THE QUERY ACROSS RELAYOUTS DOES NOT RETRIGGER A SEARCH
        this.box.setValue(query);
        this.box.setResponder(text -> {
            this.clear.setVisible(!text.isEmpty());
            onQuery.accept(text.stripLeading());
        });
        this.clear.setVisible(!query.isEmpty());

        this.addRenderableChild(this.box);
        this.addRenderableChild(this.clear);
    }

    /** Empties the bar, which pushes an empty query through the responder. */
    public void clear() {
        if (!this.box.getValue().isEmpty()) this.box.setValue("");
    }

    /** True while the edit box holds the focus, so a relayout can hand it back afterwards. */
    public boolean typing() {
        return this.box.isFocused();
    }

    public EditBox box() {
        return this.box;
    }

    public FlatButtonWidget clearButton() {
        return this.clear;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), Colors.BACKGROUND_DEFAULT);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        // ESCAPE DROPS THE QUERY INSTEAD OF CLOSING THE SCREEN, BUT ONLY WHILE THE BOX IS BEING TYPED INTO
        if (keyCode == InputConstants.KEY_ESCAPE && this.getFocused() == this.box && !this.box.getValue().isEmpty()) {
            this.clear();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(final char codePoint, final int modifiers) {
        return this.box.charTyped(codePoint, modifiers);
    }

    @Override
    public void setFocused(final boolean focused) {
        super.setFocused(focused);
        // THE BOX KEEPS ITS BLINKING CURSOR UNLESS THE FOCUS IS HANDED OVER TO IT AND TAKEN BACK EXPLICITLY
        this.setFocused(focused ? this.box : null);
    }
}
