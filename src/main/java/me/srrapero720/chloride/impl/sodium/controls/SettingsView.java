package me.srrapero720.chloride.impl.sodium.controls;

import me.srrapero720.chloride.impl.sodium.screens.SaltySettingsScreen;
import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.config.structure.Option;
import net.caffeinemc.mods.sodium.client.config.structure.Page;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

/** Option body shared by the Sodium-like and vanilla settings screen designs. */
public interface SettingsView {
    void showPage(ModOptions mod, Page page);

    void showPageResults(ModOptions mod, Page page, List<Option> options);

    void showResults(List<SaltySettingsScreen.Section> sections);

    boolean dropFiles(List<Path> paths);

    @Nullable
    SaltySettingsScreen.Tip tooltipAt(int mouseX, int mouseY);
}
