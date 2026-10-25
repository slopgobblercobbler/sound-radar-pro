package com.slopgobbler.soundradar.compat;

import com.slopgobbler.soundradar.gui.SoundRadarConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * ModMenu integration entrypoint for SoundRadar Pro.
 * Exposes the in-game configuration screen to the ModMenu GUI list.
 */
@Environment(EnvType.CLIENT)
public class SoundRadarModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SoundRadarConfigScreen::new;
    }
}
