package com.slopgobbler.soundradar.render;

/**
 * Color utility methods and visual effects for SoundRadar Pro.
 */
public final class RenderColorUtil {

    private RenderColorUtil() {}

    /**
     * Packs red, green, blue, alpha into a 32-bit ARGB integer.
     */
    public static int toArgb(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24) |
               ((red & 0xFF) << 16) |
               ((green & 0xFF) << 8) |
               (blue & 0xFF);
    }

    /**
     * Applies an alpha scalar [0.0, 1.0] to an existing ARGB color.
     */
    public static int applyAlpha(int argb, float alphaFactor) {
        int originalAlpha = (argb >>> 24) & 0xFF;
        int newAlpha = Math.max(0, Math.min(255, (int)(originalAlpha * Math.max(0.0f, Math.min(1.0f, alphaFactor)))));
        return (newAlpha << 24) | (argb & 0x00FFFFFF);
    }

    /**
     * Computes high-visibility warning strobe alpha for critical creeping cues.
     */
    public static float calculateStrobe(long nowMs, float baseAlpha, int frequencyMs) {
        float sine = (float) Math.sin((double) (nowMs % frequencyMs) / (double) frequencyMs * Math.PI * 2.0);
        float strobeMultiplier = 0.7f + 0.3f * sine;
        return Math.max(0.0f, Math.min(1.0f, baseAlpha * strobeMultiplier));
    }

    /**
     * Linear interpolation between two RGB colors.
     */
    public static int lerpRgb(int colorA, int colorB, float t) {
        float factor = Math.max(0.0f, Math.min(1.0f, t));
        int rA = (colorA >> 16) & 0xFF;
        int gA = (colorA >> 8) & 0xFF;
        int bA = colorA & 0xFF;

        int rB = (colorB >> 16) & 0xFF;
        int gB = (colorB >> 8) & 0xFF;
        int bB = colorB & 0xFF;

        int r = (int) (rA + (rB - rA) * factor);
        int g = (int) (gA + (gB - gA) * factor);
        int b = (int) (bA + (bB - bA) * factor);

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }
}
