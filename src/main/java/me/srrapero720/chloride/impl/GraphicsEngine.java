package me.srrapero720.chloride.impl;

public enum GraphicsEngine {
    OPENGL("OpenGL"),
    VULKAN("Vulkan"); // RESERVED FOR 26.2

    private final String title;

    GraphicsEngine(final String title) {
        this.title = title;
    }

    public String title() {
        return this.title;
    }

    // 1.21.1 ONLY RENDERS THROUGH OPENGL; THE 26.2 BRANCH REPLACES THIS BODY WITH THE HOTSWAP STATE
    public static GraphicsEngine active() {
        return OPENGL;
    }
}
