package me.rred.splinter.client.utils;

import net.minecraft.client.font.TextRenderer;

public class TruncateText {
    public static String truncate(String text, int maxWidth, TextRenderer textRenderer) {
        if (textRenderer.getWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        while (textRenderer.getWidth(text + ellipsis) > maxWidth && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ellipsis;
    }
}
