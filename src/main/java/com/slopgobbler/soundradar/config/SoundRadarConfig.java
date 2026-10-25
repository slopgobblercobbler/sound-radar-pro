package com.slopgobbler.soundradar.config;

import com.slopgobbler.soundradar.model.AudioCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Runtime configuration model for SoundRadar Pro.
 */
public class SoundRadarConfig {

    public enum DisplayMode {
        CIRCULAR("Circular Radar"),
        COMPASS_BAR("Tactical Heading Tape"),
        BOTH("Dual (Circle + Tape)"),
        OFF("Off");

        private final String label;

        DisplayMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum HudPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CENTER_BOTTOM
    }

    private static final SoundRadarConfig INSTANCE = new SoundRadarConfig();

    public static SoundRadarConfig getInstance() {
        return INSTANCE;
    }

    // General toggles
    private boolean radarEnabled = true;
    private DisplayMode displayMode = DisplayMode.CIRCULAR;
    private HudPosition hudPosition = HudPosition.TOP_RIGHT;
    private float radarScale = 1.0f;
    private int radarRadiusPixels = 54;
    private double maxDetectionRange = 48.0D;
    private float volumeThreshold = 0.05f;

    // In-world visualization
    private boolean inWorldPulsesEnabled = true;
    private double maxInWorldWaveRadius = 3.5D;

    // HUD detail options
    private boolean showHeightChevrons = true;
    private boolean showSoundLabels = true;
    private boolean showDistanceLabels = true;
    private boolean showCompassCardinals = true;

    // Category toggles
    private boolean trackCreeping = true;
    private boolean trackHostile = true;
    private boolean trackPlayer = true;
    private boolean trackPassive = true;
    private boolean trackEnvironmental = true;

    // Clustering and retention
    private int maxActivePulses = 64;
    private float lifetimeMultiplier = 1.0f;
    private double clusterDistanceThreshold = 2.5D;
    private long clusterTimeWindowMs = 250L;

    // Filtering
    private boolean whitelistMode = false;
    private List<String> whitelist = new ArrayList<>();
    private List<String> blacklist = new ArrayList<>();

    public SoundRadarConfig() {
        // Default ignored sound patterns (background noise / ui / music)
        blacklist.add("minecraft:music.*");
        blacklist.add("minecraft:ui.*");
        blacklist.add("minecraft:ambient.underwater.loop.*");
    }

    public boolean isRadarEnabled() {
        return radarEnabled;
    }

    public void setRadarEnabled(boolean radarEnabled) {
        this.radarEnabled = radarEnabled;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode != null ? displayMode : DisplayMode.CIRCULAR;
    }

    public void cycleDisplayMode() {
        DisplayMode[] modes = DisplayMode.values();
        int nextIdx = (this.displayMode.ordinal() + 1) % modes.length;
        this.displayMode = modes[nextIdx];
    }

    public HudPosition getHudPosition() {
        return hudPosition;
    }

    public void setHudPosition(HudPosition hudPosition) {
        this.hudPosition = hudPosition != null ? hudPosition : HudPosition.TOP_RIGHT;
    }

    public float getRadarScale() {
        return radarScale;
    }

    public void setRadarScale(float radarScale) {
        this.radarScale = Math.max(0.5f, Math.min(2.5f, radarScale));
    }

    public int getRadarRadiusPixels() {
        return radarRadiusPixels;
    }

    public void setRadarRadiusPixels(int radarRadiusPixels) {
        this.radarRadiusPixels = Math.max(30, Math.min(150, radarRadiusPixels));
    }

    public double getMaxDetectionRange() {
        return maxDetectionRange;
    }

    public void setMaxDetectionRange(double maxDetectionRange) {
        this.maxDetectionRange = Math.max(10.0D, Math.min(160.0D, maxDetectionRange));
    }

    public float getVolumeThreshold() {
        return volumeThreshold;
    }

    public void setVolumeThreshold(float volumeThreshold) {
        this.volumeThreshold = Math.max(0.0f, Math.min(1.0f, volumeThreshold));
    }

    public boolean isInWorldPulsesEnabled() {
        return inWorldPulsesEnabled;
    }

    public void setInWorldPulsesEnabled(boolean inWorldPulsesEnabled) {
        this.inWorldPulsesEnabled = inWorldPulsesEnabled;
    }

    public double getMaxInWorldWaveRadius() {
        return maxInWorldWaveRadius;
    }

    public void setMaxInWorldWaveRadius(double maxInWorldWaveRadius) {
        this.maxInWorldWaveRadius = Math.max(1.0D, Math.min(10.0D, maxInWorldWaveRadius));
    }

    public boolean isShowHeightChevrons() {
        return showHeightChevrons;
    }

    public void setShowHeightChevrons(boolean showHeightChevrons) {
        this.showHeightChevrons = showHeightChevrons;
    }

    public boolean isShowSoundLabels() {
        return showSoundLabels;
    }

    public void setShowSoundLabels(boolean showSoundLabels) {
        this.showSoundLabels = showSoundLabels;
    }

    public boolean isShowDistanceLabels() {
        return showDistanceLabels;
    }

    public void setShowDistanceLabels(boolean showDistanceLabels) {
        this.showDistanceLabels = showDistanceLabels;
    }

    public boolean isShowCompassCardinals() {
        return showCompassCardinals;
    }

    public void setShowCompassCardinals(boolean showCompassCardinals) {
        this.showCompassCardinals = showCompassCardinals;
    }

    public boolean isTrackCreeping() {
        return trackCreeping;
    }

    public void setTrackCreeping(boolean trackCreeping) {
        this.trackCreeping = trackCreeping;
    }

    public boolean isTrackHostile() {
        return trackHostile;
    }

    public void setTrackHostile(boolean trackHostile) {
        this.trackHostile = trackHostile;
    }

    public boolean isTrackPlayer() {
        return trackPlayer;
    }

    public void setTrackPlayer(boolean trackPlayer) {
        this.trackPlayer = trackPlayer;
    }

    public boolean isTrackPassive() {
        return trackPassive;
    }

    public void setTrackPassive(boolean trackPassive) {
        this.trackPassive = trackPassive;
    }

    public boolean isTrackEnvironmental() {
        return trackEnvironmental;
    }

    public void setTrackEnvironmental(boolean trackEnvironmental) {
        this.trackEnvironmental = trackEnvironmental;
    }

    public boolean isCategoryEnabled(AudioCategory category) {
        if (category == null) return true;
        return switch (category) {
            case CREEPING -> trackCreeping;
            case HOSTILE -> trackHostile;
            case PLAYER -> trackPlayer;
            case PASSIVE -> trackPassive;
            case ENVIRONMENTAL -> trackEnvironmental;
        };
    }

    public int getMaxActivePulses() {
        return maxActivePulses;
    }

    public void setMaxActivePulses(int maxActivePulses) {
        this.maxActivePulses = Math.max(10, Math.min(256, maxActivePulses));
    }

    public float getLifetimeMultiplier() {
        return lifetimeMultiplier;
    }

    public void setLifetimeMultiplier(float lifetimeMultiplier) {
        this.lifetimeMultiplier = Math.max(0.2f, Math.min(5.0f, lifetimeMultiplier));
    }

    public double getClusterDistanceThreshold() {
        return clusterDistanceThreshold;
    }

    public void setClusterDistanceThreshold(double clusterDistanceThreshold) {
        this.clusterDistanceThreshold = Math.max(0.5D, Math.min(10.0D, clusterDistanceThreshold));
    }

    public long getClusterTimeWindowMs() {
        return clusterTimeWindowMs;
    }

    public void setClusterTimeWindowMs(long clusterTimeWindowMs) {
        this.clusterTimeWindowMs = Math.max(50L, Math.min(2000L, clusterTimeWindowMs));
    }

    public boolean isWhitelistMode() {
        return whitelistMode;
    }

    public void setWhitelistMode(boolean whitelistMode) {
        this.whitelistMode = whitelistMode;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist != null ? whitelist : new ArrayList<>();
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(List<String> blacklist) {
        this.blacklist = blacklist != null ? blacklist : new ArrayList<>();
    }
}
