package me.rred.splinter.client.timer;

import me.rred.splinter.Splinter;
import me.rred.splinter.client.SplinterClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.awt.*;

public class TimerHud {

    public static void render(MatrixStack matrixStack, TextRenderer fontRenderer) {
        if (!SplinterClient.tsm.isActive()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        float scaledHeight = client.getWindow().getScaledHeight();
        float y = scaledHeight / 2;

        String time =  formatTime(SplinterClient.tsm.getElapsedMs());
        fontRenderer.drawWithShadow(matrixStack, new LiteralText(time), 10, y, Color.WHITE.getRGB());
    }

    private static String formatTime(long ms) {
        Splinter.LOGGER.info("formatting");
        long minutes = ms / 60000;
        long seconds = (ms % 60000) / 1000;
        long millis = ms % 1000;
        return String.format("%d:%02d.%03d", minutes, seconds, millis);
    }
}
