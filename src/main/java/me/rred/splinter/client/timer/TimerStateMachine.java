package me.rred.splinter.client.timer;

import me.rred.splinter.Splinter;
import net.minecraft.util.math.BlockPos;

public class TimerStateMachine {
    private enum State {
        IDLE,
        ONE_SELECTED,
        TWO_SELECTED,
        RUNNING
    }

    private State currState = State.IDLE;
    private BlockPos startPos = null;
    private BlockPos endPos = null;

    private SplinterTimer timer = new SplinterTimer();

    public BlockPos getStartPos() {
        return startPos;
    }

    public BlockPos getEndPos() {
        return endPos;
    }

    public void logTime() {
        Splinter.LOGGER.info("time: {}", timer.fetchElapsedTime());
    }

    public long getElapsedMs() {
        return timer.fetchElapsedTime();
    }

    public boolean isActive() {
        return (currState == State.RUNNING);
    }

    public void onClear() {
        currState = State.IDLE;
        startPos = null;
        endPos = null;
        timer.clear();
    }

    public void onStop() {
        currState = State.IDLE;
        startPos = null;
        endPos = null;
        timer.stop();
    }

    public void onBlockSelected(BlockPos pos) {
        switch (currState) {
            case IDLE -> {
                currState = State.ONE_SELECTED;
                startPos = pos;
            }
            case ONE_SELECTED -> {
                if (!pos.equals(startPos)) {
                    currState = State.TWO_SELECTED;
                    endPos = pos;
                }
            }
            case TWO_SELECTED -> {
                if (!(pos.equals(startPos) || pos.equals(endPos))) {
                    onClear();
                    currState = State.ONE_SELECTED;
                    startPos = pos;
                }
            }
            case RUNNING -> {} // if running, don't accept selections
        }
    }

    public void onBlockBroken(BlockPos pos) {
        switch (currState) {
            case IDLE -> {}
            case ONE_SELECTED -> {
                if (pos.equals(startPos)) {
                    Splinter.LOGGER.info("clear selection! (1block start broken)");
                    onClear();
                }
            }
            case TWO_SELECTED -> {
                if (pos.equals(startPos)) {
                    Splinter.LOGGER.info("start timer! (2block start broken)");
                    timer.start();
                    currState = State.RUNNING;
                    startPos = null;
                } else if (pos.equals(endPos)) {
                    Splinter.LOGGER.info("clear selection! (2block end broken)");
                    onClear();
                }
            }
            case RUNNING -> {
                if (pos.equals(endPos)) {
                    Splinter.LOGGER.info("stop timer! (2block end broken)");
                    onStop();
                }
            }
        }
    }

    public void toggleTimer() {
        switch (currState) {
            case IDLE -> {
                timer.start();
                currState = State.RUNNING;
            }
            case ONE_SELECTED, TWO_SELECTED -> {
                startPos = null;
                endPos = null;
                timer.start();
                currState = State.RUNNING;
            }
            case RUNNING -> {
                onStop();
            }
        }
    }
}
