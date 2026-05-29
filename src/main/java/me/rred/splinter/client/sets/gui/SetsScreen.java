package me.rred.splinter.client.sets.gui;

import me.rred.splinter.Splinter;
import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.keyboard.KeyInputHandler;
import me.rred.splinter.client.routing.Route;
import me.rred.splinter.client.widgets.modals.ConfirmModal;
import me.rred.splinter.client.widgets.modals.InputModal;
import me.rred.splinter.client.widgets.modals.SplinterModal;
import me.rred.splinter.client.sets.SplinterSet;
import me.rred.splinter.client.utils.ScissorUtil;
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
    private SplinterModal activeModal;

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
                                        new ContextMenu.Option("Rename", () -> {
                                            activeModal = new InputModal("Rename Set", () -> {
                                                if(activeModal instanceof InputModal im) {
                                                    String name = im.getTextInput();
                                                    if (name == null || name.isEmpty()) return;
                                                    activeSet.renameSet(name);
                                                }
                                                activeModal = null;
                                                init();
                                            });
                                            String setName = activeSet.getName();
                                            activeModal.setSubmessage(setName);
                                            activeModal.openModal(width, height);
                                        }, 0xFFFFFF, true),
                                        new ContextMenu.Option("Clear", () -> {
                                            activeModal = new ConfirmModal("Clear Times?", () -> {
                                                activeSet.clearSet();
                                                activeModal = null;
                                                init();
                                            });
                                            activeModal.openModal(width, height);
                                        }, 0xFFFFFF, !set.isEmpty()),
                                        new ContextMenu.Option("Duplicate", () -> {
                                            SplinterSet duplicate = new SplinterSet("Copy of " + set.getName(), false, new Route(set.getRoute()));
                                            SplinterClient.setManager.addSet(duplicate);
                                            init();
                                            },
                                                0xFFFFFF,
                                                true),
                                        new ContextMenu.Option("Delete", () -> {
                                            activeModal = new ConfirmModal("Are You Sure?", () -> {
                                                SplinterClient.setManager.deleteSet(set);
                                                activeModal = null;
                                                init();
                                            });
                                            activeModal.openModal(width, height);
                                        }, 0xFF5555, !set.isGeneral())
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
                button -> {
                    activeModal = new InputModal("Choose Set Name", () -> {
                        if(activeModal instanceof InputModal im) {
                            String name = im.getTextInput();
                            if (name == null || name.isEmpty()) return;
                            SplinterClient.setManager.createSet(name);
                        }
                        activeModal = null;
                        init();
                    });
                    activeModal.openModal(width, height);
                }
        ));

        if (activeModal != null) activeModal.openModal(width, height);

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

        double scale = client.getWindow().getScaleFactor();
        int scissorWidth = screenRight - screenLeft;
        int scissorHeight = listBottom - listTop;

        ScissorUtil.enable(scale, screenLeft, listTop, scissorWidth, scissorHeight);
        boolean showSetsHover = !contextMenu.isVisible();
        setsListPanel.render(matrixStack, textRenderer, mouseX, mouseY, showSetsHover);
        timesListPanelA.render(matrixStack, textRenderer, mouseX, mouseY, false);
        timesListPanelB.render(matrixStack, textRenderer, mouseX, mouseY, false);
        ScissorUtil.disable();

        // render context menu
        if (contextMenu.isVisible()) {
            contextMenu.render(matrixStack, textRenderer, mouseX, mouseY);
        }

        // render stats and overlays
        renderStats(matrixStack);

        // overlay renders on top of everything
//        if (activeOverlay != Overlay.NONE) renderOverlay(matrixStack);
        if (activeModal != null) {
            activeModal.render(matrixStack, textRenderer, mouseX, mouseY);
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
        lastClickX = (int) mouseX;
        lastClickY = (int) mouseY;

        if (activeModal != null) {
            boolean pressed = activeModal.handleClick(mouseX, mouseY, button);
            if (activeModal != null && !activeModal.isVisible()) activeModal = null;
            return pressed;
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
        // pass input to activeModal
        if (activeModal != null) {
            boolean pressed = activeModal.keyPressed(keyCode, scanCode, modifiers);
            if (!activeModal.isVisible()) activeModal = null;
            return pressed;
        }
        // leave screen with Esc or specified hotkey
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || KeyInputHandler.GUI_SETS_BIND.getKeyBinding().matchesKey(keyCode, scanCode)) {
            SetsScreen.toggle();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (activeModal != null) return activeModal.charTyped(chr, keyCode);
        return super.charTyped(chr, keyCode);
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

    public static void toggle() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert(client != null);
        if (client.currentScreen instanceof SetsScreen) {
            client.openScreen(null);
        } else {
            client.openScreen(new SetsScreen());
        }
    }
}
