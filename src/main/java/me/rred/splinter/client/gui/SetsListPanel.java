package me.rred.splinter.client.gui;

import me.rred.splinter.client.sets.SplinterSet;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class SetsListPanel extends ListPanel {
    private List<SplinterSet> sets;

    public SetsListPanel(int x, int y, int width, int height, List<SplinterSet> sets) {
        super(x, y, width, height);
        this.sets = sets;
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    @Override
    public void render(MatrixStack matrixStack, TextRenderer textRenderer) {
        for (int i = 0; i < getItemCount(); i++) {
            int itemY = y + (i * LINE_HEIGHT) - scrollOffset;
            if (itemY + LINE_HEIGHT < y || itemY > y + height) continue; // skip off-screen lines

            String setName = sets.get(i).getName();

            textRenderer.drawWithShadow(matrixStack, setName, x, itemY, 0xFFFFFF);
        }
    }
}
