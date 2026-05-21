package me.rred.splinter.client;

import me.rred.splinter.client.handler.RouteHandler;
import me.rred.splinter.client.keyboard.KeyInputHandler;
import me.rred.splinter.client.sets.SetManager;
import me.rred.splinter.client.timer.SplinterTimer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SplinterClient implements ClientModInitializer {
    public static SplinterTimer timer = new SplinterTimer();
    public static SplinterStateMachine ssm = new SplinterStateMachine();
    public static SetManager setManager = new SetManager();
    public static RouteHandler routeHandler = new RouteHandler();

    @Override
    public void onInitializeClient() {
        KeyInputHandler.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            routeHandler.tick();
        });
    }
}
