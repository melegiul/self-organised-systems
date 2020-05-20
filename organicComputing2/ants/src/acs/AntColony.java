package acs;

import generator.Matrix;

import java.util.*;

public class AntColony {
    private final Matrix<Integer> distanceMatrix;
    private Matrix<Double> pheromoneMatrix;
    // each array index represents unvisited/tour for a single ant
    private List<Integer> [] unvisited;
    private List<Integer> [] tour;

    // shortest distance of all iterations
    private Integer bestIterationDistance = Integer.MAX_VALUE;
    // shortest tour of all iterations
    private List<Integer> bestIterationTour;

    private final int numberAnts;
    private long acsSeed;
    private Random qGenerator;
    private final int iterations;
    // importance of exploration vs exploitation
    private final double q0;
    // importance of pheromone vs distance
    private final double beta;
    // decay of pheromone (global update rule)
    private final double alpha;
    // decay of pheromone (local update rule)
    private final double rho;
    // initial pheromone per edge
    private final double tau;

    public AntColony(Matrix<Integer> distanceMatrix, Matrix<Double> pheromoneMatrix, int numberAnts, int iterations, double q0, double beta, double alpha, double rho, double tau, long acsSeed) {
        this.distanceMatrix = distanceMatrix;
        this.pheromoneMatrix = pheromoneMatrix;
        this.numberAnts = numberAnts;
//        this.seed = seed;
        this.iterations = iterations;
        this.q0 = q0;
        this.beta = beta;
        this.alpha = alpha;
        this.rho = rho;
        this.tau = tau;
        this.acsSeed = acsSeed;
        this.qGenerator = new Random(acsSeed);
        this.unvisited = new ArrayList[numberAnts];
        this.tour = new ArrayList[numberAnts];
        this.bestIterationTour = new ArrayList<>(distanceMatrix.getNumberOfCities());
    }

    /**
     * for each iteration all ants choose a complete tour
     */
    public void antColonySystem() {
        acsInit();
        for (int i=0; i<iterations; i++) {
            resetAnts(i);
            chooseTour(i);
        }
    }

    public Integer getBestIterationDistance() {
        return bestIterationDistance;
    }

    public List<Integer> getBestIterationTour() {
        return bestIterationTour;
    }

    /**
     *
     * @param bestDistance list with possible duplicates, number of
     *                     occurrences yield the probability of distinct element
     * @param distanceSet representation of the list as a set
     *                    with no duplicates
     * @return expected value
     */
    public static Double expectedValue(List bestDistance, Set distanceSet, int total) {
        Iterator iter = distanceSet.iterator();
        Double expectedValue = 0.0;
        while (iter.hasNext()) {
            Integer currentDistance = (Integer)iter.next();
            // for each distinct path-distance number of occurrences is counted
            Double frequency = Double.valueOf(Collections.frequency(bestDistance,currentDistance));
            // formula for expected value
            expectedValue += ((frequency/total)*currentDistance);
        }
        return expectedValue;
    }

    /**
     * set initial pheromone
     */
    private void acsInit() {
        for (int i = 0; i < pheromoneMatrix.getNumberOfCities(); i++) {
            for (int j = i + 1; j < pheromoneMatrix.getNumberOfCities(); j++) {
                pheromoneMatrix.setMatrixElement(i, j, tau);
                pheromoneMatrix.setMatrixElement(j, i, tau);
            }
        }
    }

    /**
     * resets the list of tour to start and the list of unvisited
     * to all other cities
     * @param iter for random selection of start (depending on iteration)
     */
    private void resetAnts(int iter) {
        for (int k=0; k<numberAnts; k++) {
            tour[k] = new ArrayList<Integer>(distanceMatrix.getNumberOfCities());
            unvisited[k] = new ArrayList<Integer>(distanceMatrix.getNumberOfCities());
            for (int i=0; i<distanceMatrix.getNumberOfCities(); i++) {
                unvisited[k].add(i);
            }
            Integer start = getStart(k,iter);
            unvisited[k].remove(start);
            tour[k].add(start);
        }
    }

    /**
     * pseudo-random selection of start point
     * @param ant start selection depends on ant
     * @param iter start selection depends on current iteration
     * @return the start city
     */
    private Integer getStart(int ant, int iter) {
        return (ant+iter) % distanceMatrix.getNumberOfCities();
    }

    /**
     * for each iteration all ants choose a complete tour
     * after traveling a single edge local updates are executed
     * after traveling the complete tour global updates are executed
     * @param currentIteration for pseudo-random start selection
     */
    private void chooseTour(int currentIteration) {
        for (int i=1; i<distanceMatrix.getNumberOfCities(); i++) {
            // each ant travels to next city
            for (int k=0; k<numberAnts; k++) {
                Integer nextCity = chooseCity(k);
                unvisited[k].remove(nextCity);
                tour[k].add(nextCity);
            }
            localUpdate();
        }
        // ants move back to start
        for (int k=0; k<numberAnts; k++) {
            Integer start = tour[k].get(0);
            tour[k].add(start);
        }
        localUpdate();
        globalUpdate();
    }

    /**
     * local updating rule using (5)
     */
    private void localUpdate() {
        for (int k=0; k<numberAnts; k++) {
            int size = tour[k].size();
            int currentCity = tour[k].get(size-1);
            int predecessorCity = tour[k].get(size-2);
            // pheromone of a single edge
            Double currentPheromone = pheromoneMatrix.getValue(currentCity,predecessorCity);
            currentPheromone *= (1-rho);
            currentPheromone += (rho*tau);
            pheromoneMatrix.setMatrixElement(currentCity,predecessorCity,currentPheromone);
            pheromoneMatrix.setMatrixElement(predecessorCity,currentCity,currentPheromone);
        }
    }

    /**
     * global updating rule using (5)
     * only the ant with best tour according distance sets pheromone
     */
    private void globalUpdate() {
        int[] sum = new int[numberAnts];
        Integer bestDistance = Integer.MAX_VALUE;
        Integer bestAnt = -1;
        // this loop determines the shortest distance of all ants
        for(int k=0; k<numberAnts; k++) {
            sum[k] = 0;
            for(int i=0; i<distanceMatrix.getNumberOfCities(); i++){
                sum[k] += distanceMatrix.getValue(tour[k].get(i),tour[k].get(i+1));
            }
            if (sum[k]<bestDistance) {
                bestAnt = k;
                bestDistance = sum[k];
            }
        }
        // if current iteration has shortest distance
        if (bestDistance < bestIterationDistance) {
            bestIterationDistance = bestDistance;
            bestIterationTour = new ArrayList<>(tour[bestAnt]);
        }
        // evaporate pheromone for each edge
        for(int i=0; i<pheromoneMatrix.getNumberOfCities(); i++){
            for(int j=i+1; j<pheromoneMatrix.getNumberOfCities(); j++){
                Double currentPheromone = pheromoneMatrix.getValue(i,j);
                currentPheromone *= (1-alpha);
                pheromoneMatrix.setMatrixElement(i,j,currentPheromone);
                pheromoneMatrix.setMatrixElement(j,i,currentPheromone);
            }
        }
        // iterating over all edges of best tour to add pheromone
        for(int i=1; i<tour[bestAnt].size(); i++) {
            Integer preCity = tour[bestAnt].get(i-1);
            Integer sucCity = tour[bestAnt].get(i);
            Double currentPheromone = pheromoneMatrix.getValue(preCity,sucCity);
            currentPheromone += (alpha/bestDistance);
            pheromoneMatrix.setMatrixElement(preCity,sucCity,currentPheromone);
            pheromoneMatrix.setMatrixElement(sucCity,preCity,currentPheromone);
        }
    }

    /**
     * ACS pseudo-random-proportional rule using (3)
     * chooses randomly between exploitation and exploration
     * @param ant index for accessing unvisted cities list and the traveled cities list
     * @return choosen city
     */
    private Integer chooseCity(int ant) {
        Double q = qGenerator.nextDouble();
        if (q <= q0) {
            return exploitation(ant);
        } else {
            return exploration(ant);
        }
    }

    /**
     * ACS pseudo-random-proportional rule using (3)
     * @param ant index for accessing the ants tour
     * @return selects city, which edge has most pheromone and shortest distance
     */
    private Integer exploitation(int ant) {
        Integer currentCity = tour[ant].get(tour[ant].size()-1);
        Double maxExploitation = 0.0;
        Integer maxCity = -1;
        // loop over all unvisited cities
        for (Integer nextCity: unvisited[ant]) {
            Double pheromon = pheromoneMatrix.getValue(currentCity,nextCity);
            Integer distance = distanceMatrix.getValue(currentCity,nextCity);
            pheromon /= distance;
            pheromon = Math.pow(pheromon,beta);
            if (pheromon > maxExploitation) {
                maxExploitation = pheromon;
                maxCity = nextCity;
            }
        }
        return maxCity;
    }

    /**
     * Ant System Random-Proportional Rule using (1)
     * @param ant index for accessing the ants tour
     * @return next city
     */
    private Integer exploration(int ant) {
        Double dice;
        Double diceBar = 0.0;
        Integer result = 0;
        Integer currentCity = tour[ant].get(tour[ant].size()-1);
        List<Double> probabilities = new ArrayList<>(Collections.nCopies(distanceMatrix.getNumberOfCities(),0.0));
        Double divisor = 0.0;
        // loop for calculating divisor
        for (Integer nextCity: unvisited[ant]) {
            Double pheromon = pheromoneMatrix.getValue(currentCity,nextCity);
            Integer distance = distanceMatrix.getValue(currentCity,nextCity);
            Double probability = pheromon / distance;
            probability = Math.pow(probability,beta);
            divisor += probability;
            probabilities.set((int)nextCity,probability);
        }
        // calculating the probabilities for each city
        for (Integer nextCity: unvisited[ant]) {
            Double currentValue = probabilities.get(nextCity) / divisor;
            probabilities.set(nextCity,currentValue);
        }
        // dice gets value between 0 and 1
        dice = qGenerator.nextDouble();
        // loop adds the probabilities of the cities until dice-value is exceeded
        while(diceBar < dice) {
            diceBar += probabilities.get(result);
            result++;
        }
        // returns the city, which probability exceeded the dice
        return result-1;
    }
}
