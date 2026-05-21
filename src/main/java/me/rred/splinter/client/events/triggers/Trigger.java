package me.rred.splinter.client.events.triggers;

public abstract class Trigger {
    public enum TriggerType { START, END }

    protected boolean triggered = false; // state
    protected TriggerType triggerType; // config type, start or end trigger

    public Trigger(TriggerType triggerType) {
        this.triggerType = triggerType;
    }
    // called by RouteHandler each tick for poll-based events;
    public void tick() {}

    // called by mixins for push-based events
    public void onFired() {
        triggered = true;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void reset() {
        triggered = false;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }
}
