# 📡 SoundRadar Pro

> **Tactical 3D Acoustic Radar & Directional Audio Visualizer for Minecraft (Fabric 1.21.1+)**  
> *Engineered by `slopgobblercobbler`*

[![Build](https://github.com/slopgobbler/sound-radar-pro/actions/workflows/build.yml/badge.svg)](https://github.com/slopgobbler/sound-radar-pro/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Fabric: 1.21.1](https://img.shields.io/badge/Fabric-1.21.1+-blue.svg)](https://fabricmc.net/)
[![Java: 21](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)

---

## 🎯 Overview

**SoundRadar Pro** is a high-performance, client-side tactical acoustic radar and directional audio visualizer mod for Minecraft Fabric. By intercepting client-side `SoundInstance` events and subtitle cues directly at the sound engine layer, SoundRadar Pro translates raw acoustic signals into real-time 3D spatial vectors, classifies threat levels, and visualizes them on a sleek HUD radar compass, a horizontal heading strip, and expanding in-world acoustic wave rings.

Whether navigating dark cave systems, defending against stealth creepers, hunting subterranean Wardens, or tracking players in competitive multiplayer, SoundRadar Pro gives you unmatched acoustic situational awareness.

---

## ✨ Features

### 1. 🧭 Dual Tactical HUD Modes
- **Circular Polar Radar Widget**:
  - Concentric distance range rings (33%, 66%, 100% of detection radius).
  - Dynamically rotating cardinal compass rose (`N`, `E`, `S`, `W`) linked to your camera's true world heading.
  - Forward crosshair notch and player origin marker.
  - Directional sound blips with elevation carets (`▲` for elevated sounds, `▼` for subterranean sounds).
  - Configurable screen docking (`TOP_RIGHT`, `TOP_LEFT`, `BOTTOM_RIGHT`, `BOTTOM_LEFT`, `CENTER_BOTTOM`).
- **Tactical Heading Tape (Compass Strip)**:
  - Minimalist horizontal compass ribbon centered at the top of your screen.
  - Continuous 120° field-of-view angular mapping with cardinal heading ticks.
  - Linear sound projection with distance tags (e.g. `! 14m`).
- **Display Modes**: Toggle between `Circular Radar`, `Tactical Heading Tape`, `Dual (Both)`, or `Off`.

### 2. 🌊 3D In-World Acoustic Waves
- Renders expanding horizontal acoustic wave rings and spherical ripples at the exact 3D coordinates where sound events originate.
- Easing alpha decay with cubic fade-out over time.
- Localized micro-acoustic auras for critical high-threat audio cues.

### 3. 🏷️ Multi-Tier Acoustic Classification Engine
Audio signals are classified in real time into 5 prioritized tactical bands:

| Category | Priority | Color Scheme | Example Cues |
|---|:---:|:---:|---|
| **CREEPING / HAZARD** | **5 (Highest)** | Crimson-Amber Strobe (`0xFFFF1744`) | Creeper primed/fuse, Warden heartbeat/sniff/sonic boom, TNT ignition, Sculk shrieker |
| **HOSTILE** | **4** | Bright Tactical Red (`0xFFFF3333`) | Zombies, skeletons, spiders, blazes, ghasts, pillagers, endermen, witches |
| **PLAYER** | **3** | High-vis Cyan / Aqua (`0xFF00E5FF`) | Footsteps, elytra glide, weapon swings, bow shots, trident throws, eating/drinking |
| **PASSIVE** | **2** | Tactical Lime Green (`0xFF44FF44`) | Cows, sheep, pigs, villagers, horses, cats, peaceful wildlife |
| **ENVIRONMENTAL** | **1** | Muted Steel Blue (`0xFF88AAEE`) | Cave ambience, flowing lava/water, rain, thunder, pistons, dispensers, portals |

### 4. 🧮 3D Vector Geometry & Distance Falloff
- **Absolute & Relative Bearing**: Translates world positions into relative yaw (`-180.0°` to `+180.0°`) where `0.0°` points straight ahead.
- **Elevation Differential**: Measures vertical displacement and calculates pitch elevation angles (`-90.0°` to `+90.0°`).
- **Acoustic Loudness Attenuation**: Simulates realistic acoustic falloff based on inverse distance and source volume.
- **Spatial Clustering**: Rapid repetitive sounds from the same source (such as sprinting footsteps or repeated zombie groans) are clustered within a configurable 2.5m distance and 250ms time window, preventing visual clutter while keeping the ping refreshed.

### 5. 🛡️ Noise Filtering & Blacklist / Whitelist
- **Volume Cutoff Threshold**: Configurable minimum loudness threshold (e.g. `0.05` / 5%) to eliminate background hum.
- **Pattern Matching Blacklist**: Wildcard (`*`, `?`) and regex filtering (defaults ignore `minecraft:music.*` and `minecraft:ui.*`).
- **Whitelist Mode**: Restrict tracking exclusively to explicitly whitelisted sound patterns.
- **Category Muting**: Independently toggle tracking for Creeping, Hostile, Player, Passive, or Environmental signals.

### 6. 🎮 Interactive GUI & ModMenu Integration
- Built-in configuration screen (`SoundRadarConfigScreen`) accessible directly in-game.
- Seamlessly registered with **ModMenu**.
- Hotkey triggers for real-time mode switching and HUD adjustments.

---

## ⌨️ Default Keybindings

| Key | Action | Description |
|:---:|---|---|
| <kbd>H</kbd> | **Toggle Radar HUD** | Quickly enables or disables the acoustic radar HUD |
| <kbd>J</kbd> | **Cycle Radar Mode** | Cycles between `Circular`, `Compass Strip`, `Dual`, and `Off` |
| <kbd>K</kbd> | **Toggle 3D World Waves** | Toggles in-world 3D expanding acoustic wave rings |
| <kbd>U</kbd> | **Open Config Screen** | Opens the interactive SoundRadar Pro settings menu |

*(All keybindings can be customized in the vanilla Controls > Key Binds menu under the **SoundRadar Pro** category).*

---

## 🏗️ Architecture

```
com.slopgobbler.soundradar/
├── SoundRadarMod.java                    # Fabric ClientModInitializer entrypoint
├── config/
│   ├── SoundRadarConfig.java             # Runtime configuration data model
│   └── SoundRadarConfigHandler.java      # JSON persistence (config/soundradar.json)
├── model/
│   ├── AudioCategory.java                # Enum defining priorities, colors, and lifetimes
│   ├── AcousticPulse.java                # Immutable active acoustic ping data model
│   └── SoundClassifier.java              # Pattern matcher for tactical sound classification
├── math/
│   ├── Vector3d.java                     # High-performance immutable 3D double vector
│   └── RadarVectorMath.java              # Bearings, elevations, polar and tape projections
├── engine/
│   ├── SoundRadarEngine.java             # Core acoustic processing & clustering engine
│   ├── SoundFilterManager.java           # Volume cutoff, wildcard, blacklist & whitelist filters
│   └── PulseHistoryBuffer.java           # Circular telemetry buffer & category statistics
├── render/
│   ├── RenderColorUtil.java              # Color packing, alpha easing, warning strobe math
│   ├── RadarCompassWidget.java           # Circular polar radar HUD widget
│   ├── RadarHudOverlay.java              # Main HUD renderer coordinating display modes
│   └── InWorldPulseRenderer.java         # 3D expanding acoustic ripple world visualizer
├── gui/
│   └── SoundRadarConfigScreen.java       # Interactive in-game settings screen
├── event/
│   └── KeybindHandler.java               # GLFW keybinding registrations & tick listeners
├── compat/
│   └── SoundRadarModMenuCompat.java      # ModMenu API integration factory
└── mixin/
    ├── SoundSystemMixin.java             # Intercepts SoundSystem.play(SoundInstance)
    └── SubtitlesHudMixin.java            # Intercepts SubtitlesHud.onSoundPlayed(...)
```

---

## 🧪 Unit Testing

SoundRadar Pro includes a comprehensive automated test suite verifying mathematical calculations, signal classifications, and clustering behavior:

- **`RadarVectorMathTest`**:
  - Euclidean 3D and 2D horizontal distance calculations.
  - Minecraft coordinate absolute bearing (South = 0°, West = +90°, North = 180°, East = -90°).
  - Camera relative bearing wrapping (`-180°` to `+180°`).
  - Polar radar screen projection offsets (`(0, -r)` ahead, `(+r, 0)` right, `(0, +r)` behind).
  - Horizontal compass strip linear projection with FOV visibility clipping.
  - Elevation angle and height differential chevrons (`▲` elevated, `▼` subterranean).
- **`SoundClassifierTest`**:
  - Stealth hazard identification (`creeper.primed`, `warden.heartbeat`, `tnt.primed`, `sculk_shrieker.shriek`).
  - Subtitle keyword parsing ("Creeper hisses", "TNT fizzes").
  - Hostile mob categorization (zombies, skeletons, spiders, blazes, ghasts).
  - Player action categorization (bow shots, trident throws, eating, armor equip).
  - Passive creature categorization (cows, sheep, villagers).
  - Environmental cue categorization (cave ambiance, lava crackle, pistons).
- **`SoundFilterManagerTest`**:
  - Volume threshold rejection and acceptance.
  - Category mute/solo toggles.
  - Wildcard pattern matching (`minecraft:music.*`, `*bell*`).
  - Whitelist mode enforcement.
- **`AcousticPulseTest`**:
  - Pulse life cycle, elapsed age, and quadratic ease-out decay.
  - Engine spatial clustering within distance and time thresholds.
  - Expanding acoustic wave radius progression.
- **`SoundRadarConfigTest`**:
  - Display mode cycling and boundary clamping.

---

## 🔨 Building & Verification

### Option A: Gradle Loom Build
```bash
# Clone the repository
git clone https://github.com/slopgobblercobbler/sound-radar-pro.git
cd sound-radar-pro

# Build release JAR and execute JUnit test suite
./gradlew build
```
The compiled mod JAR will be located at:
```
build/libs/sound-radar-pro-1.0.0.jar
```

### Option B: Standalone Native Python Builder
You can build and test the mod directly without waiting for a full Gradle daemon bootstrap:
```bash
python build_native_mod.py
```
This standalone script compiles the sources against local Fabric libraries, runs the self-verifying test suite, generates the release JAR, and outputs the SHA-256 checksum.

---

## ⚙️ Configuration (`config/soundradar.json`)

```json
{
  "radarEnabled": true,
  "displayMode": "CIRCULAR",
  "hudPosition": "TOP_RIGHT",
  "radarScale": 1.0,
  "radarRadiusPixels": 54,
  "maxDetectionRange": 48.0,
  "volumeThreshold": 0.05,
  "inWorldPulsesEnabled": true,
  "maxInWorldWaveRadius": 3.5,
  "showHeightChevrons": true,
  "showSoundLabels": true,
  "showDistanceLabels": true,
  "showCompassCardinals": true,
  "trackCreeping": true,
  "trackHostile": true,
  "trackPlayer": true,
  "trackPassive": true,
  "trackEnvironmental": true,
  "maxActivePulses": 64,
  "lifetimeMultiplier": 1.0,
  "clusterDistanceThreshold": 2.5,
  "clusterTimeWindowMs": 250,
  "whitelistMode": false,
  "whitelist": [],
  "blacklist": [
    "minecraft:music.*",
    "minecraft:ui.*",
    "minecraft:ambient.underwater.loop.*"
  ]
}
```

---

## 📄 License

SoundRadar Pro is open-source software licensed under the **MIT License**. See [LICENSE](LICENSE) for details.
