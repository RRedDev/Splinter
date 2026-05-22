package me.rred.splinter.client.events.triggers;

public class MapTrigger extends Trigger {
    private int prevTick = 0;

    public MapTrigger(TriggerSlot triggerSlot) {
        super(triggerSlot);
    }

    public void mapTick(int tick) {
        triggered = (triggerSlot == TriggerSlot.START && prevTick == 0 && tick > 0
             || triggerSlot == TriggerSlot.END && prevTick > 0 && tick == 0);
        // if map timer starts, trigger start trigger.
        // if map timer ends, trigger end trigger
        prevTick = tick;
    }

    public TriggerType getType() {
        return TriggerType.MAP;
    }
}
