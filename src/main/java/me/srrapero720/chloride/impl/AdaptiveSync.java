package me.srrapero720.chloride.impl;

import me.srrapero720.chloride.ChlorideConfig;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AdaptiveSync {
    // 0 = UNTESTED, 1 = SUPPORTED, -1 = UNSUPPORTED; QUERIED LAZILY BECAUSE GLFW NEEDS A LIVE GL CONTEXT
    private static volatile int supported = 0;

    public static boolean supported() {
        int state = supported;
        if (state == 0) {
            supported = state = (GLFW.glfwExtensionSupported("WGL_EXT_swap_control_tear") || GLFW.glfwExtensionSupported("GLX_EXT_swap_control_tear")) ? 1 : -1;
        }
        return state == 1;
    }

    public static Mode mode() {
        if (!Minecraft.getInstance().options.enableVsync().get()) return Mode.OFF;
        return ChlorideConfig.fullscreen.adaptiveSync ? Mode.ADAPTIVE : Mode.ON;
    }

    public static void mode(final Mode mode) {
        final Minecraft mc = Minecraft.getInstance();
        ChlorideConfig.fullscreen.adaptiveSync = mode == Mode.ADAPTIVE;

        // THE VANILLA OPTION ONLY FIRES ITS CALLBACK ON VALUE CHANGES; RE-APPLY MANUALLY SO SWITCHING
        // ON <-> ADAPTIVE (BOTH VSYNC=TRUE) STILL REACHES Window#updateVsync AND THE SWAP-INTERVAL HOOK
        mc.options.enableVsync().set(mode != Mode.OFF);
        mc.getWindow().updateVsync(mode != Mode.OFF);
    }

    public enum Mode { OFF, ON, ADAPTIVE }
}
