package com.slopgobbler.soundradar;

import com.slopgobbler.soundradar.config.SoundRadarConfig;
import com.slopgobbler.soundradar.config.SoundRadarConfigHandler;
import com.slopgobbler.soundradar.engine.SoundRadarEngine;
import com.slopgobbler.soundradar.event.KeybindHandler;
import com.slopgobbler.soundradar.render.InWorldPulseRenderer;
import com.slopgobbler.soundradar.render.RadarHudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main client-side entrypoint for SoundRadar Pro Fabric Mod.
 * Initializes the tactical 3D acoustic radar engine, HUD overlay, in-world wave renderer,
 * sound classification filters, and keybinding shortcuts.
 */
@Environment(EnvType.CLIENT)
public class SoundRadarMod implements ClientModInitializer {

    public static final String MOD_ID = "soundradar";
    public static final String MOD_NAME = "SoundRadar Pro";
    public static final String MOD_VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static SoundRadarMod instance;

    private SoundRadarConfig config;
    private SoundRadarEngine engine;
    private RadarHudOverlay hudOverlay;
    private InWorldPulseRenderer inWorldRenderer;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[{}] Initializing Tactical Acoustic Radar (v{})...", MOD_NAME, MOD_VERSION);

        // 1. Load configuration
        SoundRadarConfigHandler.loadConfig();
        this.config = SoundRadarConfig.getInstance();

        // 2. Initialize processing engine and visualizers
        this.engine = SoundRadarEngine.getInstance();
        this.hudOverlay = new RadarHudOverlay(this.config, this.engine);
        this.inWorldRenderer = new InWorldPulseRenderer(this.config, this.engine);

        // 3. Register HUD overlay renderer
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            this.hudOverlay.render(drawContext, renderTickCounter);
        });

        // 4. Register In-World 3D acoustic wave renderer
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            this.inWorldRenderer.render(context);
        });

        // 5. Register tactical keybindings
        KeybindHandler.register();

        LOGGER.info("[{}] 3D Tactical Acoustic Visualizer & Directional Radar online.", MOD_NAME);
    }

    public static SoundRadarMod getInstance() {
        return instance;
    }

    public SoundRadarConfig getConfig() {
        return config;
    }

    public SoundRadarEngine getEngine() {
        return engine;
    }

    public RadarHudOverlay getHudOverlay() {
        return hudOverlay;
    }

    public InWorldPulseRenderer getInWorldRenderer() {
        return inWorldRenderer;
    }
}
