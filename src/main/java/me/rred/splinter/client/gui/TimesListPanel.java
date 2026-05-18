package me.rred.splinter.client.gui;

import me.rred.splinter.client.sets.SplinterSet;
import me.rred.splinter.client.utils.TimerFormatter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class TimesListPanel extends ListPanel {
    private SplinterSet set;

    public TimesListPanel(int x, int y, int width, int height, SplinterSet set) {
        super(x, y, width, height);
        this.set = set;
    }

    @Override
    public int getItemCount() {
        return set != null ? set.getTimes().size() : 0;
    }

    @Override
    public void render(MatrixStack matrixStack, TextRenderer textRenderer, int mouseX, int mouseY) {
        if (set != null) {
            List<Long> times = set.getTimes();
            // draw top border for first
            int borderColor = 0x80666666;
            DrawableHelper.fill(matrixStack, x, y - scrollOffset, x + width,  y - scrollOffset + 1, borderColor);

            for (int i = 0; i < times.size(); i++) {

                int itemY = y + (i * LINE_HEIGHT) - scrollOffset + i + 1;
                if (itemY + LINE_HEIGHT < y || itemY > y + height) continue; // skip off-screen lines

                // draw bottom border for each record
                DrawableHelper.fill(matrixStack, x, itemY + LINE_HEIGHT, x + width, itemY + LINE_HEIGHT + 1, borderColor);

                int textY = itemY + (LINE_HEIGHT - textRenderer.fontHeight + 1) / 2;
                String number = (i + 1) + ".";
                String timeText = TimerFormatter.format(times.get(i));

                textRenderer.drawWithShadow(matrixStack, number, x + 3, textY, 0xFFFFFF);
                textRenderer.drawWithShadow(matrixStack, timeText, x + 20, textY, 0xFFFFFF);
            }
        }
    }
}
