package me.rred.splinter.client.widgets.modals;

import me.rred.splinter.client.widgets.SplinterButton;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public abstract class SplinterModal {
    protected int x, y, width, height;
    protected SplinterButton confirmButton;
    protected boolean visible = false;

    public abstract void openModal(int screenWidth, int screenHeight);
    public abstract void render(MatrixStack matrixStack, TextRenderer textRenderer,
                                int mouseX, int mouseY);
    public abstract boolean keyPressed(int keyCode, int scanCode, int modifiers);

    public boolean isVisible() { return visible; }
    public void close() {
        visible = false;
        confirmButton = null;
    }
}
