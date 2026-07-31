package me.srrapero720.chloride.impl.sodium.controls;

import me.srrapero720.chloride.Chloride;
import net.caffeinemc.mods.sodium.client.gui.Colors;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;

/** Empty square action slot reserved for a future option action icon. */
public class OptionActionButton extends AbstractWidget {
    public enum Type {
        UNDO(Chloride.id("textures/gui/undo.png")), RESET(Chloride.id("textures/gui/reset.png"));

        private final ResourceLocation icon;
        Type(final ResourceLocation icon) {
            this.icon = icon;
        }
    }

    private final AbstractWidget parent;
    private final Type type;
    private final BooleanSupplier shown;
    private final Runnable action;

    public OptionActionButton(final Dim2i dim, final Type type, final BooleanSupplier shown, final Runnable action) {
        super(square(dim));
        this.parent = null;
        this.type = type;
        this.shown = shown;
        this.action = action;
    }

    /** Creates a square action button in a right-aligned slot of {@code parent}. */
    public OptionActionButton(final AbstractWidget parent, final Type type, final BooleanSupplier shown, final Runnable action) {
        super(new Dim2i(0, 0, parent.getHeight(), parent.getHeight()));
        this.parent = parent;
        this.type = type;
        this.shown = shown;
        this.action = action;
    }

    private static Dim2i square(final Dim2i dim) {
        if (dim.width() != dim.height()) throw new IllegalArgumentException("Option action buttons must be square");
        return dim;
    }

    @Override
    public int getX() {
        return this.parent == null ? super.getX() : this.parent.getLimitX() - (2 - this.type.ordinal()) * this.getWidth();
    }

    @Override
    public int getY() {
        return this.parent == null ? super.getY() : this.parent.getY();
    }

    public boolean shown() {
        return this.shown.getAsBoolean();
    }

    public Type type() {
        return this.type;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        if (!this.shown()) return;
        this.hovered = this.isMouseOver(mouseX, mouseY);
        this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), this.hovered ? Colors.BACKGROUND_HOVER : Colors.BACKGROUND_MEDIUM);
        VideoSettingsScreen.renderIconWithSpacing(graphics, this.type.icon, 0xffffffff, false, this.getX(), this.getY(), this.getHeight(), 2);
        if (this.isFocused()) {
            this.drawBorder(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), Colors.BUTTON_BORDER);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button != 0 || !this.shown() || !this.isMouseOver(mouseX, mouseY)) return false;
        this.activate();
        return true;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (!this.isFocused() || !this.shown() || !CommonInputs.selected(keyCode)) return false;
        this.activate();
        return true;
    }

    @Override
    public ComponentPath nextFocusPath(final FocusNavigationEvent event) {
        return this.shown() ? super.nextFocusPath(event) : null;
    }

    private void activate() {
        this.action.run();
        this.playClickSound();
    }
}
