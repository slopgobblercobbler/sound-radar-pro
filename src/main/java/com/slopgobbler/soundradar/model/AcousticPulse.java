package com.slopgobbler.soundradar.model;

import com.slopgobbler.soundradar.math.Vector3d;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents an active directional acoustic wave / ping intercepted by the radar engine.
 */
public class AcousticPulse {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    private final long id;
    private final String soundId;
    private final String subtitle;
    private final AudioCategory category;
    private final float initialVolume;
    private final float initialPitch;
    private final long creationTimeMs;
    private final long maxLifetimeMs;

    private double x;
    private double y;
    private double z;
    private int triggerCount;
    private long lastTriggerTimeMs;

    public AcousticPulse(String soundId, String subtitle, double x, double y, double z,
                         AudioCategory category, float volume, float pitch, long nowMs) {
        this(soundId, subtitle, x, y, z, category, volume, pitch, nowMs, category.getDefaultLifetimeMs());
    }

    public AcousticPulse(String soundId, String subtitle, double x, double y, double z,
                         AudioCategory category, float volume, float pitch, long nowMs, long lifetimeMs) {
        this.id = ID_GENERATOR.getAndIncrement();
        this.soundId = soundId != null ? soundId : "unknown";
        this.subtitle = subtitle;
        this.x = x;
        this.y = y;
        this.z = z;
        this.category = category != null ? category : AudioCategory.ENVIRONMENTAL;
        this.initialVolume = Math.max(0.01f, volume);
        this.initialPitch = pitch;
        this.creationTimeMs = nowMs;
        this.lastTriggerTimeMs = nowMs;
        this.maxLifetimeMs = Math.max(500L, lifetimeMs);
        this.triggerCount = 1;
    }

    public long getId() {
        return id;
    }

    public String getSoundId() {
        return soundId;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public AudioCategory getCategory() {
        return category;
    }

    public float getInitialVolume() {
        return initialVolume;
    }

    public float getInitialPitch() {
        return initialPitch;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Vector3d getPosition() {
        return new Vector3d(x, y, z);
    }

    public long getCreationTimeMs() {
        return creationTimeMs;
    }

    public long getMaxLifetimeMs() {
        return maxLifetimeMs;
    }

    public int getTriggerCount() {
        return triggerCount;
    }

    public long getLastTriggerTimeMs() {
        return lastTriggerTimeMs;
    }

    /**
     * Elapsed time in milliseconds since pulse emission.
     */
    public long getAgeMs(long nowMs) {
        return Math.max(0L, nowMs - creationTimeMs);
    }

    /**
     * Elapsed time in milliseconds since the most recent trigger/refresh.
     */
    public long getTimeSinceLastTrigger(long nowMs) {
        return Math.max(0L, nowMs - lastTriggerTimeMs);
    }

    public boolean isExpired(long nowMs) {
        return (nowMs - lastTriggerTimeMs) >= maxLifetimeMs;
    }

    /**
     * Normalized progress [0.0 (fresh) to 1.0 (expired)].
     */
    public float getDecayProgress(long nowMs) {
        long elapsed = Math.max(0L, nowMs - lastTriggerTimeMs);
        return Math.min(1.0f, (float) elapsed / (float) maxLifetimeMs);
    }

    /**
     * Opacity factor [1.0 down to 0.0] with smooth fade-out easing.
     */
    public float getAlpha(long nowMs) {
        float progress = getDecayProgress(nowMs);
        if (progress >= 1.0f) return 0.0f;
        // Smooth cubic ease-out
        float remaining = 1.0f - progress;
        return remaining * remaining;
    }

    /**
     * Outward expanding acoustic wave front radius in blocks.
     */
    public double getExpandingWaveRadius(long nowMs, double maxWaveRadius) {
        float progress = getDecayProgress(nowMs);
        return Math.min(maxWaveRadius, progress * maxWaveRadius);
    }

    /**
     * Refreshes pulse when identical sound is triggered in near proximity (clustering).
     */
    public void retrigger(double newX, double newY, double newZ, long nowMs) {
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.lastTriggerTimeMs = nowMs;
        this.triggerCount++;
    }

    public void updatePosition(double newX, double newY, double newZ) {
        this.x = newX;
        this.y = newY;
        this.z = newZ;
    }

    @Override
    public String toString() {
        return String.format("AcousticPulse[id=%d, sound=%s, cat=%s, pos=(%.1f, %.1f, %.1f)]",
                id, soundId, category, x, y, z);
    }
}
