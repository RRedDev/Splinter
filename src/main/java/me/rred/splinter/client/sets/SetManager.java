package me.rred.splinter.client.sets;

import me.rred.splinter.Splinter;

import java.util.ArrayList;
import java.util.List;

public class SetManager {
    private List<SplinterSet> sets = new ArrayList<>();
    private SplinterSet activeSet;

    public SetManager() {
         SplinterSet generalSet = new SplinterSet("general");
         sets.add(generalSet);
         activeSet = generalSet;
    }

    public void addTime(long ms) {
        Splinter.LOGGER.info("adding time to active set: {}", activeSet.getName());
        activeSet.addTime(ms);
    }

    public void removeTime(int idx) {
        activeSet.removeTime(idx);
    }

    public void createSet(String name) {
        if (sets.size() >= 8) return;
        SplinterSet newSet = new SplinterSet(name);
        sets.add(newSet);
    }

    public void deleteSet(int idx) {
        if (idx <= 0 || idx >= sets.size()) return; // 0 protects the general set
        if (activeSet == sets.get(idx)) activeSet = sets.get(0); // fall back to general
        sets.remove(idx);
    }

    public void setActiveSet(SplinterSet set) {
        activeSet = set;
    }

    public SplinterSet getActiveSet() {
        return activeSet;
    }

    public List<SplinterSet> getAllSets() {
        return sets;
    }

}
