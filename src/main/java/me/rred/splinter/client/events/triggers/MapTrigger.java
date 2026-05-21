package me.rred.splinter.client.events.triggers;

public class MapTrigger extends Trigger {
    private int prevTick = 0;

    public MapTrigger(TriggerType triggerType) {
        super(triggerType);
    }

    public void mapTick(int tick) {
        triggered = (triggerType == TriggerType.START && prevTick == 0 && tick > 0
             || triggerType == TriggerType.END && prevTick > 0 && tick == 0);
        // if map timer starts, trigger start trigger.
        // if map timer ends, trigger end trigger
        prevTick = tick;
    }
}
