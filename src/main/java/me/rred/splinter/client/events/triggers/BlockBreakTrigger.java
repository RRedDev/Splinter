package me.rred.splinter.client.events.triggers;

import me.rred.splinter.Splinter;
import net.minecraft.util.math.BlockPos;

public class BlockBreakTrigger extends Trigger{
    private BlockPos pos;

    public BlockBreakTrigger(TriggerSlot triggerSlot, BlockPos pos) {
        super(triggerSlot);
        this.pos = pos;
    }

    public TriggerType getType() {
        return TriggerType.BLOCK_BREAK;
    }

    public boolean matches(BlockPos broken) {
        return pos != null && pos.equals(broken);
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }
}
