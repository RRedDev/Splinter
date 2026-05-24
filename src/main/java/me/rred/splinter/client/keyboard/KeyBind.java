package me.rred.splinter.client.keyboard;

import me.rred.splinter.Splinter;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static me.rred.splinter.client.keyboard.KeyInputHandler.KEY_CATEGORY;

public class KeyBind {
    private final Runnable execute;
    private final KeyBinding keyBinding;

    public KeyBind(String name, int keyCode, Runnable execute) {
        this.execute = execute;
        this.keyBinding = new KeyBinding(
                name,
                InputUtil.Type.KEYSYM,
                keyCode,
                KEY_CATEGORY
        );
        KeyBindingHelper.registerKeyBinding(this.keyBinding);
    }

    public void update() {
        if (this.keyBinding.wasPressed()) {
            this.execute.run();
        }
    }

    public KeyBinding getKeyBinding() {
        return keyBinding;
    }
}
