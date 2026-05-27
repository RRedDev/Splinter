package me.rred.splinter.client.sets.gui;

import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.keyboard.KeyInputHandler;
import me.rred.splinter.client.routing.Route;
import me.rred.splinter.client.sets.SplinterSet;
import me.rred.splinter.client.utils.SetNameValidation;
import me.rred.splinter.client.utils.TimerFormatter;
import me.rred.splinter.client.utils.TruncateText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class SetsScreen extends Screen {
    private SplinterSet activeSet;

    // main screen fields
    private int screenTop, screenBottom, screenLeft, screenRight;
    private int listTop, listBottom;
    private int headerTextY;
    private int tabHeight = 20;
    private int offset = 25;
    private int padding = 5;
    private int headerButtonLen;
    private SplinterSet setA;
    private SplinterSet setB;
    private static final int textColor = 0xFFFFFF;

    // overlay fields
    private enum Overlay { NONE, CREATE, REMOVE, RENAME, CLEAR }
    private Overlay activeOverlay = Overlay.NONE;
    private static final Identifier WARNING_ICON = new Identifier("splinter", "textures/areyousuresmallest.png");
    private int overlayWidth = 150;
    private int overlayHeight = 65;
    private int overlayX, overlayY;
    private boolean showWarningIcon = false;
    private TextFieldWidget createNameField;
    private ButtonWidget confirmButton;
    private SplinterSet overlayTargetSet;

    // panel fields
    private SetsListPanel setsListPanel;
    private TimesListPanel timesListPanelA;
    private TimesListPanel timesListPanelB;
    private ContextMenu contextMenu = new ContextMenu();
    private int borderWidth = 1;
    private int setsListWidth = 80;
    private int timesListWidth = 80;
    private int list2X;
    private int list3X;
    private int list4X;
    private int lastClickX, lastClickY;

    public SetsScreen() {
        super(new LiteralText("Splinter Sets"));
    }

    @Override
    protected void init() {
        buttons.clear();
        children.clear();

        activeSet = SplinterClient.setManager.getActiveSet();
        List<SplinterSet> sets = SplinterClient.setManager.getAllSets();
        setA = SplinterClient.setManager.getDisplayedSetA();
        setB = SplinterClient.setManager.getDisplayedSetB();

        // sets UI screen dimensions
        screenTop = offset;
        screenBottom = height - (int)(offset * 1.8) ;
        screenLeft = offset;
        screenRight = width - offset;

        // middle section cutoff points
        listTop = screenTop + tabHeight;
        listBottom = screenBottom;

        // list starting X coordinate (after border) list1 starts at screenLeft
        list2X = screenLeft + setsListWidth + borderWidth;
        list3X = list2X + timesListWidth + borderWidth;
        list4X = list3X + timesListWidth + borderWidth;

        // panels for middle section
        int listHeight = listBottom - listTop;
        setsListPanel = new SetsListPanel(screenLeft, listTop, setsListWidth, listHeight, sets,
                (set, button) -> {
                        if (button == 0) { // left click
                            // refresh edit session or send confirm message
                            SplinterSet currActiveSet = SplinterClient.setManager.getActiveSet();
                            if (currActiveSet != set) {
                                if (SplinterClient.ssm.isEditingWithChanges()) {
                                    // editing with changes
                                    client.player.sendMessage(new LiteralText("confirm or cancel changes in GUI")
                                            .styled(s -> s.withColor(Formatting.YELLOW)), false);
                                } else if (SplinterClient.ssm.isEditing()) {
                                    // just editing, refresh edit session
                                    SplinterClient.setManager.setActiveSet(set);
                                    SplinterClient.ssm.refreshEditSession();
                                } else if (SplinterClient.timer.isRunning()) {
                                    // timer is running, invalidate the run then switch
                                    SplinterClient.routeHandler.invalidateRun();
                                    SplinterClient.setManager.setActiveSet(set);
                                } else {
                                    // swap set if it's not already active
                                    SplinterClient.setManager.setActiveSet(set);
                                }
                            }
                        } else if (button == 1) { // right click logic
                            // RC + SHIFT or both are full
                            if (hasShiftDown() || (setA != null && setB != null)) {
                                // context menu
                                contextMenu.open(lastClickX, lastClickY, set, List.of(
                                        new ContextMenu.Option("Set as A", () -> {
                                            SplinterClient.setManager.setDisplayedSetA(set);
                                            init();
                                        }, 0xFFFFFF,
                                                SplinterClient.setManager.getDisplayedSetA() != set),
                                        new ContextMenu.Option("Set as B", () -> {
                                            SplinterClient.setManager.setDisplayedSetB(set);
                                            init();
                                        }, 0xFFFFFF,
                                                SplinterClient.setManager.getDisplayedSetB() != set),
                                        new ContextMenu.Option("Rename", () -> openRenameOverlay(set), 0xFFFFFF, true),
                                        new ContextMenu.Option("Clear", () -> openClearOverlay(set), 0xFFFFFF, !set.isEmpty()),
                                        new ContextMenu.Option("Duplicate", () -> {
                                            SplinterSet duplicate = new SplinterSet("Copy of " + set.getName(), false, new Route(set.getRoute()));
                                            SplinterClient.setManager.addSet(duplicate);
                                            init();
                                            },
                                                0xFFFFFF,
                                                true),
                                        new ContextMenu.Option("Delete", () -> openRemoveOverlay(set), 0xFF5555, !set.isGeneral())
                                ));
                            } else if (setA == null && setB != set) {
                                SplinterClient.setManager.setDisplayedSetA(set);
                                init();
                            } else if (setB == null && setA != set) {
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
                new LiteralText("NEW SET"),
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
                        SplinterClient.setManager.clearDisplayedSetA();
                        init();
                    }
            ));
        }

        if (setB != null) {
            addButton(new ButtonWidget(startX + headerWidth + (2 * borderWidth), screenTop, headerButtonLen, headerButtonLen,
                    new LiteralText("-"),
                    button -> {
                        SplinterClient.setManager.clearDisplayedSetB();
                        init();
                    }
            ));
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

        // GUI title text
        drawCenteredText(matrixStack, textRenderer, title, width / 2, 10, textColor);

        // top panel (create button, headers)
        int topPanelColor = 0x952D2D2D;
        fill(matrixStack, screenLeft, screenTop, screenRight, screenTop + tabHeight, topPanelColor);

        // middle panel (sets, times, stats)
        int middlePanelColor = 0xE0383840; // 88% opacity
        fill(matrixStack, screenLeft, listTop, screenRight, listBottom, middlePanelColor);

        int textHeight = textRenderer.fontHeight;
        int vertGap = 3;
        int hintGap = 10;
        // context menu hint
        String menuHintText1 = "Shift + Right Mouse";
        String menuHintText2 = "to open set context menu";

        textRenderer.drawWithShadow(matrixStack, menuHintText1, list4X + 5, screenBottom - hintGap - (vertGap + textHeight) * 5, textColor);
        textRenderer.drawWithShadow(matrixStack, menuHintText2, list4X + 5, screenBottom - hintGap - (vertGap + textHeight) * 4, textColor);// top panel (tabs)

        // edit mode hint
        String keybind = KeyInputHandler.TOGGLE_EDIT_BIND.getKeyBinding().getBoundKeyLocalizedText().getString();
        String editMessage1 = "enter idle mode by";
        String editMessage2 = "pressing the \"■\" symbol";
        String editMessage3 = "& enter edit mode with " + "\"" + keybind + "\"";

        textRenderer.drawWithShadow(matrixStack, editMessage1, list4X + 5, screenBottom - (vertGap + textHeight)* 3, textColor);
        textRenderer.drawWithShadow(matrixStack, editMessage2, list4X + 5, screenBottom - (vertGap + textHeight) * 2, textColor);
        textRenderer.drawWithShadow(matrixStack, editMessage3, list4X + 5, screenBottom - (vertGap + textHeight), textColor);// top panel (tabs)

        // outer border
        // top
        int outerBorderColor = 0xFF666666;
        fill(matrixStack, screenLeft - borderWidth, screenTop - borderWidth, screenRight + borderWidth, screenTop, outerBorderColor);
        // bottom
        fill(matrixStack, screenLeft - borderWidth, screenBottom, screenRight + borderWidth, screenBottom + borderWidth, outerBorderColor);
        // left
        fill(matrixStack, screenLeft - borderWidth, screenTop, screenLeft, screenBottom, outerBorderColor);
        // right
        fill(matrixStack, screenRight, screenTop, screenRight + borderWidth, screenBottom, outerBorderColor);

        // vertical borders between columns
        int verticalBorderColor = 0xFF555560;
        fill(matrixStack, list2X - borderWidth, screenTop, list2X, listBottom, verticalBorderColor);
        fill(matrixStack, list3X - borderWidth, screenTop, list3X, listBottom, verticalBorderColor);
        fill(matrixStack, list4X - borderWidth, screenTop, list4X, listBottom, verticalBorderColor);

        // headers
        headerTextY = screenTop + (tabHeight - textRenderer.fontHeight + 1) / 2;
        int setAX = list2X + headerButtonLen + 3;
        int setBX = setAX + timesListWidth;
        int headerWidth = timesListWidth - headerButtonLen - 6;
        int headersBorderColor = 0xFF666666;
        DrawableHelper.fill(matrixStack, screenLeft, listTop, screenRight,  listTop + borderWidth, headersBorderColor);

        if (setA != null) {
            textRenderer.drawWithShadow(matrixStack,
                    TruncateText.truncate(setA.getName(), headerWidth, textRenderer),
                    setAX, headerTextY, textColor);
        }

        if (setB != null) {
            textRenderer.drawWithShadow(matrixStack,
                    TruncateText.truncate(setB.getName(), headerWidth, textRenderer),
                    setBX, headerTextY, textColor);
        }

        // render middle ListPanels
        enableScissor();
        boolean showSetsHover = !contextMenu.isVisible();
        setsListPanel.render(matrixStack, textRenderer, mouseX, mouseY, showSetsHover);
        timesListPanelA.render(matrixStack, textRenderer, mouseX, mouseY, false);
        timesListPanelB.render(matrixStack, textRenderer, mouseX, mouseY, false);
        disableScissor();

        // render context menu
        if (contextMenu.isVisible()) {
            contextMenu.render(matrixStack, textRenderer, mouseX, mouseY);
        }

        // render stats and overlays
        renderStats(matrixStack);

        // overlay renders on top of everything
        if (activeOverlay != Overlay.NONE) renderOverlay(matrixStack);

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
        lastClickX = (int) mouseX;
        lastClickY = (int) mouseY;
        // overlays get first priority
        if (activeOverlay != Overlay.NONE) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        // context menu gets priority over setlist
        if (contextMenu.isVisible()) {
            if (contextMenu.handleClick(mouseX, mouseY)) return true;
            contextMenu.close();
            return true;
        }

        if(setsListPanel.handleClick(mouseX, mouseY, button)) return true;
        if(timesListPanelA != null && timesListPanelA.handleClick(mouseX, mouseY, button)) return true;
        if(timesListPanelB != null && timesListPanelB.handleClick(mouseX, mouseY, button)) return true;
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
            } else if (KeyInputHandler.GUI_SETS_BIND.getKeyBinding().matchesKey(keyCode, scanCode)
                    && (activeOverlay == Overlay.CREATE || activeOverlay == Overlay.RENAME)) {
                // don't close create overlay,  might want to use the letter for the name
                return false;
            }
            SetsScreen.toggle();
            return true;
        }
        // pass input into the CREATE Overlay text field
        if ((activeOverlay == Overlay.CREATE || activeOverlay == Overlay.RENAME) && createNameField != null) {
            return createNameField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderStats(MatrixStack matrixStack) {
        int statsX = list4X;
        int headerWidth = screenRight - statsX;
        drawCenteredText(matrixStack, textRenderer, new LiteralText("Stats Panel"), statsX + headerWidth / 2, headerTextY, textColor);

        int panelWidth = screenRight - list4X;
        int panelX = list4X;

        int dividerColor = 0xFF555560;
        // beginning position of the next column, after the border
        int colWidth = panelWidth / 4;
        int stats2X = panelX + panelWidth / 6 + borderWidth;
        int stats3X = stats2X + colWidth + borderWidth;
        int stats4X = stats3X + colWidth + borderWidth;

        int rowHeight = 18;
        int rowTop = listTop + rowHeight + padding;
        int rowBottom = listTop + rowHeight * 4;


        // vertical dividers
        fill(matrixStack, stats2X - borderWidth, listTop + borderWidth, stats2X, rowBottom, dividerColor);
        fill(matrixStack, stats3X - borderWidth, listTop + borderWidth, stats3X, rowBottom, dividerColor);
        fill(matrixStack, stats4X - borderWidth, listTop + borderWidth, stats4X, rowBottom, dividerColor);

        // info column (best, avg, SD)
        textRenderer.drawWithShadow(matrixStack, "BEST", panelX + padding, rowTop, textColor);
        textRenderer.drawWithShadow(matrixStack, "AVG", panelX + padding, rowTop + rowHeight, textColor);
        textRenderer.drawWithShadow(matrixStack, "SD", panelX + padding, rowTop + rowHeight * 2, textColor);

        // headers
        textRenderer.drawWithShadow(matrixStack, "Set A", stats2X + padding, listTop + padding, textColor);
        textRenderer.drawWithShadow(matrixStack, "Set B", stats3X + padding, listTop + padding, textColor);
        textRenderer.drawWithShadow(matrixStack, "Diff", stats4X + padding, listTop + padding, textColor);

        // data! setA col data (best, avg, SD), draw dashes if empty/null
        long[] setAStats = null;
        long[] setBStats = null;
        if (setA != null && !setA.getTimes().isEmpty()) {
            setAStats = new long[]{setA.getBest(), setA.getAverage(), setA.getStdDev()};
            for (int i = 0; i < 2; i++) {
                textRenderer.drawWithShadow(matrixStack, TimerFormatter.format(setAStats[i]), stats2X + padding, rowTop + (i * rowHeight), textColor);
            }
            String sdText = String.format("%.2fs", setAStats[2] / 1000.0);
            textRenderer.drawWithShadow(matrixStack, sdText, stats2X + padding, rowTop + (2 * rowHeight), textColor);
        } else {
            for (int i = 0; i < 3; i++) {
                textRenderer.drawWithShadow(matrixStack, "-", stats2X + padding, rowTop + (i * rowHeight), textColor);
            }
        }

        // setB stats
        if (setB != null && !setB.getTimes().isEmpty()) {
            setBStats = new long[]{setB.getBest(), setB.getAverage(), setB.getStdDev()};
            for (int i = 0; i < 2; i++) {
                textRenderer.drawWithShadow(matrixStack, TimerFormatter.format(setBStats[i]), stats3X + padding, rowTop + (i* rowHeight), textColor);
            }
            String sdText = String.format("%.2fs", setBStats[2] / 1000.0);
            textRenderer.drawWithShadow(matrixStack, sdText, stats3X + padding, rowTop + (2 * rowHeight), textColor);
        } else {
            for (int i = 0; i < 3; i++) {
                textRenderer.drawWithShadow(matrixStack, "-", stats3X + padding, rowTop + (i * rowHeight), textColor);
            }
        }

        // diff stats
        if (setAStats != null && setBStats != null) {
            for (int i = 0; i < 2; i++) {
                long diff = setAStats[i] - setBStats[i];
                String diffText = (diff > 0 ? "+" : "") + TimerFormatter.format(Math.abs(diff));
                int diffColor = diff > 0 ? 0xFF5555 : 0x55FF55; // red if A is slower, green if faster
                textRenderer.drawWithShadow(matrixStack, diffText, stats4X + padding, rowTop + (i * rowHeight), diffColor);
            }
        } else {
            for (int i = 0; i < 2; i++) {
                textRenderer.drawWithShadow(matrixStack, "-", stats4X + padding, rowTop + (i * rowHeight), textColor);
            }
        }

    }

    private void renderOverlay(MatrixStack matrixStack) {
        // should probably refactor overlays eventually, they are very ugly
        fill(matrixStack, overlayX, overlayY, overlayX + overlayWidth, overlayY + overlayHeight, 0xFF222222);

        if (activeOverlay == Overlay.CREATE) {
            // check if the confirm button should be active
            if (createNameField != null && confirmButton != null) {
                String name = createNameField.getText().trim();
                confirmButton.active = SetNameValidation.isValid(name);
            }

            drawCenteredText(matrixStack, textRenderer, new LiteralText("Name your set"), width / 2, overlayY + 5, textColor);
            if (createNameField != null) {
                createNameField.render(matrixStack, 0, 0, 0);
            }
        } else if (activeOverlay == Overlay.REMOVE) {
            if (showWarningIcon) {
                int imgSquish = 80;
                int imgSize = 80;
                int imgX = (width - imgSize) / 2;
                int imgY = overlayY - imgSize - 4;

                if (confirmButton != null) {
                    confirmButton.active = true;
                }

                assert client != null;
                client.getTextureManager().bindTexture(WARNING_ICON);
                DrawableHelper.drawTexture(matrixStack, imgX, imgY, 0, 0, imgSquish, imgSquish, imgSize, imgSize);
            }
            drawCenteredText(matrixStack, textRenderer, new LiteralText("Are you sure?"), width / 2, overlayY + 10, textColor);
        } else if (activeOverlay == Overlay.RENAME) {
            // check if the confirm button should be active
            if (createNameField != null && confirmButton != null) {
                String name = createNameField.getText().trim();
                confirmButton.active = SetNameValidation.isValid(name);
            }
            drawCenteredText(matrixStack, textRenderer, new LiteralText("Rename set"), width / 2, overlayY + 5, textColor);
            if (createNameField != null) {
                // show current name above text field
                String currentName = "\"" + overlayTargetSet.getName() + "\"";
                drawCenteredText(matrixStack, textRenderer, new LiteralText(currentName), width / 2, overlayY + 18, 0xAAAAAA);
                createNameField.render(matrixStack, 0, 0, 0);
            }
        } else if (activeOverlay == Overlay.CLEAR) {
            drawCenteredText(matrixStack, textRenderer, new LiteralText("Clear all times?"), width / 2, overlayY + 10, textColor);
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

    private void openRemoveOverlay(SplinterSet set) {
        showWarningIcon = Math.random() < 0.01; // 1% nolan
        activeOverlay = Overlay.REMOVE;

        int confirmWidth = 60;
        int confirmHeight = 20;
        int confirmX = (width - confirmWidth) / 2; // centered
        int confirmY = overlayY + 32;

        confirmButton = new ButtonWidget(confirmX, confirmY, confirmWidth, confirmHeight,
                new LiteralText("CONFIRM"),
                button-> {
                    SplinterClient.setManager.deleteSet(set);
                    closeOverlay();
                }
        );
        addButton(confirmButton);
    }

    private void openRenameOverlay(SplinterSet set) {
        overlayHeight = 85;
        overlayX = (width - overlayWidth) / 2;
        overlayY = (height - overlayHeight) / 2;
        activeOverlay = Overlay.RENAME;
        this.overlayTargetSet = set;

        if (confirmButton != null) buttons.remove(confirmButton);

        int inputWidth = 130;
        int inputHeight = 16;
        int inputX = (width - inputWidth) / 2;
        int inputY = overlayY + 32;

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
                        set.renameSet(name);
                        closeOverlay();
                    }
                }
        );
        addButton(confirmButton);
    }

    private void openClearOverlay(SplinterSet set) {
        overlayX = (width - overlayWidth) / 2;
        overlayY = (height - overlayHeight) / 2;
        activeOverlay = Overlay.CLEAR;

        if (confirmButton != null) buttons.remove(confirmButton);

        int confirmWidth = 60;
        int confirmHeight = 20;
        int confirmX = (width - confirmWidth) / 2; // centered
        int confirmY = overlayY + 32;

        confirmButton = new ButtonWidget(confirmX, confirmY, confirmWidth, confirmHeight,
                new LiteralText("CONFIRM"),
                button-> {
                    set.clearSet();
                    closeOverlay();
                }
        );
        addButton(confirmButton);
    }

    private void closeOverlay() {
        overlayHeight = 65; // for now hardcode this, later make it dynamic
        activeOverlay = Overlay.NONE;
        this.overlayTargetSet = null;
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
