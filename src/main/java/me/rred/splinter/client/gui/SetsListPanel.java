package me.rred.splinter.client.gui;

import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.sets.SplinterSet;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.http.impl.client.ContentEncodingHttpClient;

import java.util.List;
import java.util.function.BiConsumer;

public class SetsListPanel extends ListPanel {
    private List<SplinterSet> sets;
    private BiConsumer<SplinterSet, Integer> onClick;

    public SetsListPanel(int x, int y, int width, int height, List<SplinterSet> sets, BiConsumer<SplinterSet, Integer> onClick) {
        super(x, y, width, height);
        this.sets = sets;
        this.onClick = onClick;
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    @Override
    public void render(MatrixStack matrixStack, TextRenderer textRenderer, int mouseX, int mouseY) {
        SplinterSet activeSet = SplinterClient.setManager.getActiveSet();
        // draw top border for first
        int borderColor = 0x80666666;
        DrawableHelper.fill(matrixStack, x, y - scrollOffset, x + width,  y - scrollOffset + 1, borderColor);


        for (int i = 0; i < getItemCount(); i++) {
            SplinterSet set = sets.get(i);
            String setName = set.getName();

            int itemY = y + (i * LINE_HEIGHT) - scrollOffset + i + 1;
            if (itemY + LINE_HEIGHT < y || itemY > y + height) continue; // skip off-screen lines

            boolean isActive = set == activeSet;
            boolean isHovered = (
                    mouseX >= x && mouseX <= x + width &&
                    mouseY >= itemY && mouseY <= itemY + LINE_HEIGHT
                    );

            // background color based on state
            int bgColor;
            if (isActive) {
                bgColor = 0x80447744; // green tint
            } else if (isHovered)  {
                bgColor = 0x80555555;  // lighter on hover
            }  else {
                bgColor = 0x00000000; // transparent
            }

            // draw background
            DrawableHelper.fill(matrixStack, x, itemY, x + width, itemY + LINE_HEIGHT, bgColor);

            // draw bottom border for each record
            DrawableHelper.fill(matrixStack, x, itemY + LINE_HEIGHT, x + width, itemY + LINE_HEIGHT + 1, borderColor);

            // draw text
            int textY = itemY + (LINE_HEIGHT - textRenderer.fontHeight + 1) / 2;
            int textColor = isActive ? 0xAAFFAA : 0xFFFFFF;
            textRenderer.drawWithShadow(matrixStack, setName, x + 3, textY, textColor);
        }
    }

    public boolean handleClick(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        if (onClick == null) return false;

        int index = getIndexAt(mouseY);
        if (index >= 0 && index < sets.size()) {
            onClick.accept(sets.get(index), button);
            return true;
        }
        return false;
    }
}
