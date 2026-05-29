package me.rred.splinter.client.widgets.modals;

import me.rred.splinter.client.utils.SplinterColors;
import me.rred.splinter.client.widgets.SplinterButton;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.client.gui.DrawableHelper.fill;

public class ConfirmModal extends SplinterModal{
    private final Runnable onConfirm;

    public ConfirmModal(String message, Runnable onConfirm) {
        this.message = message;
        this.onConfirm = onConfirm;
    }

    public void openModal(int screenWidth, int screenHeight) {
        this.width = screenWidth / 5;
        this.height = screenHeight / 6;
        this.x = (screenWidth - width) / 2;
        this.y = (screenHeight - height) / 2;
        visible = true;
        this.init();
    }

    protected void init() {
        int buttonWidth = (int)(width * 0.8);
        int buttonHeight = (int)(height * 0.4);
        int buttonX = x + (width - buttonWidth) / 2; // centered
        int buttonY = y + (int)(0.9 * height) - buttonHeight; // shifted up

        confirmButton = new SplinterButton(buttonX, buttonY, buttonWidth, buttonHeight,
                new LiteralText("CONFIRM"),
                onConfirm
                );
    }

    public void render(MatrixStack matrixStack, TextRenderer textRenderer,
                       int mouseX, int mouseY) {
        // border then inside fill
        fill(matrixStack, x, y, x + width, y + height, SplinterColors.BORDER);
        fill(matrixStack, x + 1, y + 1, x + width - 1, y + height - 1, SplinterColors.PANEL_BG);

        int textX = x + (width / 2) - textRenderer.getWidth(message) / 2;
        int textY = y + (int)(height * 0.1);

        textRenderer.drawWithShadow(matrixStack, message, textX, textY, SplinterColors.TEXT);
        if (confirmButton != null) {
            confirmButton.render(matrixStack, mouseX, mouseY, 0);
        }
    }

    public boolean handleClick(double mouseX, double mouseY, int button) {
        if (confirmButton != null && confirmButton.mouseClicked(mouseX, mouseY, button)) {
            close();
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }


}
