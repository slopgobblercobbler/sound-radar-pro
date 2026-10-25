package com.slopgobbler.soundradar.event;

import com.slopgobbler.soundradar.config.SoundRadarConfig;
import com.slopgobbler.soundradar.gui.SoundRadarConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Keybinding registry and event listener for SoundRadar Pro tactical shortcuts.
 */
public final class KeybindHandler {

    private static KeyBinding toggleRadarKey;
    private static KeyBinding cycleModeKey;
    private static KeyBinding toggleInWorldKey;
    private static KeyBinding openConfigKey;

    private KeybindHandler() {}

    public static void register() {
        String category = "soundradar.key.category";

        toggleRadarKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "soundradar.key.toggle_radar",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                category
        ));

        cycleModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "soundradar.key.cycle_mode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                category
        ));

        toggleInWorldKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "soundradar.key.toggle_inworld",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                category
        ));

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "soundradar.key.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(KeybindHandler::handleInput);
    }

    private static void handleInput(MinecraftClient client) {
        if (client.player == null) return;
        SoundRadarConfig config = SoundRadarConfig.getInstance();

        while (toggleRadarKey.wasPressed()) {
            config.setRadarEnabled(!config.isRadarEnabled());
            client.player.sendMessage(Text.translatable(
                    config.isRadarEnabled() ? "soundradar.message.enabled" : "soundradar.message.disabled"
            ), true);
        }

        while (cycleModeKey.wasPressed()) {
            config.cycleDisplayMode();
            client.player.sendMessage(Text.translatable(
                    "soundradar.message.mode", config.getDisplayMode().getLabel()
            ), true);
        }

        while (toggleInWorldKey.wasPressed()) {
            config.setInWorldPulsesEnabled(!config.isInWorldPulsesEnabled());
            client.player.sendMessage(Text.translatable(
                    config.isInWorldPulsesEnabled() ? "soundradar.message.inworld_on" : "soundradar.message.inworld_off"
            ), true);
        }

        while (openConfigKey.wasPressed()) {
            client.setScreen(new SoundRadarConfigScreen(client.currentScreen));
        }
    }
}
