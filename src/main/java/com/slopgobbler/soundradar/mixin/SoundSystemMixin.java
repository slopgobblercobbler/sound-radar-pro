package com.slopgobbler.soundradar.mixin;

import com.slopgobbler.soundradar.engine.SoundRadarEngine;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into SoundSystem to intercept client-side sound emissions in real time.
 */
@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    private void onPlaySound(SoundInstance sound, CallbackInfo ci) {
        if (sound == null || sound.isRelative()) {
            return;
        }

        String soundId = sound.getId() != null ? sound.getId().toString() : "unknown";
        String category = sound.getCategory() != null ? sound.getCategory().name() : "NEUTRAL";
        double x = sound.getX();
        double y = sound.getY();
        double z = sound.getZ();
        float volume = sound.getVolume();
        float pitch = sound.getPitch();

        SoundRadarEngine.getInstance().onSoundPlayed(soundId, category, x, y, z, volume, pitch, null);
    }
}
