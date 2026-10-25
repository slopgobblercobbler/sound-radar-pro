package com.slopgobbler.soundradar.engine;

import com.slopgobbler.soundradar.config.SoundRadarConfig;
import com.slopgobbler.soundradar.model.AudioCategory;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * High-performance sound filter evaluator.
 * Evaluates volume thresholds, category toggles, wildcards, blacklists, and whitelists.
 */
public class SoundFilterManager {

    private final SoundRadarConfig config;

    public SoundFilterManager(SoundRadarConfig config) {
        this.config = config != null ? config : SoundRadarConfig.getInstance();
    }

    /**
     * Determines whether an audio cue should be tracked and rendered.
     *
     * @param soundId unique sound identifier (e.g. "minecraft:entity.creeper.primed")
     * @param volume raw sound volume
     * @param category classified audio category
     * @return true if acoustic pulse passes all active filters
     */
    public boolean shouldTrack(String soundId, float volume, AudioCategory category) {
        if (!config.isRadarEnabled()) {
            return false;
        }

        // 1. Volume threshold
        if (volume < config.getVolumeThreshold()) {
            return false;
        }

        // 2. Category toggle
        if (!config.isCategoryEnabled(category)) {
            return false;
        }

        String id = soundId != null ? soundId.toLowerCase(Locale.ROOT) : "";

        // 3. Whitelist check (if whitelist mode is active)
        if (config.isWhitelistMode()) {
            boolean matchedWhitelist = false;
            for (String pattern : config.getWhitelist()) {
                if (matchesPattern(id, pattern)) {
                    matchedWhitelist = true;
                    break;
                }
            }
            if (!matchedWhitelist) {
                return false;
            }
        }

        // 4. Blacklist check
        for (String pattern : config.getBlacklist()) {
            if (matchesPattern(id, pattern)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Matches a sound ID against a pattern string.
     * Supports standard wildcards ('*' matches anything, '?' matches single character).
     */
    public static boolean matchesPattern(String text, String patternStr) {
        if (text == null || patternStr == null) return false;
        String p = patternStr.trim().toLowerCase(Locale.ROOT);
        String t = text.trim().toLowerCase(Locale.ROOT);

        if (p.equals("*") || p.equals(t)) {
            return true;
        }

        // Convert wildcard to regex if it contains * or ?
        if (p.contains("*") || p.contains("?")) {
            try {
                StringBuilder sb = new StringBuilder("^");
                for (int i = 0; i < p.length(); i++) {
                    char c = p.charAt(i);
                    if (c == '*') {
                        sb.append(".*");
                    } else if (c == '?') {
                        sb.append(".");
                    } else if ("\\.[]{}()+-^$|".indexOf(c) != -1) {
                        sb.append("\\").append(c);
                    } else {
                        sb.append(c);
                    }
                }
                sb.append("$");
                return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE).matcher(t).matches();
            } catch (Exception ignored) {}
        }

        // Direct containment fallback
        return t.contains(p);
    }
}
