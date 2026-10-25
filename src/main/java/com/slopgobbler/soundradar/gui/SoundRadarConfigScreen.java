package com.slopgobbler.soundradar.gui;

import com.slopgobbler.soundradar.config.SoundRadarConfig;
import com.slopgobbler.soundradar.config.SoundRadarConfigHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Interactive in-game configuration screen for SoundRadar Pro.
 * Integrated with ModMenu and hotkey binding.
 */
@Environment(EnvType.CLIENT)
public class SoundRadarConfigScreen extends Screen {

    private final Screen parent;
    private final SoundRadarConfig config;

    private static final double[] RANGE_PRESETS = new double[]{16.0, 32.0, 48.0, 64.0, 96.0, 128.0};
    private static final float[] VOLUME_PRESETS = new float[]{0.0f, 0.05f, 0.10f, 0.20f, 0.35f};
    private static final float[] SCALE_PRESETS = new float[]{0.8f, 1.0f, 1.25f, 1.5f};

    public SoundRadarConfigScreen(Screen parent) {
        super(Text.literal("SoundRadar Pro Configuration"));
        this.parent = parent;
        this.config = SoundRadarConfig.getInstance();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int btnW = 150;
        int btnH = 20;
        int spacing = 24;

        // Row 1: Radar Master Toggle & Display Mode
        addDrawableChild(ButtonWidget.builder(getRadarToggleText(), btn -> {
            config.setRadarEnabled(!config.isRadarEnabled());
            btn.setMessage(getRadarToggleText());
        }).dimensions(centerX - btnW - 5, startY, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(getDisplayModeText(), btn -> {
            config.cycleDisplayMode();
            btn.setMessage(getDisplayModeText());
        }).dimensions(centerX + 5, startY, btnW, btnH).build());

        // Row 2: In-World 3D Pulses & HUD Position
        addDrawableChild(ButtonWidget.builder(getInWorldText(), btn -> {
            config.setInWorldPulsesEnabled(!config.isInWorldPulsesEnabled());
            btn.setMessage(getInWorldText());
        }).dimensions(centerX - btnW - 5, startY + spacing, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(getPositionText(), btn -> {
            cyclePosition();
            btn.setMessage(getPositionText());
        }).dimensions(centerX + 5, startY + spacing, btnW, btnH).build());

        // Row 3: Detection Range & Volume Cutoff Threshold
        addDrawableChild(ButtonWidget.builder(getRangeText(), btn -> {
            cycleRange();
            btn.setMessage(getRangeText());
        }).dimensions(centerX - btnW - 5, startY + spacing * 2, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(getVolumeText(), btn -> {
            cycleVolume();
            btn.setMessage(getVolumeText());
        }).dimensions(centerX + 5, startY + spacing * 2, btnW, btnH).build());

        // Row 4: Category Toggles (Creeping & Hostile)
        addDrawableChild(ButtonWidget.builder(getCategoryText("Creeping Hazard", config.isTrackCreeping()), btn -> {
            config.setTrackCreeping(!config.isTrackCreeping());
            btn.setMessage(getCategoryText("Creeping Hazard", config.isTrackCreeping()));
        }).dimensions(centerX - btnW - 5, startY + spacing * 3, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(getCategoryText("Hostile Mobs", config.isTrackHostile()), btn -> {
            config.setTrackHostile(!config.isTrackHostile());
            btn.setMessage(getCategoryText("Hostile Mobs", config.isTrackHostile()));
        }).dimensions(centerX + 5, startY + spacing * 3, btnW, btnH).build());

        // Row 5: Category Toggles (Player & Environmental)
        addDrawableChild(ButtonWidget.builder(getCategoryText("Player Cues", config.isTrackPlayer()), btn -> {
            config.setTrackPlayer(!config.isTrackPlayer());
            btn.setMessage(getCategoryText("Player Cues", config.isTrackPlayer()));
        }).dimensions(centerX - btnW - 5, startY + spacing * 4, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(getCategoryText("Environmental", config.isTrackEnvironmental()), btn -> {
            config.setTrackEnvironmental(!config.isTrackEnvironmental());
            btn.setMessage(getCategoryText("Environmental", config.isTrackEnvironmental()));
        }).dimensions(centerX + 5, startY + spacing * 4, btnW, btnH).build());

        // Row 6: Scale preset
        addDrawableChild(ButtonWidget.builder(getScaleText(), btn -> {
            cycleScale();
            btn.setMessage(getScaleText());
        }).dimensions(centerX - (btnW / 2), startY + spacing * 5, btnW, btnH).build());

        // Bottom Controls: Done & Save
        addDrawableChild(ButtonWidget.builder(Text.literal("Done & Save"), btn -> {
            SoundRadarConfigHandler.saveConfig();
            if (this.client != null) {
                this.client.setScreen(this.parent);
            }
        }).dimensions(centerX - 100, this.height - 32, 200, 20).build());
    }

    private Text getRadarToggleText() {
        return Text.literal("Radar: " + (config.isRadarEnabled() ? "§aEnabled" : "§cDisabled"));
    }

    private Text getDisplayModeText() {
        return Text.literal("Mode: §e" + config.getDisplayMode().getLabel());
    }

    private Text getInWorldText() {
        return Text.literal("3D Waves: " + (config.isInWorldPulsesEnabled() ? "§aEnabled" : "§cDisabled"));
    }

    private Text getPositionText() {
        return Text.literal("Position: §e" + config.getHudPosition().name());
    }

    private Text getRangeText() {
        return Text.literal("Range: §b" + (int) config.getMaxDetectionRange() + "m");
    }

    private Text getVolumeText() {
        return Text.literal("Vol Threshold: §e" + (int) (config.getVolumeThreshold() * 100) + "%");
    }

    private Text getScaleText() {
        return Text.literal("HUD Scale: §e" + config.getRadarScale() + "x");
    }

    private Text getCategoryText(String name, boolean enabled) {
        return Text.literal(name + ": " + (enabled ? "§aON" : "§cOFF"));
    }

    private void cyclePosition() {
        SoundRadarConfig.HudPosition[] positions = SoundRadarConfig.HudPosition.values();
        int next = (config.getHudPosition().ordinal() + 1) % positions.length;
        config.setHudPosition(positions[next]);
    }

    private void cycleRange() {
        for (int i = 0; i < RANGE_PRESETS.length; i++) {
            if (Math.abs(config.getMaxDetectionRange() - RANGE_PRESETS[i]) < 0.1) {
                int next = (i + 1) % RANGE_PRESETS.length;
                config.setMaxDetectionRange(RANGE_PRESETS[next]);
                return;
            }
        }
        config.setMaxDetectionRange(RANGE_PRESETS[0]);
    }

    private void cycleVolume() {
        for (int i = 0; i < VOLUME_PRESETS.length; i++) {
            if (Math.abs(config.getVolumeThreshold() - VOLUME_PRESETS[i]) < 0.01) {
                int next = (i + 1) % VOLUME_PRESETS.length;
                config.setVolumeThreshold(VOLUME_PRESETS[next]);
                return;
            }
        }
        config.setVolumeThreshold(VOLUME_PRESETS[0]);
    }

    private void cycleScale() {
        for (int i = 0; i < SCALE_PRESETS.length; i++) {
            if (Math.abs(config.getRadarScale() - SCALE_PRESETS[i]) < 0.05) {
                int next = (i + 1) % SCALE_PRESETS.length;
                config.setRadarScale(SCALE_PRESETS[next]);
                return;
            }
        }
        config.setRadarScale(SCALE_PRESETS[0]);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFF00E5FF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7Tactical 3D Acoustic Radar & Directional Visualizer"),
                this.width / 2, 28, 0xFFAAAAAA);
    }

    @Override
    public void close() {
        SoundRadarConfigHandler.saveConfig();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
