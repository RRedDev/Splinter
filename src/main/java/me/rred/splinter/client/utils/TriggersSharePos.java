package me.rred.splinter.client.utils;

import me.rred.splinter.client.events.triggers.PositionTrigger;
import me.rred.splinter.client.events.triggers.Trigger;

public class TriggersSharePos {
    public static boolean check(Trigger a, Trigger b){
        if (a instanceof PositionTrigger pa && b instanceof PositionTrigger pb) {
                return pa.getPos() != null && pa.getPos().equals(pb.getPos());
            }
            return false;
        }
}
