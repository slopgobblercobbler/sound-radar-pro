package com.slopgobbler.soundradar.test;

import com.slopgobbler.soundradar.math.RadarVectorMath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SoundRadar Pro vector geometry, relative bearing calculations,
 * and polar/compass tape HUD coordinates.
 */
public class RadarVectorMathTest {

    private static final double EPSILON = 0.001D;

    @Test
    @DisplayName("Euclidean 3D distance and 2D horizontal distance math")
    void testDistances() {
        double dist3d = RadarVectorMath.distance(0, 0, 0, 3, 4, 12);
        assertEquals(13.0D, dist3d, EPSILON);

        double horizDist = RadarVectorMath.horizontalDistance(10, 20, 13, 24);
        assertEquals(5.0D, horizDist, EPSILON);
    }

    @Test
    @DisplayName("Absolute bearing calculation adhering to Minecraft coordinates")
    void testAbsoluteBearings() {
        // Minecraft convention:
        // South (+Z) = 0°
        assertEquals(0.0D, RadarVectorMath.calculateAbsoluteBearing(0, 0, 0, 10), EPSILON);

        // West (-X) = +90°
        assertEquals(90.0D, RadarVectorMath.calculateAbsoluteBearing(0, 0, -10, 0), EPSILON);

        // North (-Z) = 180°
        assertEquals(180.0D, RadarVectorMath.calculateAbsoluteBearing(0, 0, 0, -10), EPSILON);

        // East (+X) = -90°
        assertEquals(-90.0D, RadarVectorMath.calculateAbsoluteBearing(0, 0, 10, 0), EPSILON);
    }

    @Test
    @DisplayName("Relative bearing from camera heading to acoustic sound source")
    void testRelativeBearings() {
        // Facing South (yaw 0°), sound directly South (+Z) -> 0° (ahead)
        assertEquals(0.0D, RadarVectorMath.calculateRelativeBearing(0, 0, 0, 10, 0.0f), EPSILON);

        // Facing South (yaw 0°), sound directly West (-X) -> +90° (right)
        assertEquals(90.0D, RadarVectorMath.calculateRelativeBearing(0, 0, -10, 0, 0.0f), EPSILON);

        // Facing West (yaw 90°), sound directly West (-X) -> 0° (ahead)
        assertEquals(0.0D, RadarVectorMath.calculateRelativeBearing(0, 0, -10, 0, 90.0f), EPSILON);

        // Facing North (yaw 180°), sound directly South (+Z) -> 180° (behind)
        assertEquals(180.0D, Math.abs(RadarVectorMath.calculateRelativeBearing(0, 0, 0, 10, 180.0f)), EPSILON);
    }

    @Test
    @DisplayName("Elevation angle and height differential chevrons")
    void testElevationAndHeightIndicators() {
        // Source 10 blocks directly above player
        double elevUp = RadarVectorMath.calculateElevationAngle(0, 64, 0, 0, 74, 0);
        assertEquals(-90.0D, elevUp, EPSILON);
        assertEquals(1, RadarVectorMath.getHeightDifferentialIndicator(64, 74, 3.0D));

        // Source 10 blocks directly below player
        double elevDown = RadarVectorMath.calculateElevationAngle(0, 64, 0, 0, 54, 0);
        assertEquals(90.0D, elevDown, EPSILON);
        assertEquals(-1, RadarVectorMath.getHeightDifferentialIndicator(64, 54, 3.0D));

        // Source on the same vertical level
        assertEquals(0, RadarVectorMath.getHeightDifferentialIndicator(64, 65, 3.0D));
    }

    @Test
    @DisplayName("Polar radar 2D coordinates projection")
    void testPolarRadarCoordinates() {
        double maxRange = 50.0D;
        double radius = 50.0D;

        // Sound ahead (relYaw = 0°) at 25 blocks (half range) -> (0, -25)
        RadarVectorMath.ScreenPoint pAhead = RadarVectorMath.calculatePolarRadarPosition(0.0D, 25.0D, maxRange, radius);
        assertEquals(0.0D, pAhead.x(), EPSILON);
        assertEquals(-25.0D, pAhead.y(), EPSILON);

        // Sound directly right (relYaw = 90°) at 50 blocks (full range) -> (+50, 0)
        RadarVectorMath.ScreenPoint pRight = RadarVectorMath.calculatePolarRadarPosition(90.0D, 50.0D, maxRange, radius);
        assertEquals(50.0D, pRight.x(), EPSILON);
        assertEquals(0.0D, pRight.y(), EPSILON);

        // Sound directly behind (relYaw = 180°) at 50 blocks -> (0, +50)
        RadarVectorMath.ScreenPoint pBehind = RadarVectorMath.calculatePolarRadarPosition(180.0D, 50.0D, maxRange, radius);
        assertEquals(0.0D, pBehind.x(), EPSILON);
        assertEquals(50.0D, pBehind.y(), EPSILON);

        // Sound directly left (relYaw = -90°) at 50 blocks -> (-50, 0)
        RadarVectorMath.ScreenPoint pLeft = RadarVectorMath.calculatePolarRadarPosition(-90.0D, 50.0D, maxRange, radius);
        assertEquals(-50.0D, pLeft.x(), EPSILON);
        assertEquals(0.0D, pLeft.y(), EPSILON);
    }

    @Test
    @DisplayName("Tactical compass tape linear projection")
    void testCompassTapeProjection() {
        double stripWidth = 200.0D;
        double fov = 120.0D;

        // 0° bearing -> centered (x = 0)
        RadarVectorMath.ScreenPoint pCenter = RadarVectorMath.calculateCompassTapePosition(0.0D, stripWidth, fov);
        assertTrue(pCenter.visible());
        assertEquals(0.0D, pCenter.x(), EPSILON);

        // +60° bearing (half of 120° FOV) -> far right (+100)
        RadarVectorMath.ScreenPoint pEdge = RadarVectorMath.calculateCompassTapePosition(60.0D, stripWidth, fov);
        assertTrue(pEdge.visible());
        assertEquals(100.0D, pEdge.x(), EPSILON);

        // 150° bearing (behind) -> not visible within FOV
        RadarVectorMath.ScreenPoint pBehind = RadarVectorMath.calculateCompassTapePosition(150.0D, stripWidth, fov);
        assertFalse(pBehind.visible());
    }

    @Test
    @DisplayName("Acoustic loudness falloff curve")
    void testFalloff() {
        // Distance 0m with volume 1.0 -> 1.0f
        assertEquals(1.0f, RadarVectorMath.calculateFalloff(0.0D, 50.0D, 1.0f), 0.001f);

        // Distance 25m (half range) with volume 1.0 -> 0.5f
        assertEquals(0.5f, RadarVectorMath.calculateFalloff(25.0D, 50.0D, 1.0f), 0.001f);

        // Distance 50m (max range) -> 0.0f
        assertEquals(0.0f, RadarVectorMath.calculateFalloff(50.0D, 50.0D, 1.0f), 0.001f);

        // Volume scalar attenuation
        assertEquals(0.4f, RadarVectorMath.calculateFalloff(0.0D, 50.0D, 0.4f), 0.001f);
    }
}
