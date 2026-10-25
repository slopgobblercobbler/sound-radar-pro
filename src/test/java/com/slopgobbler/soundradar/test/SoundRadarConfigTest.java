package com.slopgobbler.soundradar.test;

import com.slopgobbler.soundradar.config.SoundRadarConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying configuration state, display mode cycling, and limits.
 */
public class SoundRadarConfigTest {

    @Test
    @DisplayName("DisplayMode enum cycle through modes")
    void testDisplayModeCycle() {
        SoundRadarConfig config = new SoundRadarConfig();
        config.setDisplayMode(SoundRadarConfig.DisplayMode.CIRCULAR);

        config.cycleDisplayMode();
        assertEquals(SoundRadarConfig.DisplayMode.COMPASS_BAR, config.getDisplayMode());

        config.cycleDisplayMode();
        assertEquals(SoundRadarConfig.DisplayMode.BOTH, config.getDisplayMode());

        config.cycleDisplayMode();
        assertEquals(SoundRadarConfig.DisplayMode.OFF, config.getDisplayMode());

        config.cycleDisplayMode();
        assertEquals(SoundRadarConfig.DisplayMode.CIRCULAR, config.getDisplayMode());
    }

    @Test
    @DisplayName("Range and scale boundary clamping")
    void testBoundaryClamping() {
        SoundRadarConfig config = new SoundRadarConfig();

        config.setMaxDetectionRange(9999.0D);
        assertTrue(config.getMaxDetectionRange() <= 160.0D);

        config.setMaxDetectionRange(-10.0D);
        assertTrue(config.getMaxDetectionRange() >= 10.0D);

        config.setRadarScale(0.1f);
        assertTrue(config.getRadarScale() >= 0.5f);

        config.setRadarScale(10.0f);
        assertTrue(config.getRadarScale() <= 2.5f);
    }
}
