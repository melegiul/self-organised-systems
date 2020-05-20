package main;

import acs.AntColony;
import exhaustive.ExhaustiveSearch;
import generator.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class Main {

    private static void firstSheet() {
        int numberOfCities = 10;
        long seed;
        long [] elapsedTimes = new long[10];
        long elapsedSum = 0;
        for(seed=1; seed<=10; seed++) {
//            exhaustive.ExhaustiveSearch.shortestDistance = Integer.MAX_VALUE;
            Matrix matrix = new Matrix(numberOfCities);
            ExhaustiveSearch search = new ExhaustiveSearch(matrix);
            search.matrixInit(seed);
            matrix.printMatrix();
            ArrayList<Integer> unvisited = new ArrayList<>(numberOfCities);
            for (int i = 0; i < numberOfCities; i++) {
                unvisited.add(Integer.valueOf(i));
            }
            long startTime = System.nanoTime();
            search.calculateRoute(unvisited, new ArrayList<>(numberOfCities));
            elapsedTimes[(int)(seed-1)] = System.nanoTime() - startTime;
            elapsedSum += elapsedTimes[(int)(seed-1)];
            System.out.println("Shortest Distance: " + search.getShortestDistance());
            System.out.println("Shortest Route: " + search.getShortestRoute());
            System.out.println();
        }
        System.out.println("Average Elapsed Time in sec: " + elapsedSum/(1e9*10));
    }

    public static void main(String[] args) {
//        firstSheet();
        if (args.length != 9) {
            System.out.println("error: Invalid Arguments");
            return;
        }
        int numberCities = Integer.parseInt(args[0]);
        int seed = Integer.parseInt(args[1]);
        int iterations = Integer.parseInt(args[2]);
        // hyperparameter
        int numberAnts = Integer.parseInt(args[3]);
        // importance of exploration vs exploitation
        double q = Double.parseDouble(args[4]);
        // importance of pheromone vs distance
        double beta = Double.parseDouble(args[5]);
        // decay of pheromone (global update rule)
        double alpha = Double.parseDouble(args[6]);
        // decay of pheromone (local update rule)
        double rho = Double.parseDouble(args[7]);
        // initial pheromone per edge
        double tau = Double.parseDouble(args[8]);

        List<Integer> distanceResult = new ArrayList(10);
        List<List> tourResult = new ArrayList<List>(10);
        long[] elapsedTime = new long[10];
        long elapsedSum = 0;
        long acsSeed;
        long startTime;
        Matrix.initSeed(seed);
        Matrix<Integer> distanceMatrix = new Matrix<>(numberCities);
        distanceMatrix.matrixInit();
        distanceMatrix.printMatrix();
        for (acsSeed=1; acsSeed<=10; acsSeed++) {
            Matrix<Double> pheromoneMatrix = new Matrix<>(numberCities);
            startTime = System.nanoTime();
            AntColony acs = new AntColony(distanceMatrix, pheromoneMatrix, numberAnts, iterations, q, beta, alpha, rho, tau, acsSeed);
            acs.antColonySystem();
            elapsedTime[(int)(acsSeed-1)] = System.nanoTime() - startTime;
            elapsedSum += elapsedTime[(int)(acsSeed-1)];
            distanceResult.add(acs.getBestIterationDistance());
            tourResult.add(acs.getBestIterationTour());
            System.out.println("Best Iterations Distance: " + acs.getBestIterationDistance());
            System.out.println("Best Iterations tour: " + acs.getBestIterationTour());
            System.out.println();
        }
        TreeSet<Integer> resultSet = new TreeSet<>(distanceResult);
        Double expectedValue = AntColony.expectedValue(distanceResult,resultSet,10);
        int index = distanceResult.indexOf(resultSet.first());
        System.out.println("Average elapsed time in sec: " + elapsedSum/(1e9*10));
        System.out.println("Shortest Distance for all seeds: " + resultSet.first());
        System.out.println("Expected Value for all seeds: " + expectedValue);
        System.out.println("Shortest Tour for all seeds: " + tourResult.get(index));
        return;

    }
}
