package me.rred.splinter.client.edit.gui;

import me.rred.splinter.client.events.triggers.Trigger;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class TriggerSlotButton {
    private final int x, y, width, height;
    private final Trigger.TriggerSlot slot;
    private final Color baseColor;
    private final Color hoverColor;
    private final Color selectedColor;
    private final Color borderColor;

    public TriggerSlotButton(int x, int y, int width, int height, Trigger.TriggerSlot slot) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.slot = slot;
        if (slot == Trigger.TriggerSlot.START) {
            borderColor = new Color(0, 60, 0);
            baseColor = new Color(0, 80, 0);
            hoverColor = new Color(0, 120, 0);
            selectedColor = new Color(0, 180, 0);
        } else {
            borderColor = new Color(60, 0, 0);
            baseColor = new Color(80, 0, 0);
            hoverColor = new Color(120, 0, 0);
            selectedColor = new Color(180, 0, 0);
        }
    }

    public void render (MatrixStack matrixStack, TextRenderer textRenderer, int mouseX, int mouseY, boolean selected) {
        boolean hovered = isMouseOver(mouseX, mouseY);
        Color color = selectedColor;
        if (!selected) {
            if (hovered) {
               color = hoverColor;
            } else {
                color = baseColor;
            }
        }

        // border
        int borderWidth = 2;
        DrawableHelper.fill(matrixStack, x - borderWidth, y - borderWidth, x + width + borderWidth, y + height + borderWidth, borderColor.getRGB());

        // fill
        DrawableHelper.fill(matrixStack, x, y, x + width, y + height, color.getRGB());

        // label
        String label = "START";
        int labelColor = 0xAAFFAA;
        if (slot == Trigger.TriggerSlot.END) {
            label = "END";
            labelColor = 0xFFAAAA;
        }
        int labelX = x + (width - textRenderer.getWidth(label)) / 2;
        int labelY = y + (height - textRenderer.fontHeight) / 2;

        textRenderer.draw(matrixStack, label, labelX, labelY, labelColor);

    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }

    public boolean handleClick(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }
}
