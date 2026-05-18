package me.rred.splinter.client.keyboard;

import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.gui.SetsScreen;
import me.rred.splinter.client.handler.BlockTargetHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.include.com.google.common.collect.Sets;

public class KeyInputHandler {
    public static final String KEY_CATEGORY = "key.category.splinter";
    public static final String SELECT_BLOCK = "key.splinter.select_block";
    public static final String CLEAR_SELECTION = "key.splinter.clear_selection";
    public static final String TOGGLE_TIMER = "key.splinter.toggle_timer";
    public static final String LOG_TIME = "key.splinter.log_time";
    public static final String GUI_SETS = "key.splinter.gui_sets";
    public static KeyBind GUI_SETS_BIND;



    public static void register() {
        GUI_SETS_BIND = new KeyBind(GUI_SETS, GLFW.GLFW_KEY_B, SetsScreen::toggle);

        KeyBind[] keyBinds = new KeyBind[] {
                new KeyBind(SELECT_BLOCK, GLFW.GLFW_KEY_H, BlockTargetHandler::toggleOutline),
                new KeyBind(CLEAR_SELECTION, GLFW.GLFW_KEY_K, SplinterClient.tsm::onClear),
                new KeyBind(TOGGLE_TIMER, GLFW.GLFW_KEY_N, SplinterClient.tsm::toggleTimer),
                new KeyBind(LOG_TIME, GLFW.GLFW_KEY_M, SplinterClient.tsm::logTime),
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
