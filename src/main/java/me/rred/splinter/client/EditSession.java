package me.rred.splinter.client;

import me.rred.splinter.client.events.triggers.BlockBreakTrigger;
import me.rred.splinter.client.events.triggers.MapTrigger;
import me.rred.splinter.client.events.triggers.PositionTrigger;
import me.rred.splinter.client.events.triggers.Trigger;
import me.rred.splinter.client.rendering.BlockOutlineRenderer;
import me.rred.splinter.client.route.Route;
import me.rred.splinter.client.sets.SplinterSet;
import me.rred.splinter.client.utils.TriggersSharePos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class EditSession {
    private Trigger.TriggerSlot activeSlot;
    private Trigger.TriggerType activeType;
    private Trigger pendingStart;
    private Trigger pendingEnd;
    private SplinterSet editSet;
    private BlockPos hoveredPos;


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

        // extract the hovered block for selection and draw outline
        Color hoverColor = Color.WHITE;
        if (activeSlot == Trigger.TriggerSlot.START) {
            hoverColor = new Color(50, 100, 0);
        } else if (activeSlot == Trigger.TriggerSlot.END) {
            hoverColor = new Color(100, 0, 50);
        }
        hoveredPos = getHoveredPos();
        if (hoveredPos != null) {
            new BlockOutlineRenderer(hoveredPos, hoverColor).render();
        }

        // active block outlines
        boolean activeShared = TriggersSharePos.check(activeStart, activeEnd);
        renderTriggerOutline(activeStart, false, new Color(0, 150, 0), 0f);
        renderTriggerOutline(activeEnd, false, new Color(150, 0, 0), activeShared ? 0.05f : 0f);

        // pending block outlines
        boolean pendingShared = TriggersSharePos.check(pendingStart, pendingEnd);
        renderTriggerOutline(pendingStart, true, Color.GREEN, 0f);
        renderTriggerOutline(pendingEnd, true, Color.RED, pendingShared ? 0.05f : 0f);

    }

    public void selectActive() {
        if (activeType == null || hoveredPos == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        switch (activeType) {
            case BLOCK_BREAK -> {
                // prevent selection if position already in use
                Trigger other = activeSlot == Trigger.TriggerSlot.START ? pendingEnd : pendingStart;
                if (other instanceof BlockBreakTrigger bt && hoveredPos.equals(bt.getPos())) {
                    client.player.sendMessage(new LiteralText("block already used by other trigger"), false);
                    return;
                }
                if (other instanceof PositionTrigger pt && hoveredPos.equals(pt.getPos())) {
                    client.player.sendMessage(new LiteralText("block already used by other trigger"), false);
                    return;
                }

                if (activeSlot == Trigger.TriggerSlot.START) {
                    pendingStart = new BlockBreakTrigger(Trigger.TriggerSlot.START, hoveredPos);
                } else {
                    pendingEnd = new BlockBreakTrigger(Trigger.TriggerSlot.END, hoveredPos);
                }
                client.player.sendMessage(new LiteralText("block break selected"), false);
            }
            case POSITION -> {
                if (activeSlot == Trigger.TriggerSlot.START) {
                    pendingStart = new PositionTrigger(Trigger.TriggerSlot.START, hoveredPos);
                } else {
                    pendingEnd = new PositionTrigger(Trigger.TriggerSlot.END, hoveredPos);
                }
                client.player.sendMessage(new LiteralText("position selected"), false);
            }
        }
        client.player.sendMessage(new LiteralText("selection complete"), false);

    }

    public void toggleActiveSlot() {
        if (activeSlot == null) {
            activeSlot = Trigger.TriggerSlot.START;
            activeType = pendingStart.getType();
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;


        if (activeSlot == Trigger.TriggerSlot.START) {
            activeSlot = Trigger.TriggerSlot.END;
            activeType = pendingEnd.getType();
            client.player.sendMessage(new LiteralText("active: end"), false);
        } else {
            activeSlot = Trigger.TriggerSlot.START;
            activeType = pendingStart.getType();
            client.player.sendMessage(new LiteralText("active: start"), false);
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

    public void setActiveType(Trigger.TriggerType type) {
        if (activeSlot == null) {
            activeSlot = Trigger.TriggerSlot.START;
        }
        switch (type) {
            case MAP -> {
                if (activeSlot == Trigger.TriggerSlot.START) {
                    pendingStart = new MapTrigger(Trigger.TriggerSlot.START);
                    activeType = pendingStart.getType();
                } else {
                    pendingEnd = new MapTrigger(Trigger.TriggerSlot.END);
                    activeType = pendingEnd.getType();
                }
            }
            case BLOCK_BREAK -> {
                if (activeSlot == Trigger.TriggerSlot.START) {
                    pendingStart = new BlockBreakTrigger(Trigger.TriggerSlot.START, null);
                    activeType = pendingStart.getType();
                } else {
                    pendingEnd = new BlockBreakTrigger(Trigger.TriggerSlot.END, null);
                    activeType = pendingEnd.getType();
                }
            }
            case POSITION -> {
                if (activeSlot == Trigger.TriggerSlot.START) {
                    pendingStart = new PositionTrigger(Trigger.TriggerSlot.START, null);
                    activeType = pendingStart.getType();
                } else {
                    pendingEnd = new PositionTrigger(Trigger.TriggerSlot.END, null);
                    activeType = pendingEnd.getType();
                }
            }
        }
    }

    public void setPendingStart(Trigger trigger) {
        pendingStart = trigger;
    }

    public void setPendingEnd(Trigger trigger) {
        pendingEnd = trigger;
    }

    private String getTriggerHandle(Trigger trigger) {
        if (trigger == null) return "NONE";
        return switch (trigger.getType()) {
            case MAP -> "MAP";
            case BLOCK_BREAK -> {
                BlockPos pos = ((BlockBreakTrigger) trigger).getPos();
                yield pos != null ? "BREAK (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")" : "BREAK (unset)";
            }
            case POSITION -> {
                BlockPos pos = ((PositionTrigger) trigger).getPos();
                yield pos != null ? "POS (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")" : "POS (unset)";
            }
        };
    }

    private void renderTriggerOutline(Trigger trigger, boolean pending, Color color, float padding) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        if (trigger instanceof BlockBreakTrigger bt && bt.getPos() != null) {
            // check if its air and a pending change
            if (pending && client.world.getBlockState(bt.getPos()).isAir()) {
                bt.setPos(null);
            } else {
                new BlockOutlineRenderer(bt.getPos(), color, padding).render();
            }
        }

        if (trigger instanceof PositionTrigger pt && pt.getPos() != null) {
            new BlockOutlineRenderer(pt.getPos(), color, padding).render();
        }
    }

    private BlockPos getHoveredPos() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return null;
        if (!(client.crosshairTarget instanceof BlockHitResult hit)) return null;
        if (activeType == null) return null;

        // block break: return non-air selected block
        switch (activeType) {
            case BLOCK_BREAK -> {
                BlockPos pos = hit.getBlockPos();
                return client.world.getBlockState(pos).isAir() ? null : pos;
            }
            case POSITION ->{
                BlockPos pos = hit.getBlockPos();
                return client.world.getBlockState(pos).isAir() ? pos : pos.offset(hit.getSide());
            }
        }
        return null;
    }

    public void cycleActiveType() {
        if (activeType == null) return;
        Trigger.TriggerType[] types = Trigger.TriggerType.values();
        if (activeType == null) {
            setActiveType(types[0]);
            return;
        }
        int next = (activeType.ordinal() + 1) % types.length;
        setActiveType(Trigger.TriggerType.values()[next]);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        client.player.sendMessage(new LiteralText("type: " + activeType.name()), false);
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


}
