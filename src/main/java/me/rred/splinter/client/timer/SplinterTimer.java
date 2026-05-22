package me.rred.splinter.client.timer;

import me.rred.splinter.client.SplinterClient;
import org.lwjgl.system.CallbackI;

public class SplinterTimer {
    private enum State {IDLE, RUNNING, STOPPED}
    private State state = State.IDLE;

    private int activeTicks = 0;
    private int endTicks = 0;
    private long leastTickTime;

    public void tick() {
        if (state == State.RUNNING) {
            activeTicks++;
            leastTickTime = System.currentTimeMillis();
        }
    }

    public void start() {
        activeTicks = 0;
        endTicks = 0;
        leastTickTime = System.currentTimeMillis();
        state = State.RUNNING;
    }

    public void stop() {
        if (state == State.RUNNING) {
            endTicks = activeTicks;
            state = State.STOPPED;
        }
    }

    public void clear() {
        activeTicks = 0;
        endTicks = 0;
        leastTickTime = 0;
        state = State.IDLE;
    }

    public long fetchElapsedTime() {
        if (state == State.IDLE) {
            return 0;
        }
        if (isRunning()) {
            long tickMs = activeTicks * 50L;
            long interpolation = Math.min(50, System.currentTimeMillis() - leastTickTime);
            return tickMs + interpolation;
        }
        return endTicks * 50L;
    }

    public boolean isRunning() { return state == State.RUNNING; }
}
