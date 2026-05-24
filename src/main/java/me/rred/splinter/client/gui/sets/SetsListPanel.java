package me.rred.splinter.client.gui.sets;

import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.sets.SplinterSet;
import me.rred.splinter.client.utils.TruncateText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.function.BiConsumer;

public class SetsListPanel extends ListPanel {
    private List<SplinterSet> sets;
    private BiConsumer<SplinterSet, Integer> onClick;
    private boolean isHovered = false;
    private int hoveredIndex = -1;
    private int hoveredPauseIndex = -1;

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
    public void render(MatrixStack matrixStack, TextRenderer textRenderer, int mouseX, int mouseY, boolean showHover) {
        SplinterSet activeSet = SplinterClient.setManager.getActiveSet();
        hoveredIndex = -1;
        hoveredPauseIndex = -1;

        for (int i = 0; i < getItemCount(); i++) {
            SplinterSet set = sets.get(i);
            String setName = set.getName();

            int itemY = y + (i * LINE_HEIGHT) - scrollOffset + i + 1;
            if (itemY + LINE_HEIGHT < y || itemY > y + height) continue; // skip off-screen lines

            boolean isActive = set == activeSet;
            isHovered = (
                showHover &&
                mouseX >= x && mouseX <= x + width &&
                mouseY >= itemY && mouseY <= itemY + LINE_HEIGHT
            );
            if (isHovered) {
                hoveredIndex = i;
            }

            // background color based on state
            int bgColor = 0x00000000;

            if (isActive) {
                bgColor = 0x80447744; // green tint
            } else if (isHovered)  {
                bgColor = 0xFF555555;  // lighter on hover
            }

            // draw background
            DrawableHelper.fill(matrixStack, x, itemY, x + width, itemY + LINE_HEIGHT, bgColor);

            // draw bottom border for each record
            DrawableHelper.fill(matrixStack, x, itemY + LINE_HEIGHT, x + width, itemY + LINE_HEIGHT + 1, 0x80666666);

            // draw text
            int pauseSize = 11;
            int pauseX = x + width - pauseSize - 2; // 2 px from right edge

            int textY = itemY + (ITEM_HEIGHT - textRenderer.fontHeight ) / 2;
            int textColor = isActive ? 0xAAFFAA : 0xFFFFFF;
            int textWidth = (pauseX) - (x + 3);
            textRenderer.drawWithShadow(matrixStack,
                    TruncateText.truncate(setName, textWidth, textRenderer),
                    x + 3, textY, textColor);

            // pause button to toggle between IDLE and ACTIVE state (stop recording)
            if (isActive) {
                String buttonLabel = SplinterClient.ssm.isActive() ? "■" : "▶";
                int pauseY = itemY + (ITEM_HEIGHT - pauseSize) / 2;
                int pauseTextX = 1 + pauseX + (pauseSize - textRenderer.getWidth(buttonLabel)) / 2; // 2 px from right edge
                int pauseTextY = pauseY + (pauseSize - textRenderer.fontHeight) / 2 + 1;

                boolean isPauseHovered = (
                        mouseX >= pauseX && mouseX <= pauseX + pauseSize &&
                                mouseY >= pauseY && mouseY <= pauseY + pauseSize
                );

                int pauseColor = SplinterClient.ssm.isActive() ? 0xFFFF2222 : 0xFF22FF22;
                if (isPauseHovered) {
                    hoveredPauseIndex = i;
                    DrawableHelper.fill(matrixStack, pauseX, pauseY, pauseX + pauseSize, pauseY + pauseSize, 0x80FFFFFF);
                }
                textRenderer.draw(matrixStack, buttonLabel , pauseTextX, pauseTextY, pauseColor);

            }
        }
    }

    public boolean handleClick(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        if (onClick == null) return false;
        if (hoveredIndex < 0 || hoveredIndex >= sets.size()) return false;
        if (hoveredPauseIndex >= 0 && button == 0) {
            if (SplinterClient.ssm.isActive()) {
                SplinterClient.ssm.setIdle();
            } else {
                SplinterClient.ssm.setActive();
            }
            return true;
        }

        onClick.accept(sets.get(hoveredIndex), button);
        return true;
    }
}
