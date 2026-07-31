package me.srrapero720.chloride.api.config;

import net.minecraft.network.chat.Component;

/** Builder of an option that runs something when clicked, drawn as a button on the option field. */
public interface ActionOptionBuilder extends SaltyOptionBuilder<ActionOptionBuilder> {
    /** Sets the text shown on the button. Defaults to Sodium's "Open >" label. */
    ActionOptionBuilder setLabel(Component label);

    /** Sets what the button does. Required. */
    ActionOptionBuilder setAction(Runnable action);
}
