package me.rred.splinter.client.utils;

import java.awt.*;

public class SplinterColors {
    // raw Splinter palette
    public static int LIGHT = 0xFFC5D8E0;
    public static int MID_LIGHT = 0xFF7B8FC0;
    public static int MID = 0xFF5E6FAD;
    public static int MID_DARK = 0xFF4A5494;
    public static int DARK = 0xFF363A78;
    public static int DEEP = 0xFF252460;
    public static int DEEPER = 0xFF1A1848;
    public static int DARKEST = 0xFF140F2E;

    // default colors
    public static int BORDER = DARKEST;
    public static int BORDER_HOVER = LIGHT;
    public static int FILL = MID;
    public static int PANEL_BG = DEEP;
    public static int TEXT = LIGHT;

    public static int alpha(int color, int alpha) {
        // alpha will be of the form 0xFF or 0xCC
        // 0xFF -> 0x00000FF
        int mask = 0x00FFFFFF;
        return (alpha << 24) | (color & mask);
    }

    public static int pulse(int color, float speed) {
        float time = (System.currentTimeMillis() % (long)(1000 / speed)) / (1000f / speed);
        float brightness = 0.85f + 0.15f * (float) Math.sin(time * 2 * Math.PI);
        // brightness between 0.7 and 1.0

        int r = (int)(((color >> 16) & 0xFF) * brightness);
        int g = (int)(((color >> 8) & 0xFF) * brightness);
        int b = (int)((color & 0xFF) * brightness);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
