package me.rred.splinter.client.gui.sets;

import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.sets.SplinterSet;
import me.rred.splinter.client.utils.TruncateText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class ContextMenu {
    public static class Option {
        public final String label;
        public final Runnable action;
        public final int color;
        public final boolean active;

        public Option(String label, Runnable action, int color, boolean active) {
            this.label = label;
            this.action = action;
            this.color = color;
            this.active = active;
        }
    }

    private List<Option> options = new ArrayList<>();
    private int x, y;
    private static final int WIDTH = 60;
    private static final int ITEM_HEIGHT = 12;
    private boolean visible = false;
    private SplinterSet set;
    private int hoveredOption = -1;

    public void open(int x, int y, SplinterSet set, List<Option> options) {
        if (set == null) {
            return;
        }
        this.x = x;
        this.y = y;
        this.set = set;
        this.options = options;
        this.visible = true;
    }

    public void render(MatrixStack matrixStack, TextRenderer textRenderer, int mouseX, int mouseY) {
        if (!visible) return;
        int totalHeight = ITEM_HEIGHT * (options.size() + 1);
        hoveredOption = -1;

        // background
        DrawableHelper.fill(matrixStack, x, y, x + WIDTH, y + totalHeight, 0xFF222222);

        // name header
        textRenderer.drawWithShadow(matrixStack, TruncateText.truncate(set.getName(), WIDTH - 3, textRenderer), x + 3, y + 2, 0xAAAAAA);

        // divider
        DrawableHelper.fill(matrixStack, x, y + ITEM_HEIGHT, x + WIDTH, y + ITEM_HEIGHT + 1, 0x80555555);

        // options
        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);
            int optionY = y + ITEM_HEIGHT + (i * ITEM_HEIGHT) + 1;

            boolean isHovered = (
                option.active &&
                mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= optionY && mouseY < optionY + ITEM_HEIGHT
            );

            if (isHovered) {
                hoveredOption = i;
                DrawableHelper.fill(matrixStack, x, optionY, x + WIDTH, optionY + ITEM_HEIGHT, 0x80555555);
            }

            int textColor = !option.active ? 0x80888888 : option.color;
            textRenderer.drawWithShadow(matrixStack, option.label, x + 3, optionY + 2, textColor);
        }
    }

    public void close() {
        visible = false;
        set = null;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean handleClick(double mouseX, double mouseY) {
        if (!visible) return false;
        if (hoveredOption >= 0 && hoveredOption < options.size()) {
            options.get(hoveredOption).action.run();
            close();
            return true;
        }
        return false;
    }
}
