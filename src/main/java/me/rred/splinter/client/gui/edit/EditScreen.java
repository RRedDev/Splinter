package me.rred.splinter.client.gui.edit;

import me.rred.splinter.client.EditSession;
import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.events.triggers.Trigger;
import me.rred.splinter.client.keyboard.KeyInputHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

public class EditScreen extends Screen {

    private int screenTop, screenBottom, screenLeft, screenRight;
    private int offset = 50;
    private EditSession editSession;
    private static final int textColor = 0xFFFFFF;
    private int padding = 5;

    private TriggerSlotButton startButton;
    private TriggerSlotButton endButton;
    private TriggerTypeDropdown startDropdown;
    private TriggerTypeDropdown endDropdown;

    public EditScreen(EditSession editSession) {
        super(new LiteralText("Edit Route - " + SplinterClient.setManager.getActiveSet().getName()));
        this.editSession = editSession;
    }

    @Override
    protected void init() {
        buttons.clear();
        children.clear();

        // edit UI screen dimensions
        screenTop = offset - 25;
        screenBottom = height - offset;
        screenLeft = offset;
        screenRight = width - offset;

        // initialize slot buttons
        int btnLen = 35;
        int gap = 10;
        int totalHeight = btnLen * 2 + gap;
        int startX = width / 2 - 60;
        int startY = (height - totalHeight) / 2 - 20;
        int endY = startY + btnLen + gap;

        startButton = new TriggerSlotButton(startX, startY, btnLen, btnLen, Trigger.TriggerSlot.START);
        endButton = new TriggerSlotButton(startX, endY, btnLen, btnLen, Trigger.TriggerSlot.END);

        // dropdowns
        int dropdownX = startX + btnLen + gap;
        Trigger.TriggerType activeType = editSession.getActiveType();
        startDropdown = new TriggerTypeDropdown(dropdownX, startY,
                activeType,
                type -> editSession.setActiveSlot(Trigger.TriggerSlot.START, type));
        endDropdown = new TriggerTypeDropdown(dropdownX, endY,
                activeType,
                type -> editSession.setActiveSlot(Trigger.TriggerSlot.END, type));

        int confirmY = endY + btnLen + 8;
        int btnWidth = 60;
        int gap2 = 8;
        int totalBtnWidth = btnWidth * 2 + gap2;
        int btnCenterX = width / 2 - totalBtnWidth / 2;

        // confirm button
        if (editSession.hasChanges()) {
            addButton(new ButtonWidget(
                    btnCenterX + btnWidth + gap2, confirmY, btnWidth, 20,
                    new LiteralText("CONFIRM"),
                    button -> editSession.confirm()
            ));
        }
        // cancel button
        addButton(new ButtonWidget(
                btnCenterX, confirmY, btnWidth, 20,
                new LiteralText("CANCEL"),
                button -> editSession.cancel()
        ));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        // GUI title
        drawCenteredText(matrixStack, textRenderer, title, width / 2, 10, textColor);

        // render slot buttons
        boolean startSelected = editSession.getActiveSlot() == Trigger.TriggerSlot.START;
        boolean endSelected = editSession.getActiveSlot() == Trigger.TriggerSlot.END;

        startButton.render(matrixStack, textRenderer, mouseX, mouseY, startSelected);
        endButton.render(matrixStack, textRenderer, mouseX, mouseY, endSelected);
        Trigger.TriggerType activeType = editSession.getActiveType();

        if (editSession.getActiveSlot() == Trigger.TriggerSlot.START) {
            startDropdown.setSelected(activeType);
            startDropdown.render(matrixStack, textRenderer, mouseX, mouseY);
        }
        if (editSession.getActiveSlot() == Trigger.TriggerSlot.END) {
            endDropdown.setSelected(activeType);
            endDropdown.render(matrixStack, textRenderer, mouseX, mouseY);
        }
        super.render(matrixStack, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (editSession.getActiveSlot() == Trigger.TriggerSlot.START && startDropdown.handleClick(mouseX, mouseY)) return true;
        if (editSession.getActiveSlot() == Trigger.TriggerSlot.END && endDropdown.handleClick(mouseX, mouseY)) return true;

        if (startButton.handleClick(mouseX, mouseY)) {
            editSession.setActiveSlot(Trigger.TriggerSlot.START);
            return true;
        }
        if (endButton.handleClick(mouseX, mouseY)) {
            editSession.setActiveSlot(Trigger.TriggerSlot.END);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // leave screen with Esc or specified hotkey
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || KeyInputHandler.GUI_EDIT_BIND.getKeyBinding().matchesKey(keyCode, scanCode)) {
            EditScreen.toggle();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static void toggle() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof EditScreen) {
            client.openScreen(null);
        } else {
            EditSession edit = SplinterClient.ssm.getEditSession();
            if (edit != null) client.openScreen(new EditScreen(edit));
        }
    }


}
