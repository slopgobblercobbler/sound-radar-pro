package com.slopgobbler.soundradar.test;

import com.slopgobbler.soundradar.model.AudioCategory;
import com.slopgobbler.soundradar.model.SoundClassifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying audio cue pattern classification for SoundRadar Pro.
 */
public class SoundClassifierTest {

    @Test
    @DisplayName("Classifies creeping and lethal stealth hazard cues")
    void testCreepingClassification() {
        assertEquals(AudioCategory.CREEPING,
                SoundClassifier.classify("minecraft:entity.creeper.primed"));

        assertEquals(AudioCategory.CREEPING,
                SoundClassifier.classify("minecraft:entity.warden.heartbeat"));

        assertEquals(AudioCategory.CREEPING,
                SoundClassifier.classify("minecraft:entity.warden.sonic_boom"));

        assertEquals(AudioCategory.CREEPING,
                SoundClassifier.classify("minecraft:entity.tnt.primed"));

        assertEquals(AudioCategory.CREEPING,
                SoundClassifier.classify("minecraft:block.sculk_shrieker.shriek"));

        // Subtitle based hazard classification
        assertEquals(AudioCategory.CREEPING,
                SoundClassifier.classify("minecraft:generic.random", "NEUTRAL", "Creeper hisses"));

        assertEquals(AudioCategory.CREEPING,
                SoundClassifier.classify("minecraft:generic.random", "NEUTRAL", "TNT fizzes"));
    }

    @Test
    @DisplayName("Classifies hostile mobs")
    void testHostileClassification() {
        assertEquals(AudioCategory.HOSTILE,
                SoundClassifier.classify("minecraft:entity.zombie.ambient"));

        assertEquals(AudioCategory.HOSTILE,
                SoundClassifier.classify("minecraft:entity.skeleton.step"));

        assertEquals(AudioCategory.HOSTILE,
                SoundClassifier.classify("minecraft:entity.spider.ambient"));

        assertEquals(AudioCategory.HOSTILE,
                SoundClassifier.classify("minecraft:entity.ghast.scream"));

        assertEquals(AudioCategory.HOSTILE,
                SoundClassifier.classify("minecraft:entity.blaze.shoot"));

        assertEquals(AudioCategory.HOSTILE,
                SoundClassifier.classify("minecraft:custom.monster", "HOSTILE"));
    }

    @Test
    @DisplayName("Classifies player interactions and gear actions")
    void testPlayerClassification() {
        assertEquals(AudioCategory.PLAYER,
                SoundClassifier.classify("minecraft:entity.player.hurt"));

        assertEquals(AudioCategory.PLAYER,
                SoundClassifier.classify("minecraft:item.bow.shoot"));

        assertEquals(AudioCategory.PLAYER,
                SoundClassifier.classify("minecraft:item.trident.throw"));

        assertEquals(AudioCategory.PLAYER,
                SoundClassifier.classify("minecraft:entity.generic.eat"));

        assertEquals(AudioCategory.PLAYER,
                SoundClassifier.classify("minecraft:entity.generic.drink"));

        assertEquals(AudioCategory.PLAYER,
                SoundClassifier.classify("minecraft:any.sound", "PLAYERS"));
    }

    @Test
    @DisplayName("Classifies passive creatures and farm animals")
    void testPassiveClassification() {
        assertEquals(AudioCategory.PASSIVE,
                SoundClassifier.classify("minecraft:entity.cow.ambient"));

        assertEquals(AudioCategory.PASSIVE,
                SoundClassifier.classify("minecraft:entity.sheep.ambient"));

        assertEquals(AudioCategory.PASSIVE,
                SoundClassifier.classify("minecraft:entity.pig.ambient"));

        assertEquals(AudioCategory.PASSIVE,
                SoundClassifier.classify("minecraft:entity.villager.ambient"));

        assertEquals(AudioCategory.PASSIVE,
                SoundClassifier.classify("minecraft:entity.chicken.step"));
    }

    @Test
    @DisplayName("Classifies environmental ambience and weather")
    void testEnvironmentalClassification() {
        assertEquals(AudioCategory.ENVIRONMENTAL,
                SoundClassifier.classify("minecraft:ambient.cave"));

        assertEquals(AudioCategory.ENVIRONMENTAL,
                SoundClassifier.classify("minecraft:weather.rain"));

        assertEquals(AudioCategory.ENVIRONMENTAL,
                SoundClassifier.classify("minecraft:block.lava.ambient"));

        assertEquals(AudioCategory.ENVIRONMENTAL,
                SoundClassifier.classify("minecraft:block.piston.extend"));
    }
}
