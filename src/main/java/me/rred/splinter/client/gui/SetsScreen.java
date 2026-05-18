package me.rred.splinter.client.gui;

import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.keyboard.KeyInputHandler;
import me.rred.splinter.client.sets.SplinterSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class SetsScreen extends Screen {
    private SplinterSet viewedSet;

    private int screenTop, screenBottom, screenLeft, screenRight;
    private int listTop, listBottom;
    private int tabHeight = 20;
    private int statsHeight = 40;
    private int offset = 25;
    private int padding = 5;
    private int scrollOffset = 0;
    private static final int LINE_HEIGHT = 12;
    private boolean showWarningIcon = false;

    private enum Overlay { NONE, CREATE, REMOVE }
    private Overlay activeOverlay = Overlay.NONE;
    private static final Identifier WARNING_ICON = new Identifier("splinter", "textures/areyousuresmallest.png");

    public SetsScreen() {
        super(new LiteralText("Splinter Sets"));
    }

    @Override
    protected void init() {
        // buttons and widgets
        viewedSet = SplinterClient.setManager.getActiveSet();
        List<SplinterSet> sets = SplinterClient.setManager.getAllSets();

        int buttonWidth = 60;
        int buttonHeight = 20;

        screenTop = offset;
        screenBottom = height - offset;
        screenLeft = offset;
        screenRight = width - offset;

        listTop = screenTop + tabHeight;
        listBottom = screenBottom - statsHeight;

        // initialize tabs (set buttons)
        for (int i = 0; i < sets.size(); i++) {
            SplinterSet set = sets.get(i);
            int x = offset + (i * buttonWidth);
            int y = offset;

            addButton(new ButtonWidget(x, y, buttonWidth, buttonHeight,
                    new LiteralText(set.getName()),
                    button -> {
                        viewedSet = set;
                        scrollOffset = 0;
                    }
            ));
        }

        // initialize set creation/removal buttons

        int setsButtonWidth = 55;
        int setsButtonHeight = 20;
        int buttonX = screenLeft + padding + 100;
        int row1Y = screenBottom - statsHeight;
        int row2Y = screenBottom - setsButtonHeight;

        addButton(new ButtonWidget(buttonX, row1Y, setsButtonWidth, setsButtonHeight,
                new LiteralText("CREATE"),
                button -> openCreatePanel()
        ));

        addButton(new ButtonWidget(buttonX, row2Y, setsButtonWidth, setsButtonHeight,
                new LiteralText("REMOVE"),
                button -> openRemovePanel()
        ));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        List<Long> times = viewedSet.getTimes();
        int visibleHeight = listBottom - listTop - 10; // 5px padding top and bottom
        int maxScroll = Math.max(0, times.size() * LINE_HEIGHT - visibleHeight);

        scrollOffset -= (int) (amount * LINE_HEIGHT);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || KeyInputHandler.GUI_SETS_BIND.getKeyBinding().matchesKey(keyCode, scanCode)) {
            // close overlay instead of screen
            if (keyCode == GLFW.GLFW_KEY_ESCAPE && activeOverlay != Overlay.NONE) {
                activeOverlay = Overlay.NONE;
                return true;
            }
            SetsScreen.toggle();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);

        // draw GUI title text
        drawCenteredText(matrixStack, textRenderer, title, width / 2, 10, 0xFFFFFF);

        // tabs bar (top)
        fill(matrixStack, screenLeft, screenTop, screenRight, screenTop + tabHeight, 0x80333333);

        // time list (middle)
        fill(matrixStack, screenLeft, listTop, screenRight, listBottom, 0x80222222);

        // draw vertical borders between columns
        int borderColor = 0x80555555;
        int borderWidth = 1;
        for (int i = 1; i < 5; i++) {
            int borderX = screenLeft + (i * 60);
            fill(matrixStack, borderX, listTop, borderX + borderWidth, listBottom, borderColor);
        }

        renderTimeList(matrixStack, 0); // for now just render the first timeList
        renderStats(matrixStack);

        if (activeOverlay != Overlay.NONE) {
            renderOverlay(matrixStack);
        }

    }

    private void renderTimeList(MatrixStack matrixStack, int idx) {
        List<Long> times = viewedSet.getTimes();
        int timesLeft = screenLeft + 5 + (idx * 60);
        int timesMiddle = timesLeft + 15;
        int startY = screenTop + tabHeight + 5 - scrollOffset;

        if (times.isEmpty()) {
            drawTextWithShadow(matrixStack, textRenderer, new LiteralText("No times"), timesLeft, startY, 0xFFFFFF);
        } else {
            // draw list within background
            double scale = client.getWindow().getScaleFactor();
            int scissorX = 0;
            int scissorY = (int)((height - listBottom) * scale);
            int scissorW = (int)((width * scale));
            int scissorH = (int)((listBottom - listTop) * scale);

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

            for (int i = 0; i < times.size(); i++) {
                int y = startY + (i * LINE_HEIGHT);
                if (y + LINE_HEIGHT < listTop || y > listBottom) continue; // skip off-screen lines

                String number = (i + 1) + ".";
                String timeText = formatTime(times.get(i));
                textRenderer.drawWithShadow(matrixStack, number, timesLeft, startY + (i * 12), 0xFFFFFF);
                textRenderer.drawWithShadow(matrixStack, timeText, timesMiddle, startY + (i * 12), 0xFFFFFF);
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    private void renderStats(MatrixStack matrixStack) {
        int col1X = screenLeft + padding;
        int row1Y = screenBottom - statsHeight + padding;
        int row2Y = screenBottom - 9 - padding;

        String avgTime = formatTime(viewedSet.getAverage());
        String bestTime = formatTime(viewedSet.getBest());

        textRenderer.drawWithShadow(matrixStack, "AVG:", col1X, row1Y, 0xFFFFFF);
        textRenderer.drawWithShadow(matrixStack, avgTime, col1X + 40, row1Y, 0xFFFFFF);
        textRenderer.drawWithShadow(matrixStack, "BEST:", col1X, row2Y, 0xFFFFFF);
        textRenderer.drawWithShadow(matrixStack, bestTime, col1X + 40, row2Y, 0xFFFFFF);
    }

    private void renderOverlay(MatrixStack matrixStack) {
        int panelWidth = 150;
        int panelHeight = 50;
        int panelOffset = 0;
        int panelX = (width - panelWidth) / 2;
        int panelY = (height - panelHeight + panelOffset) / 2;

        fill(matrixStack, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF222222);

        if (activeOverlay == Overlay.CREATE) {
            drawCenteredText(matrixStack, textRenderer, new LiteralText("Name your set"), width / 2, panelY + 10, 0xFFFFFF);
        } else if (activeOverlay == Overlay.REMOVE) {
            if (showWarningIcon) {
                int imgSquish = 80;
                int imgSize = 80;
                int imgX = (width - imgSize) / 2;
                int imgY = panelY - imgSize - 4;

                assert client != null;
                client.getTextureManager().bindTexture(WARNING_ICON);
                DrawableHelper.drawTexture(matrixStack, imgX, imgY, 0, 0, imgSquish, imgSquish, imgSize, imgSize);
            }
            drawCenteredText(matrixStack, textRenderer, new LiteralText("Are you sure?"), width / 2, panelY + 10, 0xFFFFFF);
        }
    }

    private void openCreatePanel() {
        activeOverlay = Overlay.CREATE;
    }

    private void openRemovePanel() {
        showWarningIcon = true; //Math.random() < 0.01; // 1% nolan
        activeOverlay = Overlay.REMOVE;
    }

    public static void toggle() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof SetsScreen) {
            client.openScreen(null);
        } else {
            client.openScreen(new SetsScreen());
        }
    }

    private static String formatTime(long ms) {
        long minutes = ms / 60000;
        long seconds = (ms % 60000) / 1000;
        long millis = ms % 1000;
        return String.format("%d:%02d.%03d", minutes, seconds, millis);
    }
}
