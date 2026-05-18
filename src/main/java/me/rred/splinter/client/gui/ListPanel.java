package me.rred.splinter.client.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public abstract class ListPanel {
    protected int x, y, width, height;
    protected int scrollOffset = 0;
    protected static final int LINE_HEIGHT = 14;

    public ListPanel(int x, int y , int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract int getItemCount();

    public void scroll(double amount) {
        int maxScroll = Math.max(0, getItemCount() * (LINE_HEIGHT + 1) - height + 1);
        scrollOffset -= (int) (amount * LINE_HEIGHT);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    public abstract void render(MatrixStack matrixStack, TextRenderer textRenderer, int mouseX, int mouseY);

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }

    public int getIndexAt(double mouseY) {
        return (int)((mouseY - y + scrollOffset) / LINE_HEIGHT);
    }
}
