package me.rred.splinter.client.handler;

import me.rred.splinter.Splinter;
import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.SplinterStateMachine;
import me.rred.splinter.client.events.triggers.BlockBreakTrigger;
import me.rred.splinter.client.events.triggers.MapTrigger;
import me.rred.splinter.client.events.triggers.PositionTrigger;
import me.rred.splinter.client.events.triggers.Trigger;
import me.rred.splinter.client.rendering.BlockOutlineRenderer;
import me.rred.splinter.client.route.Route;
import me.rred.splinter.client.timer.SplinterTimer;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class RouteHandler {
    boolean startFired = false;
    boolean endFired = false;

    public void tick() {
        if (SplinterClient.ssm.getState() != SplinterStateMachine.State.ACTIVE) return;
        // tick poll-based events
        Route route = SplinterClient.setManager.getActiveSet().getRoute();
        Trigger start = route.getStartTrigger();
        Trigger end = route.getEndTrigger();

        if (start instanceof PositionTrigger pt && !startFired) pt.tick();
        if (end instanceof PositionTrigger pt && !endFired) pt.tick();

        checkTriggers(route);
    }

    public void render() {
        if (!SplinterClient.ssm.isActive()) return;
        if (!SplinterClient.ssm.isInMap()) return;

        Route route = SplinterClient.setManager.getActiveSet().getRoute();
        Trigger start = route.getStartTrigger();
        Trigger end = route.getEndTrigger();

        renderTriggerOutline(start, startFired, Color.GREEN);
        renderTriggerOutline(end, endFired, Color.RED);
    }

    public void onMapTickUpdated(int tick) {
        if (!SplinterClient.ssm.isActive()) return;

        Route route = SplinterClient.setManager.getActiveSet().getRoute();
        Trigger start = route.getStartTrigger();
        Trigger end = route.getEndTrigger();

        if (start instanceof MapTrigger mt) mt.mapTick(tick);
        if (end instanceof MapTrigger mt) mt.mapTick(tick);

        checkTriggers(route);
    }

    private void checkTriggers(Route route) {
        Trigger start = route.getStartTrigger();
        Trigger end = route.getEndTrigger();

        if (start.isTriggered()) {
            SplinterClient.timer.clear();
            SplinterClient.timer.start();
            start.reset();
            startFired = true;
            endFired = false;
        }

        if (end.isTriggered()) {
            if (SplinterClient.timer.isRunning()) {
                SplinterClient.timer.stop();
                long time = SplinterClient.timer.fetchElapsedTime();
                SplinterClient.setManager.addTime(time);
                endFired = true;
                startFired = false;
            }
            end.reset();
        }
    }

    public void onBlockBroken(BlockPos pos) {
        if (SplinterClient.ssm.getState() != SplinterStateMachine.State.ACTIVE) return;

        Route route = SplinterClient.setManager.getActiveSet().getRoute();
        Trigger start = route.getStartTrigger();
        Trigger end = route.getEndTrigger();

        if (start instanceof BlockBreakTrigger bt && bt.matches(pos)) start.onFired();
        if (end instanceof BlockBreakTrigger bt && bt.matches(pos)) end.onFired();

    }

    public void toggleTimer() {
        // don't allow toggling outside of map
        if (!SplinterClient.ssm.isInMap()) return;

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
        startFired = false;
        endFired = false;
    }

    private void renderTriggerOutline(Trigger trigger, boolean fired, Color color) {
        if (fired) return;
        if (trigger instanceof BlockBreakTrigger bt && bt.getPos() != null) {
            new BlockOutlineRenderer(bt.getPos(), color).render();
        }
        if (trigger instanceof PositionTrigger pt && pt.getPos() != null) {
            new BlockOutlineRenderer(pt.getPos(), color).render();
        }
    }

}
