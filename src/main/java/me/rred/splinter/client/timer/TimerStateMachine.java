package me.rred.splinter.client.timer;

import me.rred.splinter.Splinter;
import net.minecraft.block.Block;
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

    public BlockPos getStartPos() {
        return startPos;
    }

    public BlockPos getEndPos() {
        return endPos;
    }

    public void onClear() {
        currState = State.IDLE;
        startPos = null;
        endPos = null;
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
                    currState = State.RUNNING;
                    startPos = null;
                } else if (pos.equals(endPos)) {
                    Splinter.LOGGER.info("clear selection! (2block end broken)");
                    onClear();
                }
            }
            case RUNNING -> {
                if (pos.equals(endPos)) {
                    Splinter.LOGGER.info("clear timer! (2block end broken)");
                    onClear();
                }
            }

        }
    }
}
