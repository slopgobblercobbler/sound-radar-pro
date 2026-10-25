package com.slopgobbler.soundradar.test;

import com.slopgobbler.soundradar.config.SoundRadarConfig;
import com.slopgobbler.soundradar.engine.SoundFilterManager;
import com.slopgobbler.soundradar.model.AudioCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying volume filtering, category muting, blacklists, and whitelists.
 */
public class SoundFilterManagerTest {

    private SoundRadarConfig config;
    private SoundFilterManager filterManager;

    @BeforeEach
    void setUp() {
        config = new SoundRadarConfig();
        config.setRadarEnabled(true);
        config.setVolumeThreshold(0.10f);
        filterManager = new SoundFilterManager(config);
    }

    @Test
    @DisplayName("Volume threshold filtering")
    void testVolumeThreshold() {
        // Sound below 10% threshold should be rejected
        assertFalse(filterManager.shouldTrack("minecraft:entity.zombie.ambient", 0.05f, AudioCategory.HOSTILE));

        // Sound above 10% threshold should be accepted
        assertTrue(filterManager.shouldTrack("minecraft:entity.zombie.ambient", 0.25f, AudioCategory.HOSTILE));
    }

    @Test
    @DisplayName("Category toggle filtering")
    void testCategoryToggles() {
        // Disable passive tracking
        config.setTrackPassive(false);

        // Hostiles still pass
        assertTrue(filterManager.shouldTrack("minecraft:entity.zombie.ambient", 0.5f, AudioCategory.HOSTILE));

        // Passives rejected
        assertFalse(filterManager.shouldTrack("minecraft:entity.cow.ambient", 0.5f, AudioCategory.PASSIVE));
    }

    @Test
    @DisplayName("Blacklist wildcard matching")
    void testBlacklistWildcards() {
        config.setBlacklist(List.of("minecraft:music.*", "minecraft:ui.*", "*click*"));

        // Blacklisted music
        assertFalse(filterManager.shouldTrack("minecraft:music.game", 1.0f, AudioCategory.ENVIRONMENTAL));

        // Blacklisted UI
        assertFalse(filterManager.shouldTrack("minecraft:ui.button.click", 1.0f, AudioCategory.ENVIRONMENTAL));

        // Regular sound passes
        assertTrue(filterManager.shouldTrack("minecraft:entity.creeper.primed", 1.0f, AudioCategory.CREEPING));
    }

    @Test
    @DisplayName("Whitelist mode filtering")
    void testWhitelistMode() {
        config.setWhitelistMode(true);
        config.setWhitelist(List.of("minecraft:entity.creeper.*", "minecraft:entity.warden.*"));

        // Allowed sounds
        assertTrue(filterManager.shouldTrack("minecraft:entity.creeper.primed", 1.0f, AudioCategory.CREEPING));
        assertTrue(filterManager.shouldTrack("minecraft:entity.warden.sniff", 1.0f, AudioCategory.CREEPING));

        // Non-whitelisted sound rejected
        assertFalse(filterManager.shouldTrack("minecraft:entity.zombie.ambient", 1.0f, AudioCategory.HOSTILE));
    }

    @Test
    @DisplayName("Wildcard pattern matching helper")
    void testPatternMatching() {
        assertTrue(SoundFilterManager.matchesPattern("minecraft:music.nether", "minecraft:music.*"));
        assertTrue(SoundFilterManager.matchesPattern("minecraft:block.bell.use", "*bell*"));
        assertFalse(SoundFilterManager.matchesPattern("minecraft:entity.zombie.ambient", "minecraft:block.*"));
    }
}
