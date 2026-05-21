package me.rred.splinter.client.handler;

import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.SplinterStateMachine;
import me.rred.splinter.client.events.triggers.BlockBreakTrigger;
import me.rred.splinter.client.events.triggers.MapTrigger;
import me.rred.splinter.client.events.triggers.Trigger;
import me.rred.splinter.client.rendering.BlockOutlineRenderer;
import me.rred.splinter.client.route.Route;
import me.rred.splinter.client.timer.SplinterTimer;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class RouteHandler {

    public void tick() {
        if (SplinterClient.ssm.getState() != SplinterStateMachine.State.ACTIVE) return;
        // tick poll-based events
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
        }

        if (end.isTriggered()) {
            SplinterClient.timer.stop();
            long time = SplinterClient.timer.fetchElapsedTime();
            SplinterClient.setManager.addTime(time);
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

    public void render() {
        if (!SplinterClient.ssm.isActive()) return;
        if (!SplinterClient.ssm.isInMap()) return;

        Route route = SplinterClient.setManager.getActiveSet().getRoute();
        Trigger start = route.getStartTrigger();
        Trigger end = route.getEndTrigger();

        if (start instanceof BlockBreakTrigger bt && bt.getPos() != null) {
            new BlockOutlineRenderer(bt.getPos(), Color.GREEN).render();
        }

        if (end instanceof BlockBreakTrigger bt && bt.getPos() != null) {
            new BlockOutlineRenderer(bt.getPos(), Color.RED).render();
        }
    }

    public void toggleTimer() {
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
    }

}
