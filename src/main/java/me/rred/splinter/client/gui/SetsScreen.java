package me.rred.splinter.client.gui;

import me.rred.splinter.Splinter;
import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.keyboard.KeyInputHandler;
import me.rred.splinter.client.sets.SplinterSet;
import me.rred.splinter.client.utils.SetNameValidation;
import me.rred.splinter.client.utils.TimerFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
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

    // screen fields
    private int screenTop, screenBottom, screenLeft, screenRight;
    private int listTop, listBottom;
    private int tabHeight = 20;
    private int statsHeight = 40;
    private int offset = 25;
    private int padding = 5;
    private int scrollOffset = 0;
    private static final int LINE_HEIGHT = 12;

    // overlay fields
    private enum Overlay { NONE, CREATE, REMOVE }
    private Overlay activeOverlay = Overlay.NONE;
    private static final Identifier WARNING_ICON = new Identifier("splinter", "textures/areyousuresmallest.png");
    private int overlayWidth = 150;
    private int overlayHeight = 65;
    private int overlayX, overlayY;
    private boolean showWarningIcon = false;
    private TextFieldWidget createNameField;
    private ButtonWidget confirmButton;

    // panel fields
    private SetsListPanel setsListPanel;
    // add time panels aswell

    public SetsScreen() {
        super(new LiteralText("Splinter Sets"));
    }

    @Override
    protected void init() {
        viewedSet = SplinterClient.setManager.getActiveSet();
        List<SplinterSet> sets = SplinterClient.setManager.getAllSets();
        SplinterSet setA = SplinterClient.setManager.getDisplayedSetA();
        SplinterSet setB = SplinterClient.setManager.getDisplayedSetB();

        // inside screen dimensions
        screenTop = offset;
        screenBottom = height - offset;
        screenLeft = offset;
        screenRight = width - offset;

        // middle section cutoff points
        listTop = screenTop + tabHeight;
        listBottom = screenBottom - statsHeight;

        // panels for middle section
        int setsListY = listTop + padding;
        int setsListWidth = 60;
        int setsListHeight = listBottom - listTop - padding;

        setsListPanel = new SetsListPanel(screenLeft, setsListY, setsListWidth, setsListHeight, sets);

        // sets creation / removal buttons
        int setsButtonWidth = 60;
        int setsButtonHeight = 20;
        int createX = screenLeft;
        int removeX = createX + setsButtonWidth;
        int setsButtonY = screenTop;

        addButton(new ButtonWidget(createX, setsButtonY, setsButtonWidth, setsButtonHeight,
                new LiteralText("CREATE"),
                button -> openCreateOverlay()
        ));

        // overlay position
        overlayX = (width - overlayWidth) / 2;
        overlayY = (height - overlayHeight) / 2;

        // TimesListPanel timesAPanel = new TimesListPanel()

        // initialize tabs (set buttons)
        /*
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
         */
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
        // close overlay if Esc is pressed
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && activeOverlay != Overlay.NONE) {
            closeOverlay();
            return true;
        }

        // leave screen with Esc or specified hotkey
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || KeyInputHandler.GUI_SETS_BIND.getKeyBinding().matchesKey(keyCode, scanCode)) {
            // close overlay instead of screen
            if (keyCode == GLFW.GLFW_KEY_ESCAPE && activeOverlay != Overlay.NONE) {
                activeOverlay = Overlay.NONE;
                return true;
            }
            SetsScreen.toggle();
            return true;
        }
        // pass input into the CREATE Overlay text field
        if (activeOverlay == Overlay.CREATE && createNameField != null) {
            return createNameField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

        // draw GUI title text
        drawCenteredText(matrixStack, textRenderer, title, width / 2, 10, 0xFFFFFF);

        // draw backgrounds
        // tabs bar (top)
        fill(matrixStack, screenLeft, screenTop, screenRight, screenTop + tabHeight, 0x80333333);

        // time list (middle)
        fill(matrixStack, screenLeft, listTop, screenRight, listBottom, 0x80222222);

        // draw vertical borders between columns
        int borderColor = 0x80555555;
        int borderWidth = 1;
        for (int i = 1; i < 4; i++) {
            int borderX = screenLeft + (i * 60);
            fill(matrixStack, borderX, listTop, borderX + borderWidth, listBottom, borderColor);
        }

        // draw middle ListPanels

        enableScissor();
        setsListPanel.render(matrixStack, textRenderer);
        // just focuson rendering the setsListPanel for now
        disableScissor();

        renderStats(matrixStack);

        if (activeOverlay != Overlay.NONE) {
            renderOverlay(matrixStack);
        }

        // draw buttons
        super.render(matrixStack, mouseX, mouseY, delta);
    }

    /*
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
    */

    private void renderStats(MatrixStack matrixStack) {
        int col1X = screenLeft + padding;
        int row1Y = screenBottom - statsHeight + padding;
        int row2Y = screenBottom - 9 - padding;

        String avgTime = TimerFormatter.format(viewedSet.getAverage());
        String bestTime = TimerFormatter.format(viewedSet.getBest());

        textRenderer.drawWithShadow(matrixStack, "AVG:", col1X, row1Y, 0xFFFFFF);
        textRenderer.drawWithShadow(matrixStack, avgTime, col1X + 40, row1Y, 0xFFFFFF);
        textRenderer.drawWithShadow(matrixStack, "BEST:", col1X, row2Y, 0xFFFFFF);
        textRenderer.drawWithShadow(matrixStack, bestTime, col1X + 40, row2Y, 0xFFFFFF);
    }

    private void renderOverlay(MatrixStack matrixStack) {

        fill(matrixStack, overlayX, overlayY, overlayX + overlayWidth, overlayY + overlayHeight, 0xFF222222);

        if (activeOverlay == Overlay.CREATE) {
            // check if the confirm button should be active
            if (createNameField != null && confirmButton != null) {
                String name = createNameField.getText().trim();
                confirmButton.active = SetNameValidation.isValid(name);
            }

            drawCenteredText(matrixStack, textRenderer, new LiteralText("Name your set"), width / 2, overlayY + 5, 0xFFFFFF);
            if (createNameField != null) {
                createNameField.render(matrixStack, 0, 0, 0);
            }
        } else if (activeOverlay == Overlay.REMOVE) {
            if (showWarningIcon) {
                int imgSquish = 80;
                int imgSize = 80;
                int imgX = (width - imgSize) / 2;
                int imgY = overlayY - imgSize - 4;

                assert client != null;
                client.getTextureManager().bindTexture(WARNING_ICON);
                DrawableHelper.drawTexture(matrixStack, imgX, imgY, 0, 0, imgSquish, imgSquish, imgSize, imgSize);
            }
            drawCenteredText(matrixStack, textRenderer, new LiteralText("Are you sure?"), width / 2, overlayY + 10, 0xFFFFFF);
        }
    }


    private void enableScissor() {
        double scale = client.getWindow().getScaleFactor();
        int scissorX = 0;
        int scissorY = (int)((height - listBottom) * scale);
        int scissorW = (int)((width * scale));
        int scissorH = (int)((listBottom - listTop) * scale);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
    }

    private void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void openCreateOverlay() {
        activeOverlay = Overlay.CREATE;

        int inputWidth = 130;
        int inputHeight = 12;
        int inputX = (width - inputWidth) / 2;
        int inputY = overlayY + 20;

        createNameField = new TextFieldWidget(textRenderer, inputX, inputY, inputWidth, inputHeight, new LiteralText(""));
        createNameField.setMaxLength(20);
        createNameField.setFocusUnlocked(true);
        children.add(createNameField);

        int confirmWidth = 60;
        int confirmHeight = 20;
        int confirmX = (width - confirmWidth) / 2; // centered
        int confirmY = inputY + inputHeight + 8;
        confirmButton = new ButtonWidget(confirmX, confirmY, confirmWidth, confirmHeight,
                new LiteralText("CONFIRM"),
                button-> {
                    String name = createNameField.getText().trim();
                    if(!name.isEmpty()) {
                        SplinterClient.setManager.createSet(name);
                        activeOverlay = Overlay.NONE;
                        children.remove(createNameField);
                        closeOverlay();
                    }

                }
                );
        addButton(confirmButton);
    }

    private void openRemoveOverlay() {
        showWarningIcon = true; //Math.random() < 0.01; // 1% nolan
        activeOverlay = Overlay.REMOVE;
    }

    private void closeOverlay() {
        activeOverlay = Overlay.NONE;
        if (createNameField != null) {
            children.remove(createNameField);
            createNameField = null;
        }
        // clear confirm button and re-add real buttons
        buttons.clear();
        init();
    }

    public static void toggle() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof SetsScreen) {
            client.openScreen(null);
        } else {
            client.openScreen(new SetsScreen());
        }
    }
}
