package me.rred.splinter.client;

import me.rred.splinter.client.events.triggers.BlockBreakTrigger;
import me.rred.splinter.client.events.triggers.MapTrigger;
import me.rred.splinter.client.events.triggers.PositionTrigger;
import me.rred.splinter.client.events.triggers.Trigger;
import me.rred.splinter.client.gui.EditScreen;
import me.rred.splinter.client.rendering.BlockOutlineRenderer;
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
    private Trigger activeTrigger;
    private Trigger oldActiveTrigger;
    private final Trigger ogStart;
    private final Trigger ogEnd;
    private Trigger pendingStart;
    private Trigger pendingEnd;
    private final SplinterSet editSet;
    private BlockPos hoveredPos;


    public EditSession(SplinterSet set) {
        this.editSet = set; // most likely just the active set for now
        this.ogStart = set.getRoute().getStartTrigger();
        this.ogEnd = set.getRoute().getEndTrigger();
        this.pendingStart = ogStart;
        this.pendingEnd = ogEnd;
    }

    public void renderHud(MatrixStack matrixStack, TextRenderer textRenderer) {
        // HUD text;
        int x = 10;
        int y = 25; // below state indicator

        Trigger activeStart = editSet.getRoute().getStartTrigger();
        Trigger activeEnd = editSet.getRoute().getEndTrigger();

        String startText = "START: " + getTriggerHandle(activeStart);
        String endText = "END: " + getTriggerHandle(activeEnd);

        if (!pendingStart.equals(activeStart)) startText += " → " + getTriggerHandle(pendingStart);
        if (!pendingEnd.equals(activeEnd)) endText += " → " + getTriggerHandle(pendingEnd);

        textRenderer.drawWithShadow(matrixStack, startText, x, y, 0xFFAA00);
        textRenderer.drawWithShadow(matrixStack, endText, x, y + 12, 0xFFAA00);
    }

    public void renderOutlines() {
        Trigger activeStart = editSet.getRoute().getStartTrigger();
        Trigger activeEnd = editSet.getRoute().getEndTrigger();

        // extract the hovered block for selection and draw outline
        Color hoverColor = Color.WHITE;
        if (getActiveSlot() == Trigger.TriggerSlot.START) {
            hoverColor = new Color(0, 100, 50);
        } else if (getActiveSlot() == Trigger.TriggerSlot.END) {
            hoverColor = new Color(100, 0, 50);
        }
        hoveredPos = getHoveredPos();
        if (hoveredPos != null) {
            new BlockOutlineRenderer(hoveredPos, hoverColor).render();
        }

        // selected block outlines
        boolean activeShared = TriggersSharePos.check(activeStart, activeEnd);
        renderTriggerOutline(activeStart, false, Color.GREEN , 0f);
        renderTriggerOutline(activeEnd, false, Color.RED, activeShared ? 0.05f : 0f);

        // pending block outlines
        boolean pendingShared = TriggersSharePos.check(pendingStart, pendingEnd);
        boolean startsShare = TriggersSharePos.check(pendingStart, ogStart);
        boolean endsShare = TriggersSharePos.check(pendingEnd, ogEnd);

        if (pendingStart != null && !startsShare) {
            renderTriggerOutline(pendingStart, true, new Color(0, 200, 100), 0f);
        }
        if (pendingEnd != null && !endsShare) {
            renderTriggerOutline(pendingEnd, true, new Color(200, 0, 100), pendingShared ? 0.05f : 0f);
        }
    }

    public void selectActive() {
        if (activeTrigger == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        switch (getActiveType()) {
            case MAP -> {
                if (getActiveSlot() == Trigger.TriggerSlot.START) {
                    pendingStart = new MapTrigger(Trigger.TriggerSlot.START);
                } else {
                    pendingEnd = new MapTrigger(Trigger.TriggerSlot.END);
                }
            }
            case BLOCK_BREAK -> {
                if (hoveredPos == null) return;
                // prevent selection if position already in use
                Trigger other = getActiveSlot() == Trigger.TriggerSlot.START ? pendingEnd : pendingStart;
                if (other instanceof BlockBreakTrigger bt && hoveredPos.equals(bt.getPos())) {
                    client.player.sendMessage(new LiteralText("block already used by other trigger"), false);
                    return;
                }
                if (other instanceof PositionTrigger pt && hoveredPos.equals(pt.getPos())) {
                    client.player.sendMessage(new LiteralText("block already used by other trigger"), false);
                    return;
                }

                if (getActiveSlot() == Trigger.TriggerSlot.START) {
                    pendingStart = new BlockBreakTrigger(Trigger.TriggerSlot.START, hoveredPos);
                } else {
                    pendingEnd = new BlockBreakTrigger(Trigger.TriggerSlot.END, hoveredPos);
                }
            }
            case POSITION -> {
                if (hoveredPos == null) return;
                if (getActiveSlot() == Trigger.TriggerSlot.START) {
                    pendingStart = new PositionTrigger(Trigger.TriggerSlot.START, hoveredPos);
                } else {
                    pendingEnd = new PositionTrigger(Trigger.TriggerSlot.END, hoveredPos);
                }
            }
        }
    }

    public void setActiveSlot(Trigger.TriggerSlot slot) {
        if (getActiveSlot() == slot) return;
        Trigger tempTrigger = activeTrigger;
        if (oldActiveTrigger != null) {
            activeTrigger = oldActiveTrigger;
        } else {
            activeTrigger = slot == Trigger.TriggerSlot.START ? pendingStart : pendingEnd;
        }
        oldActiveTrigger = tempTrigger;
    }

    public void setActiveType(Trigger.TriggerType type) {
        if (activeTrigger == null) return;
//        if (getActiveSlot() == Trigger.TriggerSlot.START) {
//            pendingStart = ogStart;
//        } else {
//            pendingEnd = ogEnd;
//        }
        switch (type) {
            case MAP -> {
                if (getActiveSlot() == Trigger.TriggerSlot.START) {
                    activeTrigger = new MapTrigger(Trigger.TriggerSlot.START);
                } else {
                    activeTrigger = new MapTrigger(Trigger.TriggerSlot.END);
                }
            }
            case BLOCK_BREAK -> {
                if (getActiveSlot() == Trigger.TriggerSlot.START) {
                    activeTrigger = new BlockBreakTrigger(Trigger.TriggerSlot.START, null);
                } else {
                    activeTrigger = new BlockBreakTrigger(Trigger.TriggerSlot.END, null);
                }
            }
            case POSITION -> {
                if (getActiveSlot() == Trigger.TriggerSlot.START) {
                    activeTrigger = new PositionTrigger(Trigger.TriggerSlot.START, null);
                } else {
                    activeTrigger = new PositionTrigger(Trigger.TriggerSlot.END, null);
                }
            }
        }
    }

    public void setActiveSlot(Trigger.TriggerSlot slot, Trigger.TriggerType type) {
        setActiveSlot(slot);
        setActiveType(type);
    }

    public void confirm() {
        if (pendingStart == null || pendingEnd == null) return;
        editSet.getRoute().setStartTrigger(pendingStart);
        editSet.getRoute().setEndTrigger(pendingEnd);
        SplinterClient.ssm.setIdle();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        client.player.sendMessage(new LiteralText("editsConfirmed"), false);
        if (client.currentScreen instanceof EditScreen) {
            client.openScreen(null);
        }
    }

    public void cancel() {
        pendingStart = ogStart;
        pendingEnd = ogEnd;
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
        if (getActiveType() == null) return null;

        // block break: return non-air selected block
        switch (getActiveType()) {
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
        if (activeTrigger == null) return;
        Trigger.TriggerType[] types = Trigger.TriggerType.values();
        if (getActiveType() == null) {
            setActiveType(types[0]);
            return;
        }
        int next = (getActiveType().ordinal() + 1) % types.length;
        setActiveType(Trigger.TriggerType.values()[next]);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        client.player.sendMessage(new LiteralText("type: " + getActiveType().name()), false);
    }

    public boolean hasChanges() {
        return !pendingStart.equals(ogStart)
                || !pendingEnd.equals(ogEnd);
    }

    public Trigger.TriggerSlot getActiveSlot() {
        return activeTrigger == null ? null : activeTrigger.getTriggerSlot();
    }

    public Trigger.TriggerType getActiveType() {
        return activeTrigger == null ? null : activeTrigger.getType();
    }

}
