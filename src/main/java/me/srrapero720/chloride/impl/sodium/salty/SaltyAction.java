package me.srrapero720.chloride.impl.sodium.salty;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/** Option that runs something instead of holding a value, drawn as a button on the option field. */
public final class SaltyAction extends SaltyEntry {
    private final Component label;
    private final Runnable action;

    SaltyAction(final ResourceLocation id, @Nullable final Consumer<List<Path>> fileDrop, final Component label, final Runnable action) {
        super(id, fileDrop);
        this.label = label;
        this.action = action;
    }

    public Component label() {
        return this.label;
    }

    public void run() {
        this.action.run();
    }
}
