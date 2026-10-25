package com.slopgobbler.soundradar.test;

import com.slopgobbler.soundradar.config.SoundRadarConfig;
import com.slopgobbler.soundradar.engine.SoundRadarEngine;
import com.slopgobbler.soundradar.model.AcousticPulse;
import com.slopgobbler.soundradar.model.AudioCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying AcousticPulse life cycle, decay curves, and engine clustering.
 */
public class AcousticPulseTest {

    @Test
    @DisplayName("Acoustic pulse life cycle and alpha fade decay")
    void testPulseDecay() {
        long now = 100000L;
        long lifetime = 2000L;
        AcousticPulse pulse = new AcousticPulse("minecraft:entity.zombie.ambient", null,
                10.0, 64.0, 10.0, AudioCategory.HOSTILE, 1.0f, 1.0f, now, lifetime);

        // Immediately fresh
        assertEquals(0.0f, pulse.getDecayProgress(now), 0.001f);
        assertEquals(1.0f, pulse.getAlpha(now), 0.001f);
        assertFalse(pulse.isExpired(now));

        // Half-way through lifetime (1000ms elapsed)
        assertEquals(0.5f, pulse.getDecayProgress(now + 1000L), 0.001f);
        assertTrue(pulse.getAlpha(now + 1000L) < 1.0f);
        assertTrue(pulse.getAlpha(now + 1000L) > 0.0f);
        assertFalse(pulse.isExpired(now + 1000L));

        // Expired (2000ms elapsed)
        assertEquals(1.0f, pulse.getDecayProgress(now + 2000L), 0.001f);
        assertEquals(0.0f, pulse.getAlpha(now + 2000L), 0.001f);
        assertTrue(pulse.isExpired(now + 2000L));
    }

    @Test
    @DisplayName("Engine spatial clustering and pulse deduplication")
    void testEngineClustering() {
        SoundRadarConfig config = new SoundRadarConfig();
        config.setClusterDistanceThreshold(2.5D);
        config.setClusterTimeWindowMs(300L);
        config.setVolumeThreshold(0.0f);

        SoundRadarEngine engine = new SoundRadarEngine(config);

        // First footstep event
        AcousticPulse p1 = engine.onSoundPlayed("minecraft:entity.player.step", "PLAYERS",
                10.0, 64.0, 10.0, 0.8f, 1.0f, null);
        assertNotNull(p1);
        assertEquals(1, p1.getTriggerCount());
        assertEquals(1, engine.getActivePulses().size());

        // Rapid second footstep 1 block away within 100ms
        AcousticPulse p2 = engine.onSoundPlayed("minecraft:entity.player.step", "PLAYERS",
                10.5, 64.0, 10.5, 0.8f, 1.0f, null);
        assertNotNull(p2);

        // Should be clustered into the same pulse instance
        assertEquals(p1.getId(), p2.getId());
        assertEquals(2, p1.getTriggerCount());
        assertEquals(1, engine.getActivePulses().size());
        assertEquals(10.5, p1.getX(), 0.001);
    }

    @Test
    @DisplayName("Expanding 3D acoustic wave radius calculation")
    void testExpandingWaveRadius() {
        long now = 50000L;
        AcousticPulse pulse = new AcousticPulse("minecraft:entity.creeper.primed", null,
                0, 0, 0, AudioCategory.CREEPING, 1.0f, 1.0f, now, 1000L);

        double maxWave = 4.0D;
        assertEquals(0.0D, pulse.getExpandingWaveRadius(now, maxWave), 0.001D);
        assertEquals(2.0D, pulse.getExpandingWaveRadius(now + 500L, maxWave), 0.001D);
        assertEquals(4.0D, pulse.getExpandingWaveRadius(now + 1000L, maxWave), 0.001D);
    }
}
