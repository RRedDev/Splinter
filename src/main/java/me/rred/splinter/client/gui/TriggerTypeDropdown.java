package me.rred.splinter.client.gui;

import me.rred.splinter.client.events.triggers.Trigger;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.Consumer;

public class TriggerTypeDropdown {
    private int x, y;
    private static final int WIDTH = 75;
    private static final int ITEM_HEIGHT = 12;
    private boolean expanded = false;
    private int hoveredOption = -1;
    private Trigger.TriggerType selected;
    private final Trigger.TriggerType[] types = Trigger.TriggerType.values();
    private Consumer<Trigger.TriggerType> onSelect;

    public TriggerTypeDropdown(int x, int y, Trigger.TriggerType initial, Consumer<Trigger.TriggerType> onSelect) {
        this.x = x;
        this.y = y;
        this.selected = initial;
        this.onSelect = onSelect;
    }

    public void render(MatrixStack matrixStack, TextRenderer textRenderer, int mouseX, int mouseY) {
        // display button
        boolean headerHovered = mouseX >= x && mouseX <= x + WIDTH
                && mouseY >= y && mouseY < y + ITEM_HEIGHT;
        int headerColor = headerHovered ? 0xFF333333 : 0xFF222222;
        DrawableHelper.fill(matrixStack, x, y, x + WIDTH, y + ITEM_HEIGHT, headerColor);
        String label = (selected != null ? selected.name() : "SELECT" + " ▼");
        textRenderer.drawWithShadow(matrixStack, label, x + 3, y + 2, 0xFFFFFF);

        if (!expanded) return;

        // options
        hoveredOption = -1;
        for (int i = 0; i < types.length; i++) {
            int optionY = y + ITEM_HEIGHT + (i * ITEM_HEIGHT);
            boolean isHovered = mouseX >= x && mouseX <= x + WIDTH
                    && mouseY >= optionY && mouseY < optionY + ITEM_HEIGHT;

            if (isHovered) hoveredOption = i;

            int itemColor = isHovered ? 0xFF333333 : 0xFF222222;
            DrawableHelper.fill(matrixStack, x, optionY, x + WIDTH, optionY + ITEM_HEIGHT,itemColor);

            int textColor = types[i] == selected ? 0xFFAA00 : 0xFFFFFF;
            textRenderer.drawWithShadow(matrixStack, types[i].name(), x + 3, optionY + 2, textColor);
        }
    }

    public boolean handleClick(double mouseX, double mouseY) {
        // toggle expand on header click
        if (mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY < y + ITEM_HEIGHT) {
            expanded = !expanded;
            return true;
        }

        if (!expanded) return false;

        // option click
        if (hoveredOption >= 0) {
            selected = types[hoveredOption];
            onSelect.accept(selected);
            expanded = false;
            return true;
        }

        // outside click - close
        expanded = false;
        return false;
    }

    public void close() {
        expanded = false;
    }

    public void setSelected(Trigger.TriggerType type) {
        this.selected = type;
    }
}
