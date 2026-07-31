package me.srrapero720.chloride.mixins;

import me.srrapero720.chloride.Chloride;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(final String s) {

    }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        // FIRST PARAM IS THE TARGET (mezz.jei...), THE MOD-PRESENCE GATE MUST CHECK THE MIXIN CLASS NAME
        if (mixinClassName.contains("iris.") && !Chloride.installed("iris")) return false;
        if (mixinClassName.contains("jei_rei_emi.Jei") && !Chloride.installed("jei")) return false;
        if (mixinClassName.contains("jei_rei_emi.Rei") && !Chloride.installed("roughlyenoughitems")) return false;
        return !mixinClassName.contains("jei_rei_emi.Emi") || Chloride.installed("emi");

        // go ahead
    }

    @Override
    public void acceptTargets(final Set<String> set, final Set<String> set1) {}

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(final String s, final ClassNode classNode, final String s1, final IMixinInfo iMixinInfo) {}

    @Override
    public void postApply(final String s, final ClassNode classNode, final String s1, final IMixinInfo iMixinInfo) {}
}
