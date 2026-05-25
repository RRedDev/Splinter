package me.rred.splinter.client.sets;

import me.rred.splinter.client.routing.Route;

import java.util.ArrayList;
import java.util.List;

public class SplinterSet {
    private String name;
    private List<Long> times = new ArrayList<>(); // for now, data will just be non-persistent
    private final boolean isGeneral;
    private Route route;

    public SplinterSet(String name, boolean isGeneral, Route route) {
        this.name = name;
        this.isGeneral = isGeneral;
        this.route = route;
    }

    public void addTime(long ms) {
        times.add(ms);
    }

    public void removeTime(int idx) {
        if (idx < 0 || idx > times.size() - 1) {
            return;
        }
        times.remove((int) idx);
    }

    public List<Long> getTimes() {
        return times;
    }

    public long getAverage() {
        return (long) times.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    public long getStdDev() {
        if (times.size() < 2) return 0;
        double avg = getAverage();
        double variance = times.stream()
                .mapToDouble(t -> Math.pow(t - avg, 2))
                .average()
                .orElse(0);
        return (long) Math.sqrt(variance);
    }

    public long getBest() {
        return (long) times.stream().mapToLong(Long::longValue).min().orElse(0);
    }

    public String getName() {
        return name;
    }

    public void renameSet(String newName) {
        this.name = newName;
    }

    public void clearSet() {
        times.clear();
    }

    public boolean isEmpty() {
        return times.isEmpty();
    }

    public boolean isGeneral() {
        return isGeneral;
    }

    public Route getRoute() {
        return route;
    }

}
