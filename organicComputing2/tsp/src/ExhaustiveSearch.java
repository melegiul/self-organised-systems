import java.util.ArrayList;


public class ExhaustiveSearch {
    private DistanceMatrix matrix;
    private ArrayList<Integer> shortestRoute;
    private int shortestDistance = Integer.MAX_VALUE;

    public ExhaustiveSearch(DistanceMatrix matrix) {
        this.matrix = matrix;
    }

    public ArrayList<Integer> getShortestRoute() {
        return shortestRoute;
    }

    public int getShortestDistance() {
        return shortestDistance;
    }

    public void calculateRoute(ArrayList<Integer> unvisited, ArrayList<Integer> currentRoute) {
        if(unvisited.isEmpty()) {
            int currentDistance = calculateDistance(currentRoute);
            if (currentDistance < this.shortestDistance) {
                this.shortestDistance = currentDistance;
                this.shortestRoute = new ArrayList<>(currentRoute);
            }
        } else {
            for(Integer i: unvisited) {
                ArrayList<Integer> nextUnvisited = new ArrayList<>(unvisited);
                ArrayList<Integer> nextCurrentRoute = new ArrayList<>(currentRoute);
                nextUnvisited.remove(i);
                nextCurrentRoute.add(i);
                calculateRoute(nextUnvisited, nextCurrentRoute);
            }
        }
    }

    private Integer calculateDistance(ArrayList<Integer> route) {
        Integer distance = Integer.valueOf(0);
        for(int i=1; i<matrix.getNumberOfCities(); i++) {
            distance = Integer.sum(distance, matrix.getDistance(route.get(i-1), route.get(i)));
        }

        return distance;
    }
}
