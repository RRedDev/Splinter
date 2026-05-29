package me.rred.splinter.client.widgets.modals;

import com.sun.jna.platform.unix.X11;
import me.rred.splinter.Splinter;
import me.rred.splinter.client.utils.SplinterColors;
import me.rred.splinter.client.widgets.SplinterButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.client.gui.DrawableHelper.fill;

public class InputModal extends SplinterModal{
    private final String allowedChars;
    private final Runnable onConfirm;
    private TextFieldWidget input;

    public InputModal(String message, Runnable onConfirm) {
        this.message = message;
        this.onConfirm = onConfirm;
        this.allowedChars = "[a-zA-z0-9_ ]";
    }

    public InputModal(String message, Runnable onConfirm, String allowedChars) {
        this.message = message;
        this.onConfirm = onConfirm;
        this.allowedChars = allowedChars;
    }

    public void openModal(int screenWidth, int screenHeight) {
        this.width = (int)(screenWidth * 0.25);
        // make room for sub message if necessary
        this.height = subMessage != null ? (int)(screenHeight * 0.35) : (int)(screenHeight * 0.25);;
        this.x = (screenWidth - width) / 2;
        this.y = (screenHeight - height) / 2;

        visible = true;
        this.init();
    }

    protected void init() {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        int buttonWidth = (int)(width * 0.6);
        int buttonHeight = (textRenderer.fontHeight * 2);
        int buttonX = x + (width - buttonWidth) / 2;
        int buttonY = y + (int)(0.9 * height) - buttonHeight;

        int inputWidth = (int)(width * 0.7);
        int inputHeight = (textRenderer.fontHeight * 2);
        int inputX = x + (width - inputWidth) / 2;
        int inputY = buttonY - inputHeight - (int)(0.1 * height);

        input = new TextFieldWidget(textRenderer, inputX, inputY, inputWidth, inputHeight, new LiteralText(""));
        input.setMaxLength(20);
        input.setFocusUnlocked(true);

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

        if (subMessage != null) {
            int subTextX = x + (width / 2) - textRenderer.getWidth(subMessage) / 2;
            int subTextY = textY + textRenderer.fontHeight + (int)(height * 0.05);
            textRenderer.drawWithShadow(matrixStack, subMessage, subTextX, subTextY, SplinterColors.SUB_TEXT);
        }

        textRenderer.drawWithShadow(matrixStack, message, textX, textY, SplinterColors.TEXT);
        if (confirmButton != null) {
            confirmButton.render(matrixStack, mouseX, mouseY, 0);
        }

        if (input != null) {
            input.render(matrixStack, mouseX, mouseY, 0);
        }
    }

    public String getTextInput() {
        if (input == null) return null;
        return input.getText().trim();
    }

    public boolean handleClick(double mouseX, double mouseY, int button) {
        if (input != null) input.mouseClicked(mouseX, mouseY, button);
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
        if (input != null) return input.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        // block unallowed chars, perhaps switch with a message later
        if (!String.valueOf(chr).matches(allowedChars)) return false;
        if (input != null) return input.charTyped(chr, keyCode);
        return false;
    }
}
