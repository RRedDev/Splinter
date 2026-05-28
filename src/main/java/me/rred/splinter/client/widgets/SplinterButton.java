package me.rred.splinter.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import me.rred.splinter.client.utils.SplinterColors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class SplinterButton extends AbstractPressableButtonWidget {
    protected final Runnable action;

    public SplinterButton(int x, int y, int width, int height, Text message, Runnable action) {
        super(x, y, width, height, message);
        this.action = action;
    }

    @Override
    public void onPress() {
        action.run();
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;

        int borderColor = isHovered() ? SplinterColors.BORDER_HOVER : SplinterColors.BORDER;
        int fillColor = SplinterColors.FILL;
        int textColor = SplinterColors.TEXT;

        // border
        fill(matrices, x, y, x + width, y + height, borderColor);
        // inner fill (1px inset)
        fill(matrices, x + 1, y + 1, x + width - 1, y + height - 1, fillColor);

        RenderSystem.enableDepthTest();

        this.drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
    }
}
