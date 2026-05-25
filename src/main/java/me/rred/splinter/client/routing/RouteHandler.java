package me.rred.splinter.client.routing;

import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.SplinterStateMachine;
import me.rred.splinter.client.events.triggers.BlockBreakTrigger;
import me.rred.splinter.client.events.triggers.MapTrigger;
import me.rred.splinter.client.events.triggers.PositionTrigger;
import me.rred.splinter.client.events.triggers.Trigger;
import me.rred.splinter.client.rendering.BlockOutlineRenderer;
import me.rred.splinter.client.utils.TriggersSharePos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class RouteHandler {
    boolean startFired = false;
    boolean endFired = false;
    private boolean initialized = false;

    public void tick() {
        if (SplinterClient.ssm.getState() != SplinterStateMachine.State.ACTIVE) return;
        if (!initialized) {
            initialized = true;
        }
        // tick poll-based events
        Route route = SplinterClient.setManager.getActiveSet().getRoute();
        Trigger start = route.getStartTrigger();
        Trigger end = route.getEndTrigger();

        if (start instanceof PositionTrigger pt) pt.tick();

        // only tick after start fires
        if (startFired && end instanceof PositionTrigger pt) pt.tick();
        checkTriggers(route);
    }

    public void onMapTickUpdated(int tick) {
        if (!SplinterClient.ssm.isActive()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        Route route = SplinterClient.setManager.getActiveSet().getRoute();
        Trigger start = route.getStartTrigger();
        Trigger end = route.getEndTrigger();

        // check for decayed block break triggers, this is only run during inMap time
        if (start instanceof BlockBreakTrigger bt && bt.getPos() != null
                && !startFired && client.world.getBlockState(bt.getPos()).isAir()) {
            invalidateRun(client, "starting");
            return;
        }
        if (end instanceof BlockBreakTrigger bt && bt.getPos() != null
                && startFired && !endFired && client.world.getBlockState(bt.getPos()).isAir()) {
            invalidateRun(client, "ending");
            return;
        }

        if (start instanceof MapTrigger mt) mt.mapTick(tick);
        if (end instanceof MapTrigger mt) mt.mapTick(tick);

        checkTriggers(route);
    }

    public void render() {
        if (!SplinterClient.ssm.isActive()) return;
        if (!SplinterClient.ssm.isInMap()) return;

        Route route = SplinterClient.setManager.getActiveSet().getRoute();
        Trigger start = route.getStartTrigger();
        Trigger end = route.getEndTrigger();

        boolean isShared = TriggersSharePos.check(start, end);
        boolean bothActive = isShared && !startFired && !endFired;

        renderTriggerOutline(start, startFired, Color.GREEN, 0f);
        renderTriggerOutline(end, endFired, Color.RED, bothActive ? 0.05f : 0f);
    }

    public void resetFired() {
        startFired = false;
        endFired = false;
    }

    private void checkTriggers(Route route) {
        Trigger start = route.getStartTrigger();
        Trigger end = route.getEndTrigger();
        boolean inMap = SplinterClient.ssm.isInMap();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!inMap) {
            resetFired();
        }
        if (start.isTriggered() && !startFired) {
            SplinterClient.timer.clear();
            SplinterClient.timer.start();
            start.reset();
            startFired = true;
            if (end instanceof PositionTrigger pt) pt.primed = false;
        }

        if (end.isTriggered()) {
            if (SplinterClient.timer.isRunning()) {
                SplinterClient.timer.stop();
                long time = SplinterClient.timer.fetchElapsedTime();
                SplinterClient.setManager.addTime(time);
                endFired = true;
            }
            end.reset();
        }
    }

    public void onBlockBroken(BlockPos pos) {
        if (SplinterClient.ssm.getState() != SplinterStateMachine.State.ACTIVE) return;

        Route route = SplinterClient.setManager.getActiveSet().getRoute();
        Trigger start = route.getStartTrigger();
        Trigger end = route.getEndTrigger();

        if (start instanceof BlockBreakTrigger bt && bt.matches(pos)) {
            start.onFired();
            checkTriggers(route);
        }
        if (end instanceof BlockBreakTrigger bt && bt.matches(pos)) {
            MinecraftClient client = MinecraftClient.getInstance();
            assert client.world != null;
            // end trigger is broken before route starts
            if (!startFired) {
                startFired = true;
                endFired = true;
                assert client.player != null;
                client.player.sendMessage( new LiteralText("trial stopped due to premature ending trigger")
                        .styled(s -> s.withColor(Formatting.RED)), false);
                SplinterClient.timer.clear();
                return;
            }
            end.onFired();
            checkTriggers(route);
        }

    }

    public void toggleTimer() {
        // don't allow toggling outside of map
        if (!SplinterClient.ssm.isInMap()) return;
        if (!SplinterClient.ssm.isActive()) return;

        if (SplinterClient.timer.isRunning()) {
            SplinterClient.timer.stop();
            long time = SplinterClient.timer.fetchElapsedTime();
            SplinterClient.setManager.addTime(time);
        } else {
            SplinterClient.timer.clear();
            SplinterClient.timer.start();
        }
        Route route = SplinterClient.setManager.getActiveSet().getRoute();
        route.getStartTrigger().reset();
        route.getEndTrigger().reset();
        resetFired();
    }

    private void renderTriggerOutline(Trigger trigger, boolean fired, Color color, float padding) {
        if (fired) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        if (trigger instanceof BlockBreakTrigger bt && bt.getPos() != null) {
            new BlockOutlineRenderer(bt.getPos(), color, padding).render();
        }
        if (trigger instanceof PositionTrigger pt && pt.getPos() != null) {
            new BlockOutlineRenderer(pt.getPos(), color, padding).render();
        }
    }

    private void invalidateRun(MinecraftClient client, String triggerName) {
        startFired = true;
        endFired = true;
        SplinterClient.timer.clear();
        if (client.player != null) {
            client.player.sendMessage(new LiteralText("trial stopped due to broken " + triggerName + " trigger. " +
                    "you may want to edit the route")
                    .styled(s -> s.withColor(Formatting.RED)), false);
        }
    }

    public void invalidateRun() {
        startFired = true;
        endFired = true;
        SplinterClient.timer.clear();
    }

    public void onWorldJoin() {
        initialized = false;
        resetFired();
    }
}
