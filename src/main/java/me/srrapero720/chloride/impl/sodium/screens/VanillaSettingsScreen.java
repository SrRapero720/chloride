package me.srrapero720.chloride.impl.sodium.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import me.srrapero720.chloride.impl.SettingsScreens;
import me.srrapero720.chloride.impl.sodium.controls.VanillaSettingsList;
import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.config.structure.Page;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/** Vanilla video settings design with world-creation tabs and standard Minecraft controls. */
public final class VanillaSettingsScreen extends SaltySettingsScreen {
    private static final int TAB_HEIGHT = 24;
    private static final int TOOLBAR_HEIGHT = 24;
    private static final int FOOTER_HEIGHT = 32;
    private static final int GAP = 4;

    private TabNavigationBar tabs;
    private EditBox search;
    private Button undo;
    private Button apply;
    private Button done;

    public VanillaSettingsScreen(final Screen prev) {
        super(Component.translatable("chloride.settings.vanilla"), SettingsScreens.Style.VANILLA, prev);
    }

    @Override
    protected void initDimensions() {
        this.dimensions(new Dim2i(0, 0, this.width, this.height));
    }

    @Override
    protected int addHeader() {
        final List<Page> pages = this.mod().pages();
        final TabManager manager = new TabManager(this::addRenderableWidget, this::removeWidget) {
            @Override
            public void setCurrentTab(final Tab tab, final boolean sound) {
                super.setCurrentTab(tab, sound);
                if (!(tab instanceof PageTab selected) || !VanillaSettingsScreen.this.selectPage(
                        VanillaSettingsScreen.this.mod(), selected.page())) return;
                VanillaSettingsScreen.this.clearVanillaSearch();
                VanillaSettingsScreen.this.requestRelayout();
            }
        };
        final PageTab[] pageTabs = pages.stream().map(PageTab::new).toArray(PageTab[]::new);
        this.tabs = TabNavigationBar.builder(manager, this.width).addTabs(pageTabs).build();
        this.tabs.arrangeElements();
        this.addRenderableWidget(this.tabs);
        this.tabs.selectTab(Math.max(0, pages.indexOf(this.page())), false);
        return TAB_HEIGHT;
    }

    @Override
    protected int addFooter() {
        final int y = this.height - FOOTER_HEIGHT + 6;
        final int count = this.minecraft.level == null ? 3 : 4;
        final int width = Math.min(100, Math.max(1, (this.width - GAP * (count - 1) - 20) / count));
        int x = (this.width - width * count - GAP * (count - 1)) / 2;

        if (this.minecraft.level != null) {
            this.addRenderableWidget(Button.builder(Component.translatable("chloride.settings.hide_ui"), button -> this.startPeek())
                    .bounds(x, y, width, 20).build());
            x += width + GAP;
        }
        this.undo = this.addRenderableWidget(Button.builder(Component.translatable("chloride.settings.undo"), button -> this.undo())
                .bounds(x, y, width, 20).build());
        x += width + GAP;
        this.apply = this.addRenderableWidget(Button.builder(Component.translatable("chloride.settings.apply"), button -> this.apply())
                .bounds(x, y, width, 20).build());
        x += width + GAP;
        this.done = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(x, y, width, 20).build());
        return this.height - FOOTER_HEIGHT;
    }

    @Override
    protected void initContent(final Dim2i area) {
        final int toolbarWidth = Math.min(630, Math.max(1, area.width() - 20));
        final int x = area.x() + (area.width() - toolbarWidth) / 2;
        final int styleWidth = Math.min(110, Math.max(70, toolbarWidth / 5));
        final int modWidth = Math.min(170, Math.max(90, toolbarWidth / 3));
        final int searchWidth = Math.max(1, toolbarWidth - styleWidth - modWidth - GAP * 2);

        final Button style = this.addRenderableWidget(Button.builder(Component.translatable("chloride.settings.style"),
                button -> SettingsScreens.next(SettingsScreens.Style.VANILLA, this.previousScreen()))
                .bounds(x, area.y() + 2, styleWidth, 20).build());
        style.setTooltip(Tooltip.create(Component.translatable("chloride.settings.design", SettingsScreens.Style.VANILLA.title())));

        final CycleButton<ModOptions> mods = CycleButton.<ModOptions>builder(mod -> Component.literal(mod.name()))
                .withValues(this.mods())
                .withInitialValue(this.mod())
                .displayOnlyValue()
                .create(x + styleWidth + GAP, area.y() + 2, modWidth, 20, Component.translatable("chloride.settings.mod"),
                        (button, mod) -> {
                            if (!this.selectPage(mod, this.firstPage(mod))) return;
                            this.clearVanillaSearch();
                            this.requestRelayout();
                        });
        this.addRenderableWidget(mods);
        mods.setTooltip(Tooltip.create(Component.translatable("chloride.settings.mod")));

        this.search = new EditBox(this.font, x + styleWidth + modWidth + GAP * 2, area.y() + 2, searchWidth, 20,
                Component.translatable("chloride.settings.search"));
        this.search.setHint(Component.translatable("chloride.settings.search").withStyle(ChatFormatting.GRAY));
        this.search.setValue(this.query());
        this.search.setResponder(this::onQuery);
        this.addRenderableWidget(this.search);

        final int listY = area.y() + TOOLBAR_HEIGHT;
        final VanillaSettingsList list = this.addRenderableWidget(new VanillaSettingsList(this,
                new Dim2i(area.x(), listY, area.width(), Math.max(0, area.getLimitY() - listY))));
        this.setSettingsView(list);
    }

    @Override
    protected void updateActionState(final boolean pending) {
        if (this.undo != null) this.undo.active = pending;
        if (this.apply != null) this.apply.active = pending;
        if (this.done != null) this.done.active = !pending;
    }

    @Override
    protected boolean searchFocused() {
        return this.search != null && this.search.isFocused();
    }

    @Override
    protected void focusSearch() {
        if (this.search != null) this.setFocused(this.search);
    }

    @Override
    protected boolean handleSearchKey(final int keyCode, final int scanCode, final int modifiers) {
        if (keyCode != InputConstants.KEY_ESCAPE || this.search == null || this.search.getValue().isEmpty()) return false;
        this.search.setValue("");
        return true;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        return this.tabs != null && this.tabs.keyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderLayout(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        RenderSystem.enableBlend();
        graphics.blit(this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR,
                0, this.height - FOOTER_HEIGHT - 2, 0.0f, 0.0f, this.width, 2, 32, 2);
        RenderSystem.disableBlend();
    }

    private void clearVanillaSearch() {
        if (this.search != null && !this.search.getValue().isEmpty()) this.search.setValue("");
    }

    private record PageTab(Page page) implements Tab {
        @Override
        public Component getTabTitle() {
            return SaltySettingsScreen.pageLabel(this.page);
        }

        @Override
        public void visitChildren(final Consumer<AbstractWidget> consumer) {}

        @Override
        public void doLayout(final ScreenRectangle rectangle) {}
    }
}
