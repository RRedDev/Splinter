package me.rred.splinter.client;

import me.rred.splinter.client.events.triggers.BlockBreakTrigger;
import me.rred.splinter.client.events.triggers.MapTrigger;
import me.rred.splinter.client.events.triggers.Trigger;
import me.rred.splinter.client.rendering.BlockOutlineRenderer;
import me.rred.splinter.client.route.Route;
import me.rred.splinter.client.sets.SplinterSet;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.datafixer.fix.BlockEntityKeepPacked;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class EditSession {
    private Trigger pendingStart;
    private Trigger pendingEnd;
    private SplinterSet editSet;

    public EditSession(SplinterSet set) {
        this.editSet = set; // most likely just the active set for now
        this.pendingStart = set.getRoute().getStartTrigger();
        this.pendingEnd = set.getRoute().getEndTrigger();
    }

    public void renderHud(MatrixStack matrixStack, TextRenderer textRenderer) {
        // HUD text;
        int x = 10;
        int y = 25; // below state indicator

        Trigger activeStart = editSet.getRoute().getStartTrigger();
        Trigger activeEnd = editSet.getRoute().getEndTrigger();

        String startText = "START: " + getTriggerHandle(activeStart);
        String endText = "END: " + getTriggerHandle(activeEnd);

        if (pendingStart != activeStart) startText += " → " + getTriggerHandle(pendingStart);
        if (pendingEnd != activeEnd) endText += " → " + getTriggerHandle(pendingEnd);

        textRenderer.drawWithShadow(matrixStack, startText, x, y, 0xFFAA00);
        textRenderer.drawWithShadow(matrixStack, endText, x, y + 12, 0xFFAA00);
    }

    public void renderOutlines() {
        Trigger activeStart = editSet.getRoute().getStartTrigger();
        Trigger activeEnd = editSet.getRoute().getEndTrigger();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        // active block triggers

        if (activeStart instanceof BlockBreakTrigger bt && bt.getPos() != null) {
            new BlockOutlineRenderer(bt.getPos(), new Color(0, 150, 0)).render();
        }
        if (activeEnd instanceof BlockBreakTrigger bt && bt.getPos() != null) {
            new BlockOutlineRenderer(bt.getPos(), new Color(150, 0, 0)).render();
        }

        // pending block triggers
        if (pendingStart instanceof BlockBreakTrigger bt && bt.getPos() != null) {
            // check if air
            if (client.world.getBlockState(bt.getPos()).isAir()) {
                bt.setPos(null);
            } else if (activeStart != pendingStart) {
                new BlockOutlineRenderer(bt.getPos(), Color.GREEN).render();
            }
        }

        if (pendingEnd instanceof BlockBreakTrigger bt && bt.getPos() != null) {
            // check if air
            if (client.world.getBlockState(bt.getPos()).isAir()) {
                bt.setPos(null);
            } else if (activeEnd != pendingEnd) {
                new BlockOutlineRenderer(bt.getPos(), Color.RED).render();
            }
        }
    }

    public void confirm() {
        if (pendingStart == null || pendingEnd == null) return;
        editSet.getRoute().setStartTrigger(pendingStart);
        editSet.getRoute().setEndTrigger(pendingEnd);
        SplinterClient.ssm.setIdle();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        client.player.sendMessage(new LiteralText("editsConfirmed"), false);
    }

    public boolean hasChanges() {
        Route current = editSet.getRoute();
        return pendingStart != current.getStartTrigger()
                || pendingEnd != current.getEndTrigger();
    }

    public Trigger getPendingStart() {
        return pendingStart;
    }

    public Trigger getPendingEnd() {
        return pendingEnd;
    }

    public void selectStartBlock() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!(client.crosshairTarget instanceof BlockHitResult hit)) return;

        BlockPos pos = hit.getBlockPos();
        if (client.world.getBlockState(pos).isAir()) return;

        pendingStart = new BlockBreakTrigger(Trigger.TriggerSlot.START, pos);
    }

    public void setStartType(Trigger.TriggerType type) {
        switch (type) {
            case MAP -> pendingStart = new MapTrigger(Trigger.TriggerSlot.START);
            case BLOCK_BREAK -> pendingStart = new BlockBreakTrigger(Trigger.TriggerSlot.START, null);
        }
    }

    public void setPendingStart(Trigger trigger) {
        pendingStart = trigger;
    }

    public void setPendingEnd(Trigger trigger) {
        pendingEnd = trigger;
    }

    private String getTriggerHandle(Trigger trigger) {
        if (trigger instanceof MapTrigger) return "MAP";
        if (trigger instanceof BlockBreakTrigger bt) {
            BlockPos pos = bt.getPos();
            return pos != null ? "BREAK (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")" : "BREAK (unset)";
        }
        return "UNKNOWN";
    }


}
