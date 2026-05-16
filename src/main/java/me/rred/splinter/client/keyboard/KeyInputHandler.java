package me.rred.splinter.client.keyboard;

import me.rred.splinter.client.handler.BlockTargetHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String KEY_CATEGORY = "key.category.splinter";
    public static final String TOGGLE_OUTLINE = "key.splinter.toggle_outline";

    public static void register() {
        KeyBind[] keyBinds = new KeyBind[] {
                new KeyBind(TOGGLE_OUTLINE, GLFW.GLFW_KEY_H, BlockTargetHandler::toggleOutline),
        };

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            for (KeyBind keyBind : keyBinds) {
                keyBind.update();
            }
        });
    }
}
