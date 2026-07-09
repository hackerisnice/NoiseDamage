package com.panda.noisedamage.hud;

import com.panda.noisedamage.NoiseDamageModClient;
import com.panda.noisedamage.config.NoiseConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class NoiseHudOverlay {
    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            float noise = NoiseDamageModClient.clientNoiseLevel;
            int screenWidth = client.getWindow().getScaledWidth();

            int barX = screenWidth - 82;
            int barY = 5;
            int barWidth = 77;
            int barHeight = 12;

            // 背景
            drawContext.fill(barX, barY, barX + barWidth, barY + barHeight, 0xAA000000);

            // 填充条
            float maxDisplay = NoiseConfig.damageThreshold;
            float fillRatio = Math.min(noise / maxDisplay, 1.0f);
            int fillWidth = (int) ((barWidth - 4) * fillRatio);
            int fillColor = noise >= NoiseConfig.damageThreshold ? 0xFFDD3333 : 0xFF33AA33;
            if (fillWidth > 0) {
                drawContext.fill(barX + 2, barY + 2, barX + 2 + fillWidth, barY + barHeight - 2, fillColor);
            }

            // 数字
            TextRenderer textRenderer = client.textRenderer;
            String text = String.format("%.0f dB", noise);
            int textColor = noise >= NoiseConfig.damageThreshold ? 0xFF5555 : 0xFFFFFF;
            drawContext.drawTextWithShadow(textRenderer, text, barX + 3, barY + barHeight + 2, textColor);
        });
    }
}
