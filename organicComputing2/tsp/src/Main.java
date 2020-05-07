import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int numberOfCities = 10;
        long seed;
        long [] elapsedTimes = new long[10];
        long elapsedSum = 0;
        for(seed=1; seed<=10; seed++) {
//            ExhaustiveSearch.shortestDistance = Integer.MAX_VALUE;
            DistanceMatrix matrix = new DistanceMatrix(numberOfCities);
            matrix.matrixInit(seed);
            matrix.printMatrix();
            ArrayList<Integer> unvisited = new ArrayList<>(numberOfCities);
            for (int i = 0; i < numberOfCities; i++) {
                unvisited.add(Integer.valueOf(i));
            }
            long startTime = System.nanoTime();
            ExhaustiveSearch search = new ExhaustiveSearch(matrix);
            search.calculateRoute(unvisited, new ArrayList<>(numberOfCities));
            elapsedTimes[(int)(seed-1)] = System.nanoTime() - startTime;
            System.out.println("Shortest Distance: " + search.getShortestDistance());
            System.out.println("Shortest Route: " + search.getShortestRoute());
            System.out.println();
        }
        for (int i=0; i<10; i++) {
            elapsedSum += elapsedTimes[i];
        }
        System.out.println("Average Elapsed Time in sec: " + elapsedSum/(1e9*10));
    }
}
