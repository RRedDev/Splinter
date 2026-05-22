package me.rred.splinter.client.events.triggers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class PositionTrigger extends Trigger {
    private BlockPos pos;

    public PositionTrigger(TriggerSlot triggerSlot, BlockPos pos) {
        super(triggerSlot);
        this.pos = pos;
    }

    @Override
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || pos == null) return;
        if (client.player.getBlockPos().equals(pos)) {
            triggered = true;
        }
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


}
