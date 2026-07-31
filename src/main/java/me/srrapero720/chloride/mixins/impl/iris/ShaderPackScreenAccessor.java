package me.srrapero720.chloride.mixins.impl.iris;

import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the flag that tells Iris' screen to show the options of the active pack instead of the pack list.
 * Iris only toggles it from its own button, and there is no API to open that view directly.
 */
@Mixin(ShaderPackScreen.class)
public interface ShaderPackScreenAccessor {
    @Accessor("optionMenuOpen")
    void chloride$optionMenuOpen(boolean open);
}
