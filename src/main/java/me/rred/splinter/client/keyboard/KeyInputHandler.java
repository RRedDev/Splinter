package me.rred.splinter.client.keyboard;

import me.rred.splinter.client.EditSession;
import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.SplinterStateMachine;
import me.rred.splinter.client.gui.SetsScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String KEY_CATEGORY = "key.category.splinter";
    public static final String SELECT_BLOCK = "key.splinter.select_block";
    public static final String CLEAR_SELECTION = "key.splinter.clear_selection";
    public static final String TOGGLE_TIMER = "key.splinter.toggle_timer";
    public static final String GUI_SETS = "key.splinter.gui_sets";
    public static final String TEMP_TOGGLE_EDIT = "key.splinter.temp_toggle_edit";
    public static final String TEMP_SELECT_ACTIVE = "key.splinter.temp_select_active";
    public static final String TEMP_CONFIRM = "key.splinter.temp_confirm";
    public static final String TEMP_TOGGLE_ACTIVE = "key.splinter.temp_toggle_active";
    public static final String TEMP_CYCLE_TYPE = "key.splinter.temp_cycle_type";


    public static KeyBind GUI_SETS_BIND;



    public static void register() {
        GUI_SETS_BIND = new KeyBind(GUI_SETS, GLFW.GLFW_KEY_B, SetsScreen::toggle);

        KeyBind[] keyBinds = new KeyBind[] {
                new KeyBind(TOGGLE_TIMER, GLFW.GLFW_KEY_N, SplinterClient.routeHandler::toggleTimer),
                new KeyBind(TEMP_TOGGLE_EDIT, GLFW.GLFW_KEY_M, () -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) return;
                    if (SplinterClient.ssm.getState() == SplinterStateMachine.State.EDIT) {
                        client.player.sendMessage(new LiteralText("idle mode entered"), false);
                        SplinterClient.ssm.setIdle();
                    } else if (SplinterClient.ssm.getState() != SplinterStateMachine.State.EDIT) {
                        if (client.player == null) return;
                        client.player.sendMessage(new LiteralText("edit mode entered"), false);
                        SplinterClient.ssm.setEdit();
                    }
                }),

                new KeyBind(TEMP_SELECT_ACTIVE, GLFW.GLFW_KEY_K, () -> {
                    if (SplinterClient.ssm.getState() == SplinterStateMachine.State.EDIT) {
                        EditSession edit = SplinterClient.ssm.getEditSession();
                        if (edit != null) edit.selectActive();
                    }
                }),

                new KeyBind(TEMP_TOGGLE_ACTIVE, GLFW.GLFW_KEY_P, () -> {
                    if (SplinterClient.ssm.getState() == SplinterStateMachine.State.EDIT) {
                        EditSession edit = SplinterClient.ssm.getEditSession();
                        if (edit != null) edit.toggleActiveSlot();
                    }
                }),

                new KeyBind(TEMP_CYCLE_TYPE, GLFW.GLFW_KEY_O, () -> {
                    if (SplinterClient.ssm.getState() == SplinterStateMachine.State.EDIT) {
                        EditSession edit = SplinterClient.ssm.getEditSession();
                        if (edit != null) edit.cycleActiveType();
                    }
                }),

                new KeyBind(TEMP_CONFIRM, GLFW.GLFW_KEY_PERIOD, () -> {
                    if (SplinterClient.ssm.getState() == SplinterStateMachine.State.EDIT) {
                        EditSession edit = SplinterClient.ssm.getEditSession();
                        if (edit != null) edit.confirm();
                    }
                }),

                GUI_SETS_BIND
        };

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            for (KeyBind keyBind : keyBinds) {
                keyBind.update();
            }
        });
    }
}
