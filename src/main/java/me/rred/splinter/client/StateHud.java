package me.rred.splinter.client;

import me.rred.splinter.client.edit.EditSession;
import me.rred.splinter.client.edit.gui.EditHud;
import me.rred.splinter.client.keyboard.KeyInputHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class StateHud {
    private static boolean hintConsumed = false;
    public static void render(MatrixStack matrixStack, TextRenderer textRenderer) {
        String setsOpenBind = KeyInputHandler.GUI_SETS_BIND.getKeyBinding().getBoundKeyLocalizedText().getString();
        String setsHintText = "enter sets gui with \"" + setsOpenBind + "\"";
        if (!hintConsumed) {
            textRenderer.drawWithShadow(matrixStack, setsHintText, 10, 24, 0xFFEE00);
        }

        String stateText = SplinterClient.ssm.getState().toString();
        String setText = SplinterClient.setManager.getActiveSet().getName();

        if (!stateText.equals("IDLE")) {
            int color = (stateText.equals("ACTIVE")) ? 0x55FF55 : 0xFFBB00;
            String setLabel = setText + " - ";
            int setTextWidth = textRenderer.getWidth(setLabel);
            textRenderer.drawWithShadow(matrixStack, setLabel, 10, 10, 0xFFFFFF);
            textRenderer.drawWithShadow(matrixStack, stateText, 10 + setTextWidth, 10, color);

            if (stateText.equals("EDIT")) {
                EditSession session = SplinterClient.ssm.getEditSession();
                if (session != null) {
                    EditHud.render(matrixStack, textRenderer, session);
                }
            }
        }
    }

    public static boolean isHintConsumed() {
        return hintConsumed;
    }

    public static void setHintConsumed(boolean value) {
        hintConsumed = value;
    }
}
