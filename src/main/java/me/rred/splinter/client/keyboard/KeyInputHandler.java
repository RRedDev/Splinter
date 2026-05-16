package me.rred.splinter.client.keyboard;

import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.handler.BlockTargetHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String KEY_CATEGORY = "key.category.splinter";
    public static final String SELECT_BLOCK = "key.splinter.select_block";
    public static final String CLEAR_SELECTION = "key.splinter.clear_selection";
    public static final String TOGGLE_TIMER = "key.splinter.toggle_timer";
    public static final String LOG_TIME = "key.splinter.log_time";
    public static final String GUI_SETS = "key.splinter.gui_sets";



    public static void register() {
        KeyBind[] keyBinds = new KeyBind[] {
                new KeyBind(SELECT_BLOCK, GLFW.GLFW_KEY_H, BlockTargetHandler::toggleOutline),
                new KeyBind(CLEAR_SELECTION, GLFW.GLFW_KEY_K, SplinterClient.tsm::onClear),
                new KeyBind(TOGGLE_TIMER, GLFW.GLFW_KEY_N, SplinterClient.tsm::toggleTimer),
                new KeyBind(LOG_TIME, GLFW.GLFW_KEY_6, SplinterClient.tsm::logTime),
        };

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            for (KeyBind keyBind : keyBinds) {
                keyBind.update();
            }
        });
    }
}
