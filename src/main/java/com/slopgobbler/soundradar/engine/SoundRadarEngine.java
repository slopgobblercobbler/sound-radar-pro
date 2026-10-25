package com.slopgobbler.soundradar.engine;

import com.slopgobbler.soundradar.config.SoundRadarConfig;
import com.slopgobbler.soundradar.model.AcousticPulse;
import com.slopgobbler.soundradar.model.AudioCategory;
import com.slopgobbler.soundradar.model.SoundClassifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tactical acoustic radar processing engine.
 * Intercepts audio emissions, clusters repetitive pings, filters irrelevant noise,
 * and maintains active acoustic wave states for HUD and in-world visualizers.
 */
public class SoundRadarEngine {

    private static final SoundRadarEngine INSTANCE = new SoundRadarEngine();

    public static SoundRadarEngine getInstance() {
        return INSTANCE;
    }

    private final SoundRadarConfig config;
    private final SoundFilterManager filterManager;
    private final PulseHistoryBuffer historyBuffer;
    private final List<AcousticPulse> activePulses;

    public SoundRadarEngine() {
        this(SoundRadarConfig.getInstance());
    }

    public SoundRadarEngine(SoundRadarConfig config) {
        this.config = config != null ? config : SoundRadarConfig.getInstance();
        this.filterManager = new SoundFilterManager(this.config);
        this.historyBuffer = new PulseHistoryBuffer(128);
        this.activePulses = new CopyOnWriteArrayList<>();
    }

    /**
     * Intercepts and processes a sound event.
     *
     * @param soundId unique identifier (e.g. "minecraft:entity.creeper.primed")
     * @param categoryName vanilla SoundCategory name or null
     * @param x sound emission world X
     * @param y sound emission world Y
     * @param z sound emission world Z
     * @param volume volume level
     * @param pitch pitch level
     * @param subtitle optional subtitle description
     */
    public AcousticPulse onSoundPlayed(String soundId, String categoryName,
                                       double x, double y, double z,
                                       float volume, float pitch,
                                       String subtitle) {
        if (!config.isRadarEnabled()) {
            return null;
        }

        AudioCategory category = SoundClassifier.classify(soundId, categoryName, subtitle);

        if (!filterManager.shouldTrack(soundId, volume, category)) {
            return null;
        }

        long now = System.currentTimeMillis();

        // 1. Check for spatial clustering with recently active pulses of the same sound
        double clusterDistSq = config.getClusterDistanceThreshold() * config.getClusterDistanceThreshold();
        long clusterWindow = config.getClusterTimeWindowMs();

        for (AcousticPulse existing : activePulses) {
            if (existing.isExpired(now)) continue;

            if (existing.getSoundId().equalsIgnoreCase(soundId) ||
                (existing.getCategory() == category && existing.getCategory() == AudioCategory.PLAYER)) {
                double dx = existing.getX() - x;
                double dy = existing.getY() - y;
                double dz = existing.getZ() - z;
                double distSq = dx * dx + dy * dy + dz * dz;

                if (distSq <= clusterDistSq && existing.getTimeSinceLastTrigger(now) <= clusterWindow) {
                    existing.retrigger(x, y, z, now);
                    return existing;
                }
            }
        }

        // 2. Compute dynamic pulse lifetime based on config multiplier
        long adjustedLifetime = (long) (category.getDefaultLifetimeMs() * config.getLifetimeMultiplier());
        AcousticPulse newPulse = new AcousticPulse(soundId, subtitle, x, y, z, category, volume, pitch, now, adjustedLifetime);

        // 3. Enforce maximum active pulse limit by dropping lowest priority or oldest pulse
        if (activePulses.size() >= config.getMaxActivePulses()) {
            evictLowestPriorityPulse();
        }

        activePulses.add(newPulse);
        historyBuffer.record(newPulse);
        return newPulse;
    }

    /**
     * Enforces pulse capacity bounds by evicting lowest priority or oldest pulse.
     */
    private synchronized void evictLowestPriorityPulse() {
        if (activePulses.isEmpty()) return;

        AcousticPulse candidate = null;
        for (AcousticPulse pulse : activePulses) {
            if (candidate == null) {
                candidate = pulse;
                continue;
            }
            // Compare priority (lower priority evicted first)
            if (pulse.getCategory().getPriority() < candidate.getCategory().getPriority()) {
                candidate = pulse;
            } else if (pulse.getCategory().getPriority() == candidate.getCategory().getPriority()) {
                // If same priority, evict older pulse
                if (pulse.getLastTriggerTimeMs() < candidate.getLastTriggerTimeMs()) {
                    candidate = pulse;
                }
            }
        }

        if (candidate != null) {
            activePulses.remove(candidate);
        }
    }

    /**
     * Ticks the engine and purges expired acoustic waves.
     */
    public void tick(long nowMs) {
        activePulses.removeIf(pulse -> pulse.isExpired(nowMs));
    }

    /**
     * Ticks with current wall clock time.
     */
    public void tick() {
        tick(System.currentTimeMillis());
    }

    /**
     * Associates or enriches subtitle information for the most recent matching pulse.
     */
    public void enrichSubtitle(String soundId, String subtitleText, double x, double y, double z) {
        if (subtitleText == null || subtitleText.isEmpty()) return;
        long now = System.currentTimeMillis();

        for (AcousticPulse pulse : activePulses) {
            if (pulse.isExpired(now)) continue;
            if (soundId == null || pulse.getSoundId().equalsIgnoreCase(soundId)) {
                double dist = pulse.getPosition().distanceTo(x, y, z);
                if (dist <= 6.0D) {
                    // Update or refine category if subtitle revealed new stealth hazard
                    if (pulse.getCategory() != AudioCategory.CREEPING) {
                        AudioCategory refined = SoundClassifier.classify(pulse.getSoundId(), null, subtitleText);
                        if (refined == AudioCategory.CREEPING) {
                            activePulses.remove(pulse);
                            AcousticPulse updated = new AcousticPulse(pulse.getSoundId(), subtitleText,
                                    pulse.getX(), pulse.getY(), pulse.getZ(),
                                    refined, pulse.getInitialVolume(), pulse.getInitialPitch(), now);
                            activePulses.add(updated);
                            return;
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * Thread-safe snapshot of currently active acoustic pulses.
     */
    public List<AcousticPulse> getActivePulses() {
        return new ArrayList<>(activePulses);
    }

    public SoundRadarConfig getConfig() {
        return config;
    }

    public SoundFilterManager getFilterManager() {
        return filterManager;
    }

    public PulseHistoryBuffer getHistoryBuffer() {
        return historyBuffer;
    }

    public void clear() {
        activePulses.clear();
        historyBuffer.clear();
    }
}
