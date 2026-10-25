package com.slopgobbler.soundradar.render;

import com.slopgobbler.soundradar.config.SoundRadarConfig;
import com.slopgobbler.soundradar.engine.SoundRadarEngine;
import com.slopgobbler.soundradar.math.RadarVectorMath;
import com.slopgobbler.soundradar.model.AcousticPulse;
import com.slopgobbler.soundradar.model.AudioCategory;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 3D tactical in-world acoustic pulse and spherical wave visualizer.
 * Projects expanding acoustic ripples and sound wave rings directly into the Minecraft world.
 */
public class InWorldPulseRenderer {

    private final SoundRadarConfig config;
    private final SoundRadarEngine engine;

    public InWorldPulseRenderer(SoundRadarConfig config, SoundRadarEngine engine) {
        this.config = config != null ? config : SoundRadarConfig.getInstance();
        this.engine = engine != null ? engine : SoundRadarEngine.getInstance();
    }

    /**
     * Renders in-world acoustic ripples during world render pass.
     */
    public void render(WorldRenderContext context) {
        if (!config.isRadarEnabled() || !config.isInWorldPulsesEnabled()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        Camera camera = context.camera();
        if (camera == null) return;
        Vec3d camPos = camera.getPos();

        MatrixStack matrices = context.matrixStack();
        if (matrices == null) return;

        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        long now = System.currentTimeMillis();
        List<AcousticPulse> pulses = engine.getActivePulses();
        double maxDist = config.getMaxDetectionRange();

        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());

        for (AcousticPulse pulse : pulses) {
            if (pulse.isExpired(now)) continue;

            double dist = RadarVectorMath.distance(camPos.x, camPos.y, camPos.z, pulse.getX(), pulse.getY(), pulse.getZ());
            if (dist > maxDist || dist < 0.2D) continue;

            float alpha = pulse.getAlpha(now);
            if (pulse.getCategory() == AudioCategory.CREEPING) {
                alpha = RenderColorUtil.calculateStrobe(now, alpha, 200);
            }
            if (alpha <= 0.02f) continue;

            double radius = pulse.getExpandingWaveRadius(now, config.getMaxInWorldWaveRadius());
            if (radius <= 0.05D) radius = 0.1D;

            int argb = pulse.getCategory().getColorArgb();
            int red = (argb >> 16) & 0xFF;
            int green = (argb >> 8) & 0xFF;
            int blue = argb & 0xFF;

            matrices.push();
            matrices.translate(pulse.getX() - camPos.x, pulse.getY() - camPos.y, pulse.getZ() - camPos.z);

            drawAcousticWaveRing(matrices, buffer, radius, red, green, blue, alpha);

            matrices.pop();
        }
    }

    /**
     * Draws an expanding circular acoustic wave ring on the X-Z horizontal plane.
     */
    private void drawAcousticWaveRing(MatrixStack matrices, VertexConsumer buffer,
                                      double radius, int r, int g, int b, float a) {
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();
        int segments = 16;
        double angleStep = (Math.PI * 2.0) / segments;

        for (int i = 0; i < segments; i++) {
            double a1 = i * angleStep;
            double a2 = (i + 1) * angleStep;

            float x1 = (float) (radius * Math.sin(a1));
            float z1 = (float) (radius * Math.cos(a1));
            float x2 = (float) (radius * Math.sin(a2));
            float z2 = (float) (radius * Math.cos(a2));

            buffer.vertex(posMatrix, x1, 0.05f, z1)
                  .color(r, g, b, (int) (a * 255.0f))
                  .normal(0.0f, 1.0f, 0.0f);

            buffer.vertex(posMatrix, x2, 0.05f, z2)
                  .color(r, g, b, (int) (a * 255.0f))
                  .normal(0.0f, 1.0f, 0.0f);
        }
    }

    /**
     * Spawns localized directional micro-acoustic particles in the world for immediate feedback.
     */
    public static void spawnAcousticAura(MinecraftClient client, AcousticPulse pulse) {
        if (client.world == null || pulse == null) return;
        if (pulse.getCategory() == AudioCategory.CREEPING) {
            client.world.addParticle(
                    ParticleTypes.SONIC_BOOM,
                    pulse.getX(), pulse.getY() + 0.1, pulse.getZ(),
                    0.0, 0.0, 0.0
            );
        }
    }
}
