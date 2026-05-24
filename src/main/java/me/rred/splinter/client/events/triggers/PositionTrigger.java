package me.rred.splinter.client.events.triggers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class PositionTrigger extends Trigger {
    private BlockPos pos;
    public boolean primed = true;

    public PositionTrigger(TriggerSlot triggerSlot, BlockPos pos) {
        super(triggerSlot);
        this.pos = pos;
    }

    @Override
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || pos == null) return;
        BlockPos feet = client.player.getBlockPos();
        BlockPos head = feet.up();
        boolean inPos = feet.equals(pos) || head.equals(pos);

        if (!inPos) primed = true; // must leave the position before it can be primed and fire
        if (inPos && primed) onFired();
    }

    @Override
    public void reset() {
        super.reset();
        primed = false;
    }

    public TriggerType getType() {
        return TriggerType.POSITION;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PositionTrigger other)) return false;
        if (pos != null && other.pos != null) {
            return pos.equals(other.pos);
        } else {
            return pos == null && other.pos == null;
        }
    }



}
