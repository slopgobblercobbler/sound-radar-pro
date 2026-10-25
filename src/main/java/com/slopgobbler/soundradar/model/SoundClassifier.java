package com.slopgobbler.soundradar.model;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Intelligent acoustic signal classifier for SoundRadar Pro.
 * Classifies raw sound IDs, categories, and subtitle cues into tactical priority bands:
 * CREEPING (Stealth hazards), HOSTILE, PLAYER, PASSIVE, and ENVIRONMENTAL.
 */
public final class SoundClassifier {

    private SoundClassifier() {}

    // Precompiled fast matching regex patterns for creeping / hazard cues
    private static final Pattern CREEPING_PATTERN = Pattern.compile(
            ".*(creeper\\.(primed|fuse)|warden\\.(heartbeat|sniff|sonic_boom|emerge|roar|agitated|tendril_clicks)|" +
            "tnt\\.primed|sculk_shrieker\\.shriek|sculk_sensor\\.clicking|phantom\\.(swoop|flap)|" +
            "wither\\.spawn|end_crystal\\.explode).*",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CREEPING_SUBTITLE_PATTERN = Pattern.compile(
            ".*(hisses|fuse|fizz|creeper|heartbeat|warden|shriek|primed|ignites).*",
            Pattern.CASE_INSENSITIVE
    );

    // Hostile patterns
    private static final Pattern HOSTILE_PATTERN = Pattern.compile(
            ".*(entity\\.(zombie|skeleton|spider|creeper|enderman|witch|blaze|ghast|pillager|vindicator|" +
            "evoker|ravager|vex|slime|magma_cube|drowned|husk|stray|piglin_brute|wither|" +
            "breeze|bogged|silverfish|endermite|shulker|guardian|elder_guardian|hoglin|zoglin|" +
            "phantom|warden)).*",
            Pattern.CASE_INSENSITIVE
    );

    // Player action patterns
    private static final Pattern PLAYER_PATTERN = Pattern.compile(
            ".*(entity\\.player\\.|item\\.(bow|crossbow|trident|shield|armor|spyglass|totem)|" +
            "entity\\.(arrow|snowball|splash_potion|ender_pearl)\\.throw|" +
            "entity\\.generic\\.(eat|drink|swim|splash)|" +
            "item\\.bundle\\.|item\\.firework_rocket).*",
            Pattern.CASE_INSENSITIVE
    );

    // Passive creature patterns
    private static final Pattern PASSIVE_PATTERN = Pattern.compile(
            ".*(entity\\.(cow|sheep|pig|chicken|villager|wandering_trader|horse|donkey|mule|" +
            "llama|cat|wolf|ocelot|bat|parrot|allay|bee|axolotl|glow_squid|squid|strider|" +
            "dolphin|polar_bear|panda|fox|frog|tadpole|camel|armadillo|sniffer|" +
            "iron_golem|snow_golem|mooshroom)).*",
            Pattern.CASE_INSENSITIVE
    );

    // Environmental patterns
    private static final Pattern ENVIRONMENTAL_PATTERN = Pattern.compile(
            ".*(ambient\\.|weather\\.|block\\.(lava|water|fire|dripstone|bell|piston|dispenser|" +
            "portal|campfire|furnace|anvil|beacon|lightning)).*",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Classifies a sound based on its identifier, sound category, and optional subtitle.
     *
     * @param soundId unique sound identifier, e.g. "minecraft:entity.creeper.primed"
     * @param categoryName vanilla SoundCategory name, e.g. "HOSTILE", "PLAYERS", "WEATHER"
     * @param subtitleText translated or raw subtitle text if available
     * @return tactical AudioCategory
     */
    public static AudioCategory classify(String soundId, String categoryName, String subtitleText) {
        String cleanId = soundId != null ? soundId.toLowerCase(Locale.ROOT) : "";
        String cleanSub = subtitleText != null ? subtitleText.toLowerCase(Locale.ROOT) : "";
        String cleanCat = categoryName != null ? categoryName.toUpperCase(Locale.ROOT) : "";

        // 1. Highest Priority: Lethal Creeping and Stealth Hazards
        if (CREEPING_PATTERN.matcher(cleanId).matches()) {
            return AudioCategory.CREEPING;
        }
        if (!cleanSub.isEmpty() && CREEPING_SUBTITLE_PATTERN.matcher(cleanSub).matches()) {
            return AudioCategory.CREEPING;
        }

        // 2. Player actions and interactions
        if ("PLAYERS".equals(cleanCat) || PLAYER_PATTERN.matcher(cleanId).matches()) {
            return AudioCategory.PLAYER;
        }

        // 3. Hostile entities
        if ("HOSTILE".equals(cleanCat) || HOSTILE_PATTERN.matcher(cleanId).matches()) {
            return AudioCategory.HOSTILE;
        }

        // 4. Passive animals and peaceful creatures
        if ("NEUTRAL".equals(cleanCat) && PASSIVE_PATTERN.matcher(cleanId).matches()) {
            return AudioCategory.PASSIVE;
        }
        if (PASSIVE_PATTERN.matcher(cleanId).matches()) {
            return AudioCategory.PASSIVE;
        }

        // 5. Environmental & Ambiance
        if ("WEATHER".equals(cleanCat) || "AMBIENT".equals(cleanCat) || "BLOCKS".equals(cleanCat)
                || ENVIRONMENTAL_PATTERN.matcher(cleanId).matches()) {
            return AudioCategory.ENVIRONMENTAL;
        }

        // Fallback checks
        if (cleanId.contains("step") || cleanId.contains("jump") || cleanId.contains("hit")) {
            return AudioCategory.PLAYER;
        }

        return AudioCategory.ENVIRONMENTAL;
    }

    /**
     * Convenient overload for classifying sound with soundId and categoryName.
     */
    public static AudioCategory classify(String soundId, String categoryName) {
        return classify(soundId, categoryName, null);
    }

    /**
     * Simplified overload for classifying sound with soundId alone.
     */
    public static AudioCategory classify(String soundId) {
        return classify(soundId, null, null);
    }
}
