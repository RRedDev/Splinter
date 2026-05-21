package me.rred.splinter.client;

import me.rred.splinter.Splinter;
import me.rred.splinter.client.route.Route;

public class SplinterStateMachine {
    public enum State {
        IDLE, // transitional state, !ACTIVE && !EDIT
        ACTIVE,
        EDIT
    }

    private State state = State.ACTIVE;
    private boolean inMap = false;

    public State getState() {
        return state;
    }

    public void setActive() {
        if (state == State.IDLE) {
            state = State.ACTIVE;
            Splinter.LOGGER.info("SSM: switched to ACTIVE");
            // begin listening for events
        }
    }

    public void setIdle() {
        if (state != State.IDLE) {
            state = State.IDLE;
            SplinterClient.timer.clear();
            Splinter.LOGGER.info("SSM: switched to IDLE");
        }
        // stop listening, clear highlights
    }

    public void setEdit() {
        if (state == State.ACTIVE) return; // can't start running while making changes
        if (state == State.IDLE) {
            state = State.EDIT;
            Splinter.LOGGER.info("SSM: switched to EDIT");
            // display set UI
        }
    }

    public boolean isActive() {
        return state == State.ACTIVE;
    }

    public void setInMap(boolean inMap) {
        this.inMap = inMap;
        if (!inMap) {
            SplinterClient.timer.clear();
            Route route = SplinterClient.setManager.getActiveSet().getRoute();
            route.getStartTrigger().reset();
            route.getEndTrigger().reset();
        }
    }

    public boolean isInMap() {
        return inMap;
    }








}
