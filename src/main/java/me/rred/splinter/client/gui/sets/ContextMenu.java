package me.rred.splinter.client.gui.sets;

import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.sets.SplinterSet;
import me.rred.splinter.client.utils.TruncateText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class ContextMenu {
    private int x, y;
    private static final int WIDTH = 60;
    private static final int ITEM_HEIGHT = 12;
    private Runnable onSetA, onSetB, onDelete;
    private boolean visible = false;
    private SplinterSet set;
    private int hoveredOption = -1;

    private List<String> options = List.of("Set as A", "Set as B", "Delete");

    public void open(int x, int y, SplinterSet set,
                     Runnable onSetA, Runnable onSetB, Runnable onDelete) {
        if (set == null) {
            return;
        }
        this.x = x;
        this.y = y;
        this.set = set;
        this.onSetA = onSetA;
        this.onSetB = onSetB;
        this.onDelete = onDelete;
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
            int optionY = y + ITEM_HEIGHT + (i * ITEM_HEIGHT) + 1;

            // check if an option should be active
            boolean isActive = true;
            switch (i) {
                case 0 -> { // set as A
                    SplinterSet setA = SplinterClient.setManager.getDisplayedSetA();
                    isActive = setA != set;
                }
                case 1 -> { // set as B
                    SplinterSet setB = SplinterClient.setManager.getDisplayedSetB();
                    isActive = setB != set;
                }
                case 2 -> { // delete
                    isActive = !set.isGeneral();
                }
            }


            boolean isHovered = (
                isActive &&
                mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= optionY && mouseY < optionY + ITEM_HEIGHT
            );

            if (isHovered) {
                hoveredOption = i;
                DrawableHelper.fill(matrixStack, x, optionY, x + WIDTH, optionY + ITEM_HEIGHT, 0x80555555);
            }

            int textColor = 0xFFFFFF;
            if (!isActive) {
                textColor = 0x80888888;
            } else if (i == 2) {
                textColor = 0xFF5555;
            }
            textRenderer.drawWithShadow(matrixStack, options.get(i), x + 3, optionY + 2, textColor);
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
        switch (hoveredOption) {
            case 0 -> {
                onSetA.run();
                close();
                return true;
            }
            case 1 -> {
                onSetB.run();
                close();
                return true;
            }
            case 2 -> {
                onDelete.run();
                close();
                return true;
            }

        }
        return false;
    }
}
