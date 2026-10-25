package com.slopgobbler.soundradar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Persists and loads SoundRadar Pro configuration from disk.
 */
public final class SoundRadarConfigHandler {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_PATH = "config/soundradar.json";

    private SoundRadarConfigHandler() {}

    public static void loadConfig() {
        File file = new File(CONFIG_FILE_PATH);
        if (!file.exists()) {
            saveConfig();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            SoundRadarConfig loaded = GSON.fromJson(reader, SoundRadarConfig.class);
            if (loaded != null) {
                copyConfig(loaded, SoundRadarConfig.getInstance());
            }
        } catch (Exception e) {
            System.err.println("[SoundRadar Pro] Failed to load configuration: " + e.getMessage());
        }
    }

    public static void saveConfig() {
        File file = new File(CONFIG_FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(SoundRadarConfig.getInstance(), writer);
        } catch (IOException e) {
            System.err.println("[SoundRadar Pro] Failed to save configuration: " + e.getMessage());
        }
    }

    private static void copyConfig(SoundRadarConfig source, SoundRadarConfig target) {
        target.setRadarEnabled(source.isRadarEnabled());
        target.setDisplayMode(source.getDisplayMode());
        target.setHudPosition(source.getHudPosition());
        target.setRadarScale(source.getRadarScale());
        target.setRadarRadiusPixels(source.getRadarRadiusPixels());
        target.setMaxDetectionRange(source.getMaxDetectionRange());
        target.setVolumeThreshold(source.getVolumeThreshold());
        target.setInWorldPulsesEnabled(source.isInWorldPulsesEnabled());
        target.setMaxInWorldWaveRadius(source.getMaxInWorldWaveRadius());
        target.setShowHeightChevrons(source.isShowHeightChevrons());
        target.setShowSoundLabels(source.isShowSoundLabels());
        target.setShowDistanceLabels(source.isShowDistanceLabels());
        target.setShowCompassCardinals(source.isShowCompassCardinals());
        target.setTrackCreeping(source.isTrackCreeping());
        target.setTrackHostile(source.isTrackHostile());
        target.setTrackPlayer(source.isTrackPlayer());
        target.setTrackPassive(source.isTrackPassive());
        target.setTrackEnvironmental(source.isTrackEnvironmental());
        target.setMaxActivePulses(source.getMaxActivePulses());
        target.setLifetimeMultiplier(source.getLifetimeMultiplier());
        target.setClusterDistanceThreshold(source.getClusterDistanceThreshold());
        target.setClusterTimeWindowMs(source.getClusterTimeWindowMs());
        target.setWhitelistMode(source.isWhitelistMode());
        target.setWhitelist(source.getWhitelist());
        target.setBlacklist(source.getBlacklist());
    }
}
