package com.slopgobbler.soundradar.math;

/**
 * Tactical vector geometry and spherical coordinate mathematics for SoundRadar Pro.
 * Computes bearings, elevations, distance falloff curves, and polar/compass HUD projections.
 */
public final class RadarVectorMath {

    private RadarVectorMath() {}

    /**
     * Calculates 3D Euclidean distance between two spatial points.
     */
    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculates 2D horizontal distance on the X-Z plane.
     */
    public static double horizontalDistance(double x1, double z1, double x2, double z2) {
        double dx = x1 - x2;
        double dz = z1 - z2;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Wraps any angular value in degrees to the [-180.0, 180.0) interval.
     */
    public static double wrapDegrees(double angle) {
        double wrapped = angle % 360.0D;
        if (wrapped > 180.0D) {
            wrapped -= 360.0D;
        }
        if (wrapped <= -180.0D) {
            wrapped += 360.0D;
        }
        return wrapped;
    }

    /**
     * Calculates absolute world bearing in degrees from camera position to target.
     * Adheres strictly to Minecraft coordinate orientation:
     * - South (+Z) is 0.0°
     * - West (-X) is +90.0°
     * - North (-Z) is 180.0°
     * - East (+X) is -90.0° (or +270.0°)
     */
    public static double calculateAbsoluteBearing(double camX, double camZ, double targetX, double targetZ) {
        double dx = targetX - camX;
        double dz = targetZ - camZ;
        if (Math.abs(dx) < 1.0E-9D) {
            dx = 0.0D;
        }
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        if (Math.abs(angle - (-180.0D)) < 1.0E-6D) {
            angle = 180.0D;
        }
        return wrapDegrees(angle);
    }

    /**
     * Calculates relative horizontal bearing from player camera to acoustic source in degrees [-180.0, 180.0].
     * 0.0° = Directly in front of the player's crosshair.
     * +90.0° = Directly to the player's right.
     * -90.0° = Directly to the player's left.
     * ±180.0° = Directly behind the player.
     *
     * @param camX camera eye X
     * @param camZ camera eye Z
     * @param targetX sound source X
     * @param targetZ sound source Z
     * @param cameraYaw camera yaw in degrees
     * @return relative bearing in [-180.0, 180.0]
     */
    public static double calculateRelativeBearing(double camX, double camZ, double targetX, double targetZ, float cameraYaw) {
        double absoluteBearing = calculateAbsoluteBearing(camX, camZ, targetX, targetZ);
        return wrapDegrees(absoluteBearing - cameraYaw);
    }

    /**
     * Calculates vertical elevation angle in degrees [-90.0, 90.0] from camera to target.
     * Following Minecraft pitch convention:
     * 0.0° = level horizontal
     * +90.0° = looking directly down
     * -90.0° = looking directly up
     */
    public static double calculateElevationAngle(double camX, double camY, double camZ, double targetX, double targetY, double targetZ) {
        double dy = targetY - camY;
        double horizDist = horizontalDistance(camX, camZ, targetX, targetZ);
        if (horizDist < 1.0E-5D) {
            return dy > 0 ? -90.0D : (dy < 0 ? 90.0D : 0.0D);
        }
        return Math.toDegrees(Math.atan2(-dy, horizDist));
    }

    /**
     * Calculates relative elevation in degrees [-180.0, 180.0] between sound elevation and camera pitch.
     */
    public static double calculateRelativeElevation(double camX, double camY, double camZ,
                                                   double targetX, double targetY, double targetZ,
                                                   float cameraPitch) {
        double absolutePitch = calculateElevationAngle(camX, camY, camZ, targetX, targetY, targetZ);
        return wrapDegrees(absolutePitch - cameraPitch);
    }

    /**
     * Determines vertical height indicator category:
     * +1 for significant elevation above player (+3 blocks or higher)
     * -1 for significant elevation below player (-3 blocks or lower)
     * 0 for roughly co-planar (level)
     */
    public static int getHeightDifferentialIndicator(double camY, double targetY, double threshold) {
        double deltaY = targetY - camY;
        if (deltaY > threshold) {
            return 1;
        } else if (deltaY < -threshold) {
            return -1;
        }
        return 0;
    }

    /**
     * 2D Point on a HUD element.
     */
    public record ScreenPoint(double x, double y, boolean visible) {}

    /**
     * Projects acoustic source coordinates into 2D polar radar offsets relative to center.
     *
     * @param relativeBearingDeg relative yaw angle (-180 to 180, 0 = forward/up)
     * @param distance Euclidean distance
     * @param maxRadarRange max range in blocks (e.g. 48.0)
     * @param radarRadiusPixel radius in screen pixels
     * @return ScreenPoint with radar offsets (xOffset, yOffset) where (0,0) is center
     */
    public static ScreenPoint calculatePolarRadarPosition(double relativeBearingDeg, double distance,
                                                         double maxRadarRange, double radarRadiusPixel) {
        if (maxRadarRange <= 0.001D) maxRadarRange = 1.0D;
        double clampedDist = Math.min(distance, maxRadarRange);
        double normalizedRadius = (clampedDist / maxRadarRange) * radarRadiusPixel;

        double rad = Math.toRadians(relativeBearingDeg);
        // 0° bearing points UP (-Y in screen space), 90° points RIGHT (+X)
        double offsetX = normalizedRadius * Math.sin(rad);
        double offsetY = -normalizedRadius * Math.cos(rad);

        return new ScreenPoint(offsetX, offsetY, true);
    }

    /**
     * Projects sound relative bearing onto a horizontal tactical compass strip.
     *
     * @param relativeBearingDeg relative bearing (-180 to 180, 0 = centered)
     * @param stripWidth total compass strip width in pixels
     * @param visibleFovDegrees visible FOV span in degrees (e.g. 120°)
     * @return ScreenPoint where x is horizontal offset from strip center, y is 0, visible is true if in FOV
     */
    public static ScreenPoint calculateCompassTapePosition(double relativeBearingDeg, double stripWidth, double visibleFovDegrees) {
        double halfFov = visibleFovDegrees * 0.5D;
        boolean visible = Math.abs(relativeBearingDeg) <= halfFov;
        double clampedAngle = Math.max(-halfFov, Math.min(halfFov, relativeBearingDeg));
        double normalizedX = clampedAngle / halfFov; // [-1.0, 1.0]
        double offsetX = normalizedX * (stripWidth * 0.5D);

        return new ScreenPoint(offsetX, 0.0D, visible);
    }

    /**
     * Calculates distance acoustic loudness attenuation factor [0.0, 1.0].
     * Combines distance falloff with raw sound volume.
     *
     * @param distance distance in blocks
     * @param maxRange mod detection range
     * @param volume sound instance raw volume [0.0, 1.0+]
     * @return effective intensity factor [0.0, 1.0]
     */
    public static float calculateFalloff(double distance, double maxRange, float volume) {
        if (maxRange <= 0.001D) return 0.0f;
        float baseVol = Math.max(0.0f, Math.min(1.0f, volume));
        float distFactor = (float) Math.max(0.0D, 1.0D - (distance / maxRange));
        return Math.max(0.0f, Math.min(1.0f, distFactor * baseVol));
    }
}
