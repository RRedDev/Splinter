package me.rred.splinter.client.route;

import me.rred.splinter.client.events.triggers.MapTrigger;
import me.rred.splinter.client.events.triggers.Trigger;

public class Route {
    private Trigger startTrigger = new MapTrigger(Trigger.TriggerSlot.START);
    private Trigger endTrigger = new MapTrigger(Trigger.TriggerSlot.END);

    public Trigger getStartTrigger() {
        return startTrigger;
    }

    public Trigger getEndTrigger() {
        return endTrigger;
    }

    public void setStartTrigger(Trigger trigger) {
        startTrigger = trigger;
    }

    public void setEndTrigger(Trigger trigger) {
        endTrigger = trigger;
    }

}
