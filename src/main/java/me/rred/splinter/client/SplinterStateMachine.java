package me.rred.splinter.client;

import me.rred.splinter.Splinter;
import me.rred.splinter.client.route.Route;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class SplinterStateMachine {
    public enum State {
        IDLE, // transitional state, !ACTIVE && !EDIT
        ACTIVE,
        EDIT
    }

    private State state = State.ACTIVE;
    private boolean inMap = false;
    private EditSession editSession = null;

    public State getState() {
        return state;
    }

    public void setActive() {
        if (state == State.IDLE) {
            state = State.ACTIVE;
            SplinterClient.routeHandler.resetFired();
            // begin listening for events
        }
        // eventually allow active -> edit possibly
    }

    public void setIdle() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        if (state != State.IDLE) {
            if (state == State.EDIT && editSession.hasChanges()) {
                client.player.sendMessage( new LiteralText("confirm or cancel changes in GUI")
                        .styled(s -> s.withColor(Formatting.YELLOW)), false);
                return;
            }
            state = State.IDLE;
            SplinterClient.timer.clear();
            editSession = null;
        }
        // stop listening, clear highlights
    }

    public void setEdit() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        if (state == State.ACTIVE) {
            client.player.sendMessage( new LiteralText("can't edit in active mode. see sets GUI")
                    .styled(s -> s.withColor(Formatting.RED)), false);
            return; // can't start running while making changes
        }
        if (state == State.IDLE) {
            state = State.EDIT;
            editSession = new EditSession(SplinterClient.setManager.getActiveSet());
            // display set UI
        }
    }

    public void refreshEditSession() {
        if (state == State.EDIT) {
            editSession = new EditSession(SplinterClient.setManager.getActiveSet());
        }
    }

    public EditSession getEditSession() {
        return editSession;
    }

    public boolean isActive() {
        return state == State.ACTIVE;
    }

    public boolean isIdle() {return state == State.IDLE;}

    public boolean isEditing() {return state == State.EDIT;}

    public boolean isEditingWithChanges() { return state == State.EDIT && editSession.hasChanges(); }

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
