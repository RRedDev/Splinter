package me.rred.splinter.client.timer;

import org.lwjgl.system.CallbackI;

public class SplinterTimer {
    private long startTime = -1;
    private long endTime = -1;
    private boolean running = false;

    public void start() {
        startTime = System.currentTimeMillis();
        endTime = -1;
        running = true;
    }

    public void stop() {
        if (running) {
            endTime = System.currentTimeMillis();
            running = false;
        }
    }

    public void clear() {
        startTime = -1;
        endTime = -1;
        running = false;
    }

    public long fetchElapsedTime() {
        if (startTime == -1) {
            return 0;
        }
        if (running) {
            return System.currentTimeMillis() - startTime;
        }
        return endTime - startTime;
    }

    public boolean isRunning() {
        return running;
    }
}
