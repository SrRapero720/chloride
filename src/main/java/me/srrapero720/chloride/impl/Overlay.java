package me.srrapero720.chloride.impl;

import me.srrapero720.chloride.Chloride;
import me.srrapero720.chloride.ChlorideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.Tags;

import java.util.Arrays;
import java.util.Locale;

@EventBusSubscriber(modid = Chloride.ID, value = Dist.CLIENT)
public class Overlay {
    private static final FPSDisplayBuilder DISPLAY = new FPSDisplayBuilder();

    private static final Component MSG_FPS = Component.translatable("chloride.overlay.fps");
    private static final Component MSG_MIN = Component.translatable("chloride.overlay.min");
    private static final Component MSG_AVG = Component.translatable("chloride.overlay.avg");
    private static final Component MSG_GPU = Component.translatable("chloride.overlay.gpu");
    private static final Component MSG_MEM = Component.translatable("chloride.overlay.mem");
    private static final Component MSG_SPRINT = Component.translatable("chloride.overlay.sprint");

    // -Xmx NEVER CHANGES AT RUNTIME
    private static final String MEM_TOTAL = " (" + String.format(Locale.ROOT, "%.1f", Runtime.getRuntime().maxMemory() / 1073741824d) + "GB)";

    private static int fps = -1;
    private static int minFPS = -1;
    private static int avgFPS = -1;
    private static int gpuPercent = -1;
    private static int memUsage = -1;

    // BIOME NAME/COLOR ARE CACHED UNTIL THE BIOME CHANGES; TIMESTAMPS DRIVE THE LIGHT-ON-CHANGE HIGHLIGHT
    private static ResourceKey<Biome> biomeKey;
    private static String biomeName = "?";
    private static int biomeColor = 0xFFFFFF;
    private static long biomeChange;
    private static boolean sprinting;
    private static long sprintChange;
    private static float sprintOpacity;

    // AVG
    private static final int[] avgCount = new int[24];
    private static boolean avgFilled = false;
    private static int avgIndex = 0;

    public static void pushAvgFps(final int value) {
        if (avgIndex == avgCount.length) {
            avgIndex = 0;
            avgFilled = true;
        }

        if (!avgFilled) {
            Arrays.fill(avgCount, avgIndex, avgCount.length, value);
        }

        avgCount[avgIndex++] = value;
    }

    public static int calculateAverage() {
        int times = 0;
        for (final int i: avgCount) {
            times += i;
        }

        return times / avgCount.length;
    }

    @SubscribeEvent
    public static void onRenderOverlay(final RenderGuiEvent.Pre event) {
        final var mc = Minecraft.getInstance();

        if (mc.gui.getDebugOverlay().showDebugScreen() || mc.options.hideGui) return;

        // PRECALCULATE
        fps = mc.getFps();
        minFPS = minFPS(mc);
        memUsage = (int) ((ramUsed() * 100) / Runtime.getRuntime().maxMemory());
        gpuPercent = Math.min((int) mc.getGpuUtilization(), 100);
        avgFPS = calculateAverage();
        renderOverlays(mc, event.getGuiGraphics(), mc.font, mc.getWindow().getGuiScale());
    }

    private static void renderOverlays(final Minecraft mc, final GuiGraphics graphics, final Font font, final double guiScale) {
        if (mc.getDebugOverlay().showDebugScreen() || mc.getDebugOverlay().showProfilerChart()) return; // No render when F3 is open

        final var player = mc.player;
        final long now = Util.getMillis();
        final float scale = ChlorideConfig.overlay.scale / 100f;
        // CONFIG MARGINS ARE RAW SCREEN PIXELS; KEEP THEM CONSTANT REGARDLESS OF GUI OR OVERLAY SCALE
        final float pixel = (float) ((guiScale > 0 ? guiScale : 1) * scale);
        final float availW = graphics.guiWidth() / scale;
        final float availH = graphics.guiHeight() / scale;

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1f);

        // FPS AND SYSTEM STATS
        final var fpsCfg = ChlorideConfig.overlay.fps;
        if (!fpsCfg.mode.off() || !fpsCfg.systemDetails.off()) {
            DISPLAY.release();
            switch (fpsCfg.mode) {
                case SIMPLE -> DISPLAY.append(colorByLow(fps)).add(fix(fps)).add(" ").add(MSG_FPS.getString()).add(ChatFormatting.RESET);
                case ADVANCED -> {
                    DISPLAY.append(colorByLow(fps)).add(fix(fps)).add(ChatFormatting.RESET);
                    DISPLAY.append(colorByLow(minFPS)).add(MSG_MIN).add(" ").add(fix(minFPS)).add(ChatFormatting.RESET);
                    DISPLAY.append(colorByLow(avgFPS)).add(MSG_AVG).add(" ").add(fix(avgFPS)).add(ChatFormatting.RESET);
                }
            }
            if (!DISPLAY.isEmpty()) DISPLAY.split();

            // GPU AND RAM, WITH OPTIONAL ACTIVE ENGINE AND JVM TOTAL MEMORY SUFFIXES
            final String engine = fpsCfg.engine ? " (" + GraphicsEngine.active().title() + ")" : "";
            final String memTotal = fpsCfg.totalMemory ? MEM_TOTAL : "";
            switch (fpsCfg.systemDetails) {
                case GPU_ONLY ->
                        DISPLAY.append(colorByPercent(gpuPercent)).add(MSG_GPU).add(" ").add(fix(gpuPercent)).add("%").add(ChatFormatting.RESET).add(engine);
                case RAM_ONLY ->
                        DISPLAY.append(colorByPercent(memUsage)).add(MSG_MEM).add(" ").add(fix(memUsage)).add("%").add(ChatFormatting.RESET).add(memTotal);
                case ALL -> {
                    DISPLAY.append(colorByPercent(gpuPercent)).add(MSG_GPU).add(" ").add(fix(gpuPercent)).add("%").add(ChatFormatting.RESET).add(engine);
                    DISPLAY.append(colorByPercent(memUsage)).add(MSG_MEM).add(" ").add(fix(memUsage)).add("%").add(ChatFormatting.RESET).add(memTotal);
                }
            }

            if (!DISPLAY.isEmpty()) {
                draw(graphics, font, DISPLAY.toString(), argb(0xFFFFFF, 1 - fpsCfg.transparency / 100f),
                        fpsCfg.align, fpsCfg.verticalAlign, fpsCfg.margin / pixel, fpsCfg.verticalMargin / pixel, availW, availH);
            }
        }

        // COORDINATES, ROTATION AND FACING
        final var coords = ChlorideConfig.overlay.coords;
        if (player != null && coords.mode.visible()) {
            DISPLAY.release();
            if (coords.colors) {
                DISPLAY.append(ChatFormatting.RED).add("X ").add(ChatFormatting.RESET).add(Mth.floor(player.getX()));
                DISPLAY.append(ChatFormatting.GREEN).add("Y ").add(ChatFormatting.RESET).add(Mth.floor(player.getY()));
                DISPLAY.append(ChatFormatting.BLUE).add("Z ").add(ChatFormatting.RESET).add(Mth.floor(player.getZ()));
            } else {
                DISPLAY.append("X ").add(Mth.floor(player.getX()));
                DISPLAY.append("Y ").add(Mth.floor(player.getY()));
                DISPLAY.append("Z ").add(Mth.floor(player.getZ()));
            }
            final String pos = DISPLAY.toString();

            // ROTATION AND CARDINAL FACING AS "X 0° | Y 0° | NORTH", SHARING THE PURPLE ACCENT
            String facing = "";
            if (coords.mode.extended()) {
                DISPLAY.release();
                if (coords.mode.rotation()) {
                    if (coords.colors) {
                        DISPLAY.append(ChatFormatting.LIGHT_PURPLE).add("X ").add(ChatFormatting.RESET).add(Math.round(Mth.wrapDegrees(player.getYRot()))).add("°");
                        DISPLAY.append(ChatFormatting.LIGHT_PURPLE).add("Y ").add(ChatFormatting.RESET).add(Math.round(player.getXRot())).add("°");
                    } else {
                        DISPLAY.append("X ").add(Math.round(Mth.wrapDegrees(player.getYRot()))).add("°");
                        DISPLAY.append("Y ").add(Math.round(player.getXRot())).add("°");
                    }
                }
                if (coords.mode.facing()) {
                    final String direction = player.getDirection().getName().toUpperCase(Locale.ROOT);
                    if (coords.colors) {
                        DISPLAY.append(ChatFormatting.LIGHT_PURPLE).add(direction).add(ChatFormatting.RESET);
                    } else {
                        DISPLAY.append(direction);
                    }
                }
                facing = DISPLAY.toString();
            }

            final float marginX = coords.margin / pixel;
            final float marginY = coords.verticalMargin / pixel;
            final int posArgb = argb(0xFFFFFF, 1 - coords.transparency / 100f);
            final int facingArgb = argb(0xFFFFFF, 1 - coords.rotationTransparency / 100f);

            if (facing.isEmpty()) {
                draw(graphics, font, pos, posArgb, coords.align, coords.verticalAlign, marginX, marginY, availW, availH);
            } else if (coords.rotationNewLine) {
                // TWO INDEPENDENTLY-ALIGNED LINES SHARING ONE BLOCK POSITION
                final int step = font.lineHeight + 3;
                final float y = posY(step + font.lineHeight, coords.verticalAlign, marginY, availH);
                drawAt(graphics, font, pos, posArgb, posX(font.width(pos), coords.align, marginX, availW), y);
                drawAt(graphics, font, facing, facingArgb, posX(font.width(facing), coords.align, marginX, availW), y + step);
            } else {
                // SINGLE LINE; THE ROTATION SEGMENT KEEPS ITS OWN TRANSPARENCY
                final String sep = " - ";
                final float posW = font.width(pos);
                final float x = posX(posW + font.width(sep) + font.width(facing), coords.align, marginX, availW);
                final float y = posY(font.lineHeight, coords.verticalAlign, marginY, availH);
                drawAt(graphics, font, pos, posArgb, x, y);
                drawAt(graphics, font, sep + facing, facingArgb, x + posW, y);
            }
        }

        // BIOME NAME, NO LABEL
        final var biome = ChlorideConfig.overlay.biome;
        if (player != null && biome.enabled && mc.level != null) {
            final Holder<Biome> holder = mc.level.getBiome(player.blockPosition());
            final ResourceKey<Biome> key = holder.unwrapKey().orElse(null);
            if (key != biomeKey) {
                biomeKey = key;
                biomeName = (key == null) ? "?" : Component.translatable(key.location().toLanguageKey("biome")).getString();
                biomeColor = biomeColor(holder, key);
                biomeChange = now;
            }
            final int color = biome.colorByBiome ? biomeColor : 0xFFFFFF;
            draw(graphics, font, biomeName, argb(color, opacity(biome.transparency, biome.lightOnChange, biomeChange, now)),
                    biome.align, biome.verticalAlign, biome.margin / pixel, biome.verticalMargin / pixel, availW, availH);
        }

        // RUNNING STATE: FADE-IN ON START, BRIGHT -> LIGHT WHILE SPRINTING, FADE-OUT ON STOP
        final var running = ChlorideConfig.overlay.running;
        if (player != null && running.enabled) {
            if (player.isSprinting() != sprinting) {
                sprinting = player.isSprinting();
                sprintChange = now;
            }
            if (sprinting) {
                sprintOpacity = opacity(running.transparency, running.lightOnChange, sprintChange, now) * easeOut((now - sprintChange) / 250f);
                draw(graphics, font, MSG_SPRINT.getString(), argb(0x55FF55, sprintOpacity),
                        running.align, running.verticalAlign, running.margin / pixel, running.verticalMargin / pixel, availW, availH);
            } else if (now - sprintChange < 750) {
                // FADE-OUT FROM THE LAST VISIBLE OPACITY
                final float fade = sprintOpacity * (1 - easeOut((now - sprintChange) / 750f));
                if (fade > 0.02f) {
                    draw(graphics, font, MSG_SPRINT.getString(), argb(0x55FF55, fade),
                            running.align, running.verticalAlign, running.margin / pixel, running.verticalMargin / pixel, availW, availH);
                }
            }
        }

        DISPLAY.release();
        graphics.pose().popPose();
    }

    // SHARED SINGLE-LINE DRAW FOR THE SIMPLE OVERLAYS
    private static void draw(final GuiGraphics graphics, final Font font, final String text, final int argb,
                             final FPSAlign align, final FPSVAlign verticalAlign, final float marginX, final float marginY,
                             final float availW, final float availH) {
        drawAt(graphics, font, text, argb, posX(font.width(text), align, marginX, availW), posY(font.lineHeight, verticalAlign, marginY, availH));
    }

    private static void drawAt(final GuiGraphics graphics, final Font font, final String text, final int argb, final float x, final float y) {
        if (ChlorideConfig.overlay.contrast == Contrast.BACKGROUND) {
            // BOX ALPHA FOLLOWS THE TEXT ALPHA, SCALED TO THE F3-STYLE BASE (0x90)
            final int box = ((argb >>> 24) * 144 / 255) << 24 | 0x505050;
            graphics.fill((int) x - 2, (int) y - 2, (int) (x + font.width(text)) + 2, (int) y + font.lineHeight + 1, box);
            graphics.flush();
        }

        graphics.drawString(font, text, x, y, argb, ChlorideConfig.overlay.contrast != Contrast.NONE);
    }

    private static float posX(final float width, final FPSAlign align, final float margin, final float availW) {
        return switch (align) {
            case LEFT -> margin;
            case CENTER -> (availW - width) / 2;
            case RIGHT -> availW - width - margin;
        };
    }

    private static float posY(final float height, final FPSVAlign align, final float margin, final float availH) {
        return switch (align) {
            case TOP -> margin;
            case CENTER -> (availH - height) / 2;
            case BOTTOM -> availH - height - margin;
        };
    }

    // LIGHT-ON-CHANGE OPACITY: FULL FOR 3s AFTER A CHANGE, THEN A 0.75s EASE-OUT BACK TO THE CONFIGURED ONE
    private static float opacity(final int transparency, final boolean lightOnChange, final long changed, final long now) {
        final float base = 1 - transparency / 100f;
        if (!lightOnChange) return base;

        final long dt = now - changed;
        if (dt < 3000) return 1;
        if (dt >= 3750) return base;
        return 1 - (1 - base) * easeOut((dt - 3000) / 750f);
    }

    // QUADRATIC EASE-OUT, CLAMPED TO [0..1]
    private static float easeOut(final float progress) {
        final float inverse = 1 - Math.min(progress, 1);
        return 1 - inverse * inverse;
    }

    private static int argb(final int rgb, final float opacity) {
        return ((int) (opacity * 255) << 24) | rgb;
    }

    // BIOME CATEGORY COLORS: SPECIFIC BIOMES FIRST (NETHER/CHERRY/PALE/MUSHROOM), THEN WATER/WET/COLD/HOT TAGS
    private static int biomeColor(final Holder<Biome> biome, final ResourceKey<Biome> key) {
        if (biome.is(BiomeTags.IS_NETHER)) return 0xFF5533;
        if (key == Biomes.CHERRY_GROVE) return 0xFFB7C5;
        // if (key != null && key.location().getPath().equals("pale_garden")) return 0xAAAAAA; // RESERVED FOR PALE GARDEN 1.21.4+
        if (biome.is(Tags.Biomes.IS_MUSHROOM)) return 0x9B6F47;
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_RIVER)) return 0x4169E1;
        if (biome.is(BiomeTags.IS_JUNGLE) || biome.is(Tags.Biomes.IS_SWAMP)) return 0x00AA00;
        if (biome.is(Tags.Biomes.IS_COLD) || biome.is(Tags.Biomes.IS_ICY) || biome.is(Tags.Biomes.IS_SNOWY)) return 0x55FFFF;
        if (biome.is(Tags.Biomes.IS_HOT)) return 0xFFAA00;
        if (biome.is(Tags.Biomes.IS_PLAINS) || biome.is(BiomeTags.IS_FOREST)) return 0x55FF55;
        return 0xFFFFFF;
    }

    private static String fix(final int value) {
        return (value == -1) ? "--" : "" + value;
    }

    private static int minFPS(final Minecraft mc) {

        final LocalSampleLogger timer = mc.getDebugOverlay().frameTimeLogger;
        final int size = timer.size();

        if (size == 0) return minFPS;

        int fps = mc.getFps();
        if (fps <= 0) fps = 1;

        long maxNS = (long) (1 / (double) fps * 1000000000);
        long totalNS = 0;

        for (int i = size - 1; i >= 0 && totalNS < 1_000_000_000L; i--) {
            final long timeNs = timer.get(i, 0); // dim 0 = tiempo total del frame
            if (timeNs > maxNS) maxNS = timeNs;
            totalNS += timeNs;
        }

        return (int) (1 / ((double) maxNS / 1000000000));
    }

    public static ChatFormatting colorByLow(final int usage) {
        return ((usage < 9) ? ChatFormatting.DARK_RED
                : (usage < 16) ? ChatFormatting.RED
                : (usage < 30) ? ChatFormatting.GOLD
                : ChatFormatting.RESET);
    }

    public static ChatFormatting colorByPercent(final int usage) {
        return ((usage >= 100) ? ChatFormatting.DARK_RED
                : (usage >= 90) ? ChatFormatting.RED
                : (usage >= 75) ? ChatFormatting.GOLD
                : ChatFormatting.RESET);
    }

    // JAVA USES STUPID AND CONFUSING NAMES
    // max memory is the assigned memory (ej: -Xmx8G)
    // total memory is the allocated memory (normally isn't much)
    // used memory needs to be calculated using total memory - free memory, same with percent
    public static long ramUsed() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public static long bytesToMB(final long input) {
        return input / 1024 / 1024;
    }

    /* CONFIG VALUES */
    public enum FPS {
        OFF, SIMPLE, ADVANCED;

        public boolean off() {
            return this == OFF;
        }
    }

    public enum FPSAlign { LEFT, CENTER, RIGHT}

    public enum FPSVAlign { TOP, CENTER, BOTTOM}

    public enum Contrast { NONE, SHADOW, BACKGROUND }

    public enum CoordsMode {
        OFF, SIMPLE, ROTATION, FACING, ALL;

        public boolean visible() { return this != OFF; }
        public boolean rotation() { return this == ROTATION || this == ALL; }
        public boolean facing() { return this == FACING || this == ALL; }
        public boolean extended() { return this != OFF && this != SIMPLE; }
    }

    public enum FPSDetails {
        OFF, ALL, GPU_ONLY, RAM_ONLY;

        public boolean ram() { return this == RAM_ONLY || this == ALL; }
        public boolean gpu() { return this == GPU_ONLY || this == ALL; }
        public boolean off() { return this == OFF; }
    }

    private static class FPSDisplayBuilder {
        private StringBuilder builder = new StringBuilder();
        private boolean split = false;
        private boolean divisor = false;

        public FPSDisplayBuilder append(final String param) {
            if (this.split) this.builder.append(" - ");
            if (this.divisor) this.builder.append(" | ");
            this.builder.append(param);

            this.split = false;
            this.divisor = true;
            return this;
        }

        public FPSDisplayBuilder append(final ChatFormatting formatting) {
            return this.append(formatting.toString());
        }

        public FPSDisplayBuilder add(final int param) {
            this.builder.append(param);
            return this;
        }

        public FPSDisplayBuilder add(final String param) {
            this.builder.append(param);
            return this;
        }

        public FPSDisplayBuilder add(final Component component) {
            return this.add(component.getString());
        }

        public FPSDisplayBuilder add(final ChatFormatting formatting) {
            return this.add(formatting.toString());
        }

        public void split() {
            this.split = true;
            this.divisor = false;
        }

        public boolean isEmpty() {
            return this.builder.isEmpty();
        }

        public void release() {
            this.builder = new StringBuilder();
            this.split = false;
            this.divisor = false;
        }

        @Override
        public String toString() {
            return this.builder.toString();
        }
    }
}
