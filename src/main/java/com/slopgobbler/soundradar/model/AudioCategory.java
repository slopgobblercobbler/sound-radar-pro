package com.slopgobbler.soundradar.model;

/**
 * Tactical audio category classification for SoundRadar Pro.
 * Defines priority, default ARGB color scheme, HUD indicators, and retention lifetime.
 */
public enum AudioCategory {

    /**
     * Stealth hazards, primed explosives, sneaking enemies, and high-threat apex entities.
     * Examples: Creeper priming/fuse, Warden sniffing/heartbeat/sonic boom, TNT ignition, Sculk shrieker.
     */
    CREEPING(
        "Creeping / Lethal Hazard",
        0xFFFF1744, // Intense Vibrant Crimson-Amber
        0x55FF1744,
        5,
        4000L,
        "!"
    ),

    /**
     * Hostile mobs and aggressive entities.
     * Examples: Zombies, Skeletons, Spiders, Blazes, Ghasts, Pillagers, Endermen.
     */
    HOSTILE(
        "Hostile Mob",
        0xFFFF3333, // Bright Tactical Red
        0x44FF3333,
        4,
        2800L,
        "H"
    ),

    /**
     * Player interactions, actions, footsteps, eating, weapon swings, bow shots, tool breaks.
     */
    PLAYER(
        "Player Action",
        0xFF00E5FF, // High-visibility Cyan / Aqua
        0x4400E5FF,
        3,
        2200L,
        "P"
    ),

    /**
     * Peaceful and farm entities.
     * Examples: Cows, Sheep, Pigs, Chickens, Villagers, Horses, Cats, Wolves.
     */
    PASSIVE(
        "Passive Creature",
        0xFF44FF44, // Tactical Lime Green
        0x3344FF44,
        2,
        2000L,
        "+"
    ),

    /**
     * Environmental cues, cave ambiance, weather, water, lava, pistons, redstone, portals.
     */
    ENVIRONMENTAL(
        "Environmental / Cave",
        0xFF88AAEE, // Muted Steel Blue
        0x2288AAEE,
        1,
        1800L,
        "~"
    );

    private final String displayName;
    private final int colorArgb;
    private final int glowColorArgb;
    private final int priority;
    private final long defaultLifetimeMs;
    private final String iconGlyph;

    AudioCategory(String displayName, int colorArgb, int glowColorArgb, int priority, long defaultLifetimeMs, String iconGlyph) {
        this.displayName = displayName;
        this.colorArgb = colorArgb;
        this.glowColorArgb = glowColorArgb;
        this.priority = priority;
        this.defaultLifetimeMs = defaultLifetimeMs;
        this.iconGlyph = iconGlyph;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColorArgb() {
        return colorArgb;
    }

    public int getGlowColorArgb() {
        return glowColorArgb;
    }

    public int getPriority() {
        return priority;
    }

    public long getDefaultLifetimeMs() {
        return defaultLifetimeMs;
    }

    public String getIconGlyph() {
        return iconGlyph;
    }

    /**
     * Returns color formatted with variable alpha [0.0, 1.0].
     */
    public int getColorWithAlpha(float alpha) {
        int a = Math.max(0, Math.min(255, (int)(alpha * 255.0f)));
        return (a << 24) | (colorArgb & 0x00FFFFFF);
    }
}
