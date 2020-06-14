package acs;

import java.util.ArrayList;
import java.util.List;

public class Tuple<T> {
    private Integer iteration;
    private T value;
    private List<Integer> shortestTour = new ArrayList<>();

    public Tuple(Integer iteration, T value, List<Integer> shortestTour) {
        this.iteration = iteration;
        this.value = value;
        this.shortestTour = shortestTour;
    }

    public Tuple(Integer iteration, T value) {
        this.iteration = iteration;
        this.value = value;
    }

    public Integer getIteration() {
        return iteration;
    }

    public T getValue() {
        return value;
    }

    public List<Integer> getShortestTour() {
        return shortestTour;
    }
}
