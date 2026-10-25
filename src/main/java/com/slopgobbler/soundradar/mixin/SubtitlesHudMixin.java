package com.slopgobbler.soundradar.mixin;

import com.slopgobbler.soundradar.engine.SoundRadarEngine;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into SubtitlesHud to intercept subtitle cues and enrich radar classification.
 */
@Mixin(SubtitlesHud.class)
public class SubtitlesHudMixin {

    @Inject(method = "onSoundPlayed(Lnet/minecraft/client/sound/SoundInstance;Lnet/minecraft/client/sound/WeightedSoundSet;F)V", at = @At("HEAD"))
    private void onSubtitleSound(SoundInstance sound, WeightedSoundSet soundSet, float range, CallbackInfo ci) {
        if (sound == null || soundSet == null) return;

        Text subtitleText = soundSet.getSubtitle();
        String text = subtitleText != null ? subtitleText.getString() : "";
        if (!text.isEmpty()) {
            String soundId = sound.getId() != null ? sound.getId().toString() : null;
            SoundRadarEngine.getInstance().enrichSubtitle(soundId, text, sound.getX(), sound.getY(), sound.getZ());
        }
    }
}
