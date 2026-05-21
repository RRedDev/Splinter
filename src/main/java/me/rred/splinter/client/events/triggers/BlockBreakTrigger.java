package me.rred.splinter.client.events.triggers;

import net.minecraft.util.math.BlockPos;

public class BlockBreakTrigger extends Trigger{
    private BlockPos pos;

    public BlockBreakTrigger(TriggerType triggerType, BlockPos pos) {
        super(triggerType);
        this.pos = pos;
    }

    public BlockBreakTrigger(TriggerType triggerType) {
        super(triggerType);
        this.pos = null;
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
