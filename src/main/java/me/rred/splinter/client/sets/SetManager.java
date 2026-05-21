package me.rred.splinter.client.sets;

import me.rred.splinter.Splinter;
import me.rred.splinter.client.route.Route;

import java.util.ArrayList;
import java.util.List;

public class SetManager {
    private List<SplinterSet> sets = new ArrayList<>();
    private SplinterSet activeSet = null;
    private SplinterSet displayedSetA = null;
    private SplinterSet displayedSetB = null;


    public SetManager() {
         SplinterSet generalSet = new SplinterSet("general", true, new Route());
         sets.add(generalSet);
         activeSet = generalSet;
         displayedSetA = generalSet;
    }

    public void addTime(long ms) {
        Splinter.LOGGER.info("adding time to active set: {}", activeSet.getName());
        activeSet.addTime(ms);
    }

    public void removeTime(int idx) {
        activeSet.removeTime(idx);
    }

    public void createSet(String name) {
        if (sets.size() >= 20) return; // arbitrary cap of 8 for now
        SplinterSet newSet = new SplinterSet(name, false, new Route());
        sets.add(newSet);
    }

    public void deleteSet(SplinterSet set) {
        if (set.isGeneral()) return;
        sets.remove(set);
        if (activeSet == set) activeSet = sets.get(0); // fall back to general
        if (displayedSetA == set) displayedSetA = null;
        if (displayedSetB == set) displayedSetB = null;
    }

    public SplinterSet getActiveSet() {
        return activeSet;
    }

    public void setActiveSet(SplinterSet set) {
        activeSet = set;
    }

    public SplinterSet getDisplayedSetA() { return displayedSetA; };

    public void setDisplayedSetA(SplinterSet set) {
        // swap
        if (set == displayedSetB) {
            displayedSetB = displayedSetA;
        }
        displayedSetA = set;
    }
    public void clearDisplayedSetA() {
        displayedSetA = null;
    }


    public SplinterSet getDisplayedSetB() { return displayedSetB; };

    public void setDisplayedSetB(SplinterSet set) {
        // swap
        if (set == displayedSetA) {
            displayedSetA = displayedSetB;
        }
        displayedSetB = set;
    }

    public void clearDisplayedSetB() {
        displayedSetB = null;
    }

    public List<SplinterSet> getAllSets() {
        return sets;
    }

}
