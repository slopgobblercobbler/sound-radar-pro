package com.slopgobbler.soundradar.render;

import com.slopgobbler.soundradar.config.SoundRadarConfig;
import com.slopgobbler.soundradar.engine.SoundRadarEngine;
import com.slopgobbler.soundradar.math.RadarVectorMath;
import com.slopgobbler.soundradar.model.AcousticPulse;
import com.slopgobbler.soundradar.model.AudioCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.List;

/**
 * Main HUD rendering coordinator for SoundRadar Pro.
 * Orchestrates circular radar widget and horizontal tactical heading tape.
 */
public class RadarHudOverlay {

    private final SoundRadarConfig config;
    private final SoundRadarEngine engine;
    private final RadarCompassWidget compassWidget;

    public RadarHudOverlay(SoundRadarConfig config, SoundRadarEngine engine) {
        this.config = config != null ? config : SoundRadarConfig.getInstance();
        this.engine = engine != null ? engine : SoundRadarEngine.getInstance();
        this.compassWidget = new RadarCompassWidget(this.config);
    }

    /**
     * HUD rendering entrypoint called on every HUD frame.
     */
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!config.isRadarEnabled() || config.getDisplayMode() == SoundRadarConfig.DisplayMode.OFF) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) {
            return;
        }

        long now = System.currentTimeMillis();
        engine.tick(now);

        List<AcousticPulse> pulses = engine.getActivePulses();

        double camX = client.gameRenderer.getCamera().getPos().x;
        double camY = client.gameRenderer.getCamera().getPos().y;
        double camZ = client.gameRenderer.getCamera().getPos().z;
        float cameraYaw = client.gameRenderer.getCamera().getYaw();

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        // 1. Render Circular Radar Widget if selected
        if (config.getDisplayMode() == SoundRadarConfig.DisplayMode.CIRCULAR ||
            config.getDisplayMode() == SoundRadarConfig.DisplayMode.BOTH) {
            int radius = (int) (config.getRadarRadiusPixels() * config.getRadarScale());
            int padding = 16;
            int cx, cy;

            switch (config.getHudPosition()) {
                case TOP_LEFT -> {
                    cx = radius + padding;
                    cy = radius + padding;
                }
                case BOTTOM_LEFT -> {
                    cx = radius + padding;
                    cy = screenHeight - radius - padding - 10;
                }
                case BOTTOM_RIGHT -> {
                    cx = screenWidth - radius - padding;
                    cy = screenHeight - radius - padding - 10;
                }
                case CENTER_BOTTOM -> {
                    cx = screenWidth / 2;
                    cy = screenHeight - radius - padding - 45;
                }
                case TOP_RIGHT -> {
                    cx = screenWidth - radius - padding;
                    cy = radius + padding;
                }
                default -> {
                    cx = screenWidth - radius - padding;
                    cy = radius + padding;
                }
            }

            compassWidget.render(context, client, cx, cy, pulses, camX, camY, camZ, cameraYaw);
        }

        // 2. Render Tactical Heading Tape (Compass Strip) if selected
        if (config.getDisplayMode() == SoundRadarConfig.DisplayMode.COMPASS_BAR ||
            config.getDisplayMode() == SoundRadarConfig.DisplayMode.BOTH) {
            renderTacticalCompassTape(context, client, screenWidth, pulses, camX, camY, camZ, cameraYaw, now);
        }
    }

    /**
     * Renders horizontal tactical heading tape across the top center of the screen.
     */
    private void renderTacticalCompassTape(DrawContext context, MinecraftClient client,
                                          int screenWidth, List<AcousticPulse> pulses,
                                          double camX, double camY, double camZ,
                                          float cameraYaw, long now) {
        int tapeWidth = 220;
        int tapeHeight = 16;
        int tapeX = (screenWidth - tapeWidth) / 2;
        int tapeY = 12;
        int tapeCenterX = tapeX + (tapeWidth / 2);

        TextRenderer tr = client.textRenderer;

        // Background and border
        context.fill(tapeX, tapeY, tapeX + tapeWidth, tapeY + tapeHeight, 0x800B111A);
        context.drawBorder(tapeX, tapeY, tapeWidth, tapeHeight, 0x4000E5FF);

        // Center reticle notch (▼)
        context.fill(tapeCenterX - 1, tapeY - 2, tapeCenterX + 2, tapeY + 2, 0xFF00E5FF);

        // Visible FOV span across tape
        double fov = 120.0D;

        // Draw compass cardinal / degree tick marks
        drawCompassMark(context, tr, tapeCenterX, tapeY, "N", 180.0, cameraYaw, tapeWidth, fov, 0xFFFF5555);
        drawCompassMark(context, tr, tapeCenterX, tapeY, "NE", 135.0, cameraYaw, tapeWidth, fov, 0xFF888888);
        drawCompassMark(context, tr, tapeCenterX, tapeY, "E", -90.0, cameraYaw, tapeWidth, fov, 0xFFCCCCCC);
        drawCompassMark(context, tr, tapeCenterX, tapeY, "SE", -45.0, cameraYaw, tapeWidth, fov, 0xFF888888);
        drawCompassMark(context, tr, tapeCenterX, tapeY, "S", 0.0, cameraYaw, tapeWidth, fov, 0xFFCCCCCC);
        drawCompassMark(context, tr, tapeCenterX, tapeY, "SW", 45.0, cameraYaw, tapeWidth, fov, 0xFF888888);
        drawCompassMark(context, tr, tapeCenterX, tapeY, "W", 90.0, cameraYaw, tapeWidth, fov, 0xFFCCCCCC);
        drawCompassMark(context, tr, tapeCenterX, tapeY, "NW", -135.0, cameraYaw, tapeWidth, fov, 0xFF888888);

        // Project sound blips onto the horizontal tape
        double maxRange = config.getMaxDetectionRange();
        for (AcousticPulse pulse : pulses) {
            if (pulse.isExpired(now)) continue;

            double dist = RadarVectorMath.distance(camX, camY, camZ, pulse.getX(), pulse.getY(), pulse.getZ());
            if (dist > maxRange) continue;

            double relYaw = RadarVectorMath.calculateRelativeBearing(camX, camZ, pulse.getX(), pulse.getZ(), cameraYaw);
            RadarVectorMath.ScreenPoint sp = RadarVectorMath.calculateCompassTapePosition(relYaw, tapeWidth - 16, fov);

            if (sp.visible()) {
                int blipX = (int) Math.round(tapeCenterX + sp.x());
                int blipY = tapeY + (tapeHeight / 2);

                float alpha = pulse.getAlpha(now);
                if (pulse.getCategory() == AudioCategory.CREEPING) {
                    alpha = RenderColorUtil.calculateStrobe(now, alpha, 250);
                }

                int color = pulse.getCategory().getColorWithAlpha(alpha);

                // Ping marker
                context.fill(blipX - 1, blipY - 3, blipX + 2, blipY + 4, color);

                // Height caret
                int hDiff = RadarVectorMath.getHeightDifferentialIndicator(camY, pulse.getY(), 3.0D);
                if (hDiff > 0) {
                    context.fill(blipX, blipY - 5, blipX + 1, blipY - 3, color);
                } else if (hDiff < 0) {
                    context.fill(blipX, blipY + 4, blipX + 1, blipY + 6, color);
                }
            }
        }
    }

    private void drawCompassMark(DrawContext context, TextRenderer tr, int centerX, int tapeY,
                                 String label, double bearingDeg, float cameraYaw,
                                 int tapeWidth, double fov, int color) {
        double relBearing = RadarVectorMath.wrapDegrees(bearingDeg - cameraYaw);
        RadarVectorMath.ScreenPoint sp = RadarVectorMath.calculateCompassTapePosition(relBearing, tapeWidth - 16, fov);
        if (sp.visible()) {
            int px = (int) Math.round(centerX + sp.x());
            context.fill(px, tapeY + 1, px + 1, tapeY + 4, 0x66FFFFFF);
            int tw = tr.getWidth(label);
            context.drawText(tr, label, px - (tw / 2), tapeY + 5, color, false);
        }
    }
}
