package com.slopgobbler.soundradar.render;

import com.slopgobbler.soundradar.config.SoundRadarConfig;
import com.slopgobbler.soundradar.math.RadarVectorMath;
import com.slopgobbler.soundradar.model.AcousticPulse;
import com.slopgobbler.soundradar.model.AudioCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/**
 * Tactical circular polar radar compass HUD widget.
 * Displays concentric distance range rings, dynamically rotating cardinal bearings,
 * player heading, classified audio blips, and elevation chevrons.
 */
public class RadarCompassWidget {

    private final SoundRadarConfig config;

    public RadarCompassWidget(SoundRadarConfig config) {
        this.config = config != null ? config : SoundRadarConfig.getInstance();
    }

    /**
     * Renders the circular radar HUD widget.
     *
     * @param context DrawContext for 2D rendering
     * @param client MinecraftClient instance
     * @param centerX widget center X on screen
     * @param centerY widget center Y on screen
     * @param pulses active acoustic pulses
     * @param camX camera eye X
     * @param camY camera eye Y
     * @param camZ camera eye Z
     * @param cameraYaw camera yaw
     */
    public void render(DrawContext context, MinecraftClient client,
                       int centerX, int centerY,
                       List<AcousticPulse> pulses,
                       double camX, double camY, double camZ,
                       float cameraYaw) {
        int radius = (int) (config.getRadarRadiusPixels() * config.getRadarScale());
        TextRenderer textRenderer = client.textRenderer;
        long now = System.currentTimeMillis();

        // 1. Draw radar disc background
        drawCircularBackground(context, centerX, centerY, radius);

        // 2. Draw concentric distance rings (33%, 66%, 100%)
        drawConcentricRing(context, centerX, centerY, (int) (radius * 0.33), 0x2500E5FF);
        drawConcentricRing(context, centerX, centerY, (int) (radius * 0.66), 0x3000E5FF);
        drawConcentricRing(context, centerX, centerY, radius, 0x6000E5FF);

        // 3. Draw crosshairs
        context.drawHorizontalLine(centerX - radius + 4, centerX + radius - 4, centerY, 0x3300E5FF);
        context.drawVerticalLine(centerX, centerY - radius + 4, centerY + radius - 4, 0x3300E5FF);

        // 4. Draw rotating cardinal markers (N, E, S, W) on radar rim
        if (config.isShowCompassCardinals()) {
            drawCardinalMarkers(context, textRenderer, centerX, centerY, radius - 6, cameraYaw);
        }

        // 5. Draw center player origin blip and forward view chevron
        context.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, 0xFFFFFFFF);
        context.fill(centerX, centerY - 4, centerX + 1, centerY - 1, 0xFF00E5FF); // Forward notch

        // 6. Draw active acoustic pulses
        double maxRange = config.getMaxDetectionRange();
        AcousticPulse closestAlertPulse = null;
        double minDistance = Double.MAX_VALUE;

        for (AcousticPulse pulse : pulses) {
            if (pulse.isExpired(now)) continue;

            double dist = RadarVectorMath.distance(camX, camY, camZ, pulse.getX(), pulse.getY(), pulse.getZ());
            if (dist > maxRange) continue;

            double relYaw = RadarVectorMath.calculateRelativeBearing(camX, camZ, pulse.getX(), pulse.getZ(), cameraYaw);
            RadarVectorMath.ScreenPoint sp = RadarVectorMath.calculatePolarRadarPosition(relYaw, dist, maxRange, radius - 4);

            int px = (int) Math.round(centerX + sp.x());
            int py = (int) Math.round(centerY + sp.y());

            float baseAlpha = pulse.getAlpha(now);
            if (pulse.getCategory() == AudioCategory.CREEPING) {
                baseAlpha = RenderColorUtil.calculateStrobe(now, baseAlpha, 300);
            }

            int blipColor = pulse.getCategory().getColorWithAlpha(baseAlpha);
            int glowColor = RenderColorUtil.applyAlpha(pulse.getCategory().getGlowColorArgb(), baseAlpha * 0.8f);

            // Draw blip halo / glow
            context.fill(px - 2, py - 2, px + 3, py + 3, glowColor);
            // Draw core blip
            context.fill(px - 1, py - 1, px + 2, py + 2, blipColor);

            // Draw height differential chevrons
            if (config.isShowHeightChevrons()) {
                int heightDiff = RadarVectorMath.getHeightDifferentialIndicator(camY, pulse.getY(), 3.0D);
                if (heightDiff > 0) {
                    // Elevated sound: upward caret
                    context.fill(px, py - 3, px + 1, py - 2, blipColor);
                } else if (heightDiff < 0) {
                    // Lowered sound: downward caret
                    context.fill(px, py + 2, px + 1, py + 3, blipColor);
                }
            }

            // Track closest high threat for tactical label
            if (pulse.getCategory().getPriority() >= AudioCategory.HOSTILE.getPriority() && dist < minDistance) {
                minDistance = dist;
                closestAlertPulse = pulse;
            }
        }

        // 7. Draw tactical range and status label at base of radar
        if (config.isShowDistanceLabels() && closestAlertPulse != null) {
            String alertText = String.format("%s %.0fm", closestAlertPulse.getCategory().getIconGlyph(), minDistance);
            int alertColor = closestAlertPulse.getCategory().getColorArgb();
            int textWidth = textRenderer.getWidth(alertText);
            context.drawText(textRenderer, alertText, centerX - (textWidth / 2), centerY + radius + 3, alertColor, true);
        } else if (config.isShowDistanceLabels()) {
            String rangeText = String.format("%.0fm", maxRange);
            int textWidth = textRenderer.getWidth(rangeText);
            context.drawText(textRenderer, rangeText, centerX - (textWidth / 2), centerY + radius + 3, 0x88AAAAAA, false);
        }
    }

    private void drawCircularBackground(DrawContext context, int cx, int cy, int r) {
        // Approximate a dark circular vignette using horizontal pixel slices
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            context.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, 0x900B111A);
        }
    }

    private void drawConcentricRing(DrawContext context, int cx, int cy, int r, int color) {
        // Draw 16-point regular polygon approximation of circle rim
        int steps = 24;
        double angleStep = (Math.PI * 2.0) / steps;
        for (int i = 0; i < steps; i++) {
            double a1 = i * angleStep;
            double a2 = (i + 1) * angleStep;
            int x1 = (int) Math.round(cx + r * Math.sin(a1));
            int y1 = (int) Math.round(cy - r * Math.cos(a1));
            int x2 = (int) Math.round(cx + r * Math.sin(a2));
            int y2 = (int) Math.round(cy - r * Math.cos(a2));

            // Plot small connection dots
            context.fill(x1, y1, x1 + 1, y1 + 1, color);
            context.fill((x1 + x2) / 2, (y1 + y2) / 2, (x1 + x2) / 2 + 1, (y1 + y2) / 2 + 1, color);
        }
    }

    private void drawCardinalMarkers(DrawContext context, TextRenderer tr, int cx, int cy, int r, float cameraYaw) {
        // In Minecraft coordinates:
        // North is -Z (180°), East is +X (-90°), South is +Z (0°), West is -X (+90°)
        drawCardinal(context, tr, cx, cy, r, "N", 180.0, cameraYaw, 0xFFFF5555); // Highlight North red
        drawCardinal(context, tr, cx, cy, r, "E", -90.0, cameraYaw, 0xFFBBBBBB);
        drawCardinal(context, tr, cx, cy, r, "S", 0.0, cameraYaw, 0xFFBBBBBB);
        drawCardinal(context, tr, cx, cy, r, "W", 90.0, cameraYaw, 0xFFBBBBBB);
    }

    private void drawCardinal(DrawContext context, TextRenderer tr, int cx, int cy, int r,
                              String text, double worldBearingDeg, float cameraYaw, int color) {
        double relBearing = RadarVectorMath.wrapDegrees(worldBearingDeg - cameraYaw);
        double rad = Math.toRadians(relBearing);
        int px = (int) Math.round(cx + r * Math.sin(rad));
        int py = (int) Math.round(cy - r * Math.cos(rad));

        int tw = tr.getWidth(text);
        context.drawText(tr, text, px - (tw / 2), py - 4, color, false);
    }
}
