package me.rred.splinter.client.gui.sets;

import me.rred.splinter.client.sets.SplinterSet;
import me.rred.splinter.client.utils.TimerFormatter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class TimesListPanel extends ListPanel {
    private SplinterSet set;
    private int hoveredDelIndex = -1;

    public TimesListPanel(int x, int y, int width, int height, SplinterSet set) {
        super(x, y, width, height);
        this.set = set;
    }

    @Override
    public int getItemCount() {
        return set != null ? set.getTimes().size() : 0;
    }

    @Override
    public void render(MatrixStack matrixStack, TextRenderer textRenderer, int mouseX, int mouseY, boolean showHover) {
        if (set != null) {
            List<Long> times = set.getTimes();
            hoveredDelIndex = -1;

            for (int i = 0; i < times.size(); i++) {
                int itemY = y + (i * LINE_HEIGHT) - scrollOffset + i + 1;
                if (itemY + LINE_HEIGHT < y || itemY > y + height) continue; // skip off-screen lines

                int delSize = 11;
                int delX = x + width - delSize - 2; // 2 px from right edge
                int delY = itemY + (ITEM_HEIGHT - delSize) / 2;
                int delTextX = 1 + delX + (delSize - textRenderer.getWidth("x")) / 2; // 2 px from right edge
                int delTextY = delY + (delSize - textRenderer.fontHeight) / 2;

                boolean isDelHovered = (
                    mouseX >= delX && mouseX <= delX + delSize &&
                    mouseY >= delY && mouseY <= delY + delSize
                );
                int deleteColor = 0xFFFF2222;
                if (isDelHovered) {
                    hoveredDelIndex = i;
                    DrawableHelper.fill(matrixStack, delX, delY, delX + delSize, delY + delSize, 0xFF666666);
                }

                textRenderer.draw(matrixStack, "x", delTextX, delTextY, deleteColor);

                // draw bottom border for each record
                DrawableHelper.fill(matrixStack, x, itemY + LINE_HEIGHT, x + width, itemY + LINE_HEIGHT + 1, 0x80666666);

                int textY = itemY + (LINE_HEIGHT - textRenderer.fontHeight + 1) / 2 + 1;
                String number = (i + 1) + ".";
                String timeText = TimerFormatter.format(times.get(i));

                textRenderer.drawWithShadow(matrixStack, number, x + 3, textY, 0xFFFFFF);
                textRenderer.drawWithShadow(matrixStack, timeText, x + 20, textY, 0xFFFFFF);
            }
        }
    }

    public boolean handleClick(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        if (set == null) return false;

        if (hoveredDelIndex >= 0 && button == 0) {
            set.removeTime(hoveredDelIndex);
            return true;
        }
        return false;
    }
}
