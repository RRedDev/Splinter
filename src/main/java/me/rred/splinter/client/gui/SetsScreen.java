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
import java.sql.Time;
import java.util.List;

public class SetsScreen extends Screen {
    private SplinterSet viewedSet;

    // main screen fields
    private int screenTop, screenBottom, screenLeft, screenRight;
    private int listTop, listBottom;
    private int tabHeight = 20;
    private int statsHeight = 40;
    private int offset = 25;
    private int padding = 5;
    private int headerButtonLen;
    private SplinterSet setA;
    private SplinterSet setB;

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
    private TimesListPanel timesListPanelA;
    private TimesListPanel timesListPanelB;
    private int borderColor = 0x80555555;
    private int borderWidth = 1;
    private int setsListWidth = 80;
    private int timesListWidth = 80;
    private int list2X;
    private int list3X;
    private int list4X;

    public SetsScreen() {
        super(new LiteralText("Splinter Sets"));
    }

    @Override
    protected void init() {
        buttons.clear();
        children.clear();

        viewedSet = SplinterClient.setManager.getActiveSet();
        List<SplinterSet> sets = SplinterClient.setManager.getAllSets();
        setA = SplinterClient.setManager.getDisplayedSetA();
        setB = SplinterClient.setManager.getDisplayedSetB();

        // inside screen dimensions
        screenTop = offset;
        screenBottom = height - offset;
        screenLeft = offset;
        screenRight = width - offset;

        // middle section cutoff points
        listTop = screenTop + tabHeight;
        listBottom = screenBottom - statsHeight;
        // list starting X coordinate (after border) list1 starts at screenLeft
        list2X = screenLeft + setsListWidth + borderWidth;
        list3X = list2X + timesListWidth + borderWidth;
        list4X = list3X + timesListWidth + borderWidth;

        // panels for middle section
        int listHeight = listBottom - listTop;
        setsListPanel = new SetsListPanel(screenLeft, listTop, setsListWidth, listHeight, sets,
                (set, button) -> {
                        if (button == 0) { // left click
                            SplinterClient.setManager.setActiveSet(set);
                        } else if (button == 1) { // right click logic
                            // RC + SHIFT or both are full
                            if (hasShiftDown() || (setA != null && setB != null)) {
                                // context menu
                            } else if (setA == null) {
                                SplinterClient.setManager.setDisplayedSetA(set);
                                init();
                            } else if (setB == null) {
                                SplinterClient.setManager.setDisplayedSetB(set);
                                init();
                            }
                        }
                    }
                );

        timesListPanelA = new TimesListPanel(list2X, listTop, timesListWidth, listHeight, setA);
        timesListPanelB = new TimesListPanel(list3X, listTop, timesListWidth, listHeight, setB);


        // set creation button
        int createButtonWidth = 80;
        int createButtonHeight = 20;

        addButton(new ButtonWidget(screenLeft, screenTop, createButtonWidth, createButtonHeight,
                new LiteralText("CREATE"),
                button -> openCreateOverlay()
        ));

        // overlay position
        overlayX = (width - overlayWidth) / 2;
        overlayY = (height - overlayHeight) / 2;

        // dynamic header buttons to clear the specified displayed set
        int headerWidth = timesListWidth;
        headerButtonLen = 20;
        int startX = screenLeft + createButtonWidth;

        if (setA != null) {
            // + 1 for vertical borders
            addButton(new ButtonWidget(startX + borderWidth, screenTop, headerButtonLen, headerButtonLen,
                    new LiteralText("-"),
                    button -> {
                        SplinterClient.setManager.setDisplayedSetA(null);
                        init();
                    }
            ));
        }

        if (setB != null) {
            addButton(new ButtonWidget(startX + headerWidth + (2 * borderWidth), screenTop, headerButtonLen, headerButtonLen,
                    new LiteralText("-"),
                    button -> {
                        SplinterClient.setManager.setDisplayedSetB(null);
                        init();
                    }
            ));
        }
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
        fill(matrixStack, list2X - borderWidth, screenTop, list2X, listBottom, borderColor);
        fill(matrixStack, list3X - borderWidth, screenTop, list3X, listBottom, borderColor);
        fill(matrixStack, list4X - borderWidth, screenTop, list4X, listBottom, borderColor);

        // draw headers
        int headerTextY = screenTop + (tabHeight - textRenderer.fontHeight + 1) / 2;
        int setAX = list2X + headerButtonLen + padding;
        int setBX = setAX + timesListWidth;

        if (setA != null) {
            textRenderer.drawWithShadow(matrixStack, setA.getName(), setAX, headerTextY, 0xFFFFFF);
        }

        if (setB != null) {
            textRenderer.drawWithShadow(matrixStack, setB.getName(), setBX, headerTextY, 0xFFFFFF);
        }

        // render middle ListPanels

        enableScissor();
        setsListPanel.render(matrixStack, textRenderer, mouseX, mouseY);
        timesListPanelA.render(matrixStack, textRenderer, mouseX, mouseY);
        timesListPanelB.render(matrixStack, textRenderer, mouseX, mouseY);
        // just focus on rendering the setsListPanel for now
        disableScissor();

        // render stats and overlays

        renderStats(matrixStack);

        if (activeOverlay != Overlay.NONE) {
            renderOverlay(matrixStack);
        }

        // draw buttons
        super.render(matrixStack, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (setsListPanel.isMouseOver(mouseX, mouseY)) {
            setsListPanel.scroll(amount);
            return true;
        }

        if (timesListPanelA.isMouseOver(mouseX, mouseY)) {
            timesListPanelA.scroll(amount);
            return true;
        }

        if (timesListPanelB.isMouseOver(mouseX, mouseY)) {
            timesListPanelB.scroll(amount);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(setsListPanel.handleClick(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // leave screen with Esc or specified hotkey
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || KeyInputHandler.GUI_SETS_BIND.getKeyBinding().matchesKey(keyCode, scanCode)) {
            // close overlay instead of screen
            if (keyCode == GLFW.GLFW_KEY_ESCAPE && activeOverlay != Overlay.NONE) {
                closeOverlay();
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
                    if (createNameField == null) return;
                    String name = createNameField.getText().trim();
                    if(!name.isEmpty()) {
                        SplinterClient.setManager.createSet(name);
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
        confirmButton = null;
        buttons.clear();
        children.clear();
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
