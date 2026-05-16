package me.rred.splinter.client;

import me.rred.splinter.client.keyboard.KeyInputHandler;
import net.fabricmc.api.ClientModInitializer;

public class SplinterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyInputHandler.register();
    }
}
