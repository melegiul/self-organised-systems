package acs;

import generator.Matrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class AntColony<T extends Comparable<T>> implements Comparator<T[]>{
    private final Matrix<Integer> distanceMatrix;
    private Matrix<Double> pheromoneMatrix;
    // each array index represents unvisited/tour for a single ant
    private List<Integer> [] unvisited;
    private List<Integer> [] tour;

    // shortest distance of all iterations
    private Integer bestIterationDistance = Integer.MAX_VALUE;
    // shortest tour of all iterations
    private List<Integer> bestIterationTour;

    // a tuple comprises the iteration and the corresponding value
    // shortest distance for each iteration
    private List<Tuple> shortestDistances;
    // position emergence for each iteration
    private List<Tuple> positionEmergence;
    // route emergence for each iteration
    private List<Tuple> routeEmergence;
    // pheromone emergence for each iteration
    private List<Tuple> pheromoneEmergence;

    // files and writers for logging entropy
    List<Integer> positionCounter;
    File positionFile;
    File routeFile;
    File pheromoneFile;
    FileWriter positionWriter;
    FileWriter routeWriter;
    FileWriter pheromoneWriter;

//    File posEmergFile;
//    File routEmergFile;
//    File pheroEmergFile;
//    FileWriter posEmergWriter;
//    FileWriter routEmergWriter;
//    FileWriter pheroEmergWriter;

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
        this.positionCounter = new ArrayList<>(Collections.nCopies(distanceMatrix.getNumberOfCities(), 0));
        this.shortestDistances = new ArrayList<>(iterations);
        this.positionEmergence = new ArrayList<>(iterations);
        this.routeEmergence = new ArrayList<>(iterations);
        this.pheromoneEmergence = new ArrayList<>(iterations);
    }

    /**
     * for each iteration all ants choose a complete tour
     */
    public void antColonySystem() {
        acsInit();
        try{
            for (int i=0; i<iterations; i++) {
                resetAnts(i);
                chooseTour(i);
                rotateTour(0);
                computePositionEntropy();
                computeRouteEntropy();
                computePheromoneEntropy(4);
                if (i>=1) {
                    this.positionWriter.flush();
                    this.routeWriter.flush();
                    this.pheromoneWriter.flush();
                    computeEmergence(i, new String[]{"position", "route", "pheromone"});
//                    computeEmergence(i, "route");
//                    computeEmergence(i,"pheromone");
//                    System.out.println();
                }
            }
            this.routeWriter.close();
            this.positionWriter.close();
            this.positionWriter.close();
        } catch (IOException e) {
            System.out.println("Error at closing the writer or while choosing tour");
        }
    }

    public Integer getBestIterationDistance() {
        return bestIterationDistance;
    }

    public List<Integer> getBestIterationTour() {
        return bestIterationTour;
    }

    public List<Tuple> getShortestDistances() { return shortestDistances; }

    public List<Tuple> getPositionEmergence() {
        return positionEmergence;
    }

    public List<Tuple> getRouteEmergence() {
        return routeEmergence;
    }

    public List<Tuple> getPheromoneEmergence() {
        return pheromoneEmergence;
    }

    private void rotateTour(Integer normCity) {
        for(int k=0; k<numberAnts; k++){
            LinkedList<Integer> current = new LinkedList(tour[k]);
            while(current.getFirst() != normCity){
                Integer puffer = current.pollLast();
                current.addFirst(puffer);
            }
            tour[k] = new ArrayList<>(current);
        }
    }

    private void computePositionEntropy(){
        int total = positionCounter.stream().mapToInt(Integer::intValue).sum();
        entropyFormula(positionCounter,total,"position");
//        Double positionEntropy = 0.0;
//        for(Integer i: positionCounter){
//            if(i!=0) {
//                Double x = Double.valueOf(i) / total;
//                x *= Math.log10(x) / Math.log10(2.0);
//                positionEntropy += x;
//            }
//        }
//        positionEntropy *= -1;
//        try{
//            positionWriter.write(String.valueOf(positionEntropy) + "\n");
//        } catch(IOException e) {
//            System.out.println("Error during writing position entropy");
//        }
    }

    private void computeRouteEntropy(){
        TreeSet<Integer[]> routeSet = new TreeSet<Integer[]>(new Comparator<Integer[]>() {
            @Override
            public int compare(Integer[] arr1, Integer[] arr2) {
                if(arr1.length <= arr2.length){
                    if (arr1.length == arr2.length) {
                        for (int i=0; i<arr1.length; i++) {
                            if(arr1[i] < arr2[i]) {
                                return -1;
                            } if(arr1[i] == arr2[i]) {
                                continue;
                            } else {
                                return 1;
                            }
                        }
                        return 0;
                    } else {
                        return -1;
                    }
                } else {
                    return 1;
                }
            }
        });
//        TreeSet<Integer[]> routeSet = new TreeSet<>();
        ArrayList<Integer[]> routeList = new ArrayList<>();
        for(int k=0; k<numberAnts; k++){
            Integer[] array = new Integer[tour[k].size()];
            array = tour[k].toArray(array);
            routeSet.add(array);
            routeList.add(array);
        }
        int numberDistinctRoutes = routeSet.size();
        int j = 0;
        List<Integer> routeCounter = new ArrayList<>(Collections.nCopies(routeSet.size(), 0));
        for(Integer[] route: routeSet){
            for(int i=0; i<routeList.size(); i++) {
                if(compare((T[])route,(T[])routeList.get(i)) == 0) {
                    Integer x = routeCounter.get(j);
                    x++;
                    routeCounter.set(j, x);
                }
            }
            j++;
        }
        entropyFormula(routeCounter,numberAnts,"route");
    }

    private void computePheromoneEntropy(int granularity) {
        int numberCities = pheromoneMatrix.getNumberOfCities();
        List<Integer> counter = new ArrayList<>(Collections.nCopies(granularity,0));
        Double maxPheromone = pheromoneMatrix.getMax();
        Double minPheromone = pheromoneMatrix.getMin();
        Double pheromoneDif = maxPheromone-minPheromone;
        Double[] quantification = new Double[granularity];
        for (int i=0; i<granularity; i++) {
            if (i==0)
                quantification[i] = minPheromone + (pheromoneDif/granularity);
            else if (i==granularity-1)
                quantification[i] = maxPheromone;
            else
                quantification[i] = quantification[i-1] + (pheromoneDif/granularity);
        }
        for (int i=0; i<numberCities; i++) {
            for (int j=i+1; j<numberCities; j++) {
                int k=0;
                Double pheromone = pheromoneMatrix.getValue(i,j);
                while(pheromone > quantification[k]){
                    k++;
                }
                Integer element = counter.get(k);
                element++;
                counter.set(k,element);
            }
        }
        int size = 0;
        for (int i=0; i<numberCities; i++){
            size+=i;
        }
        entropyFormula(counter,size, "pheromone");
    }

    private void entropyFormula(List<Integer> p, int size, String attribute){
        Double entropy = 0.0;
        for(Integer i: p){
            if(i!=0) {
                Double x = Double.valueOf(i) / size;
                x *= Math.log10(x) / Math.log10(2);
                entropy += x;
            }
        }
        entropy *= -1;
        try{
            switch (attribute) {
                case "position":
                    positionWriter.write(String.valueOf(entropy) + "\n");
                    break;
                case "route":
                    routeWriter.write(String.valueOf(entropy) + "\n");
                    break;
                case "pheromone":
                    pheromoneWriter.write(String.valueOf(entropy) + "\n");
                    break;
                default:
                    throw new IOException();
            }
        } catch(IOException e) {
            System.out.println("Error during writing entropy to log file");
        }
    }

    private void computeEmergence(int iteration, String[] attributeList){
        for(int i=0; i<attributeList.length; i++) {
            String attribute = attributeList[i];
            String path = String.format("/home/giuliano/oc2-mango/organicComputing2" +
                    "/logs/%s/%s-1-%d.log", attribute, attribute, this.acsSeed);
            try (Stream<String> lines = Files.lines(Paths.get(path))) {
                Object[] array;
                Double preEntropy;
                Double lastEntropy;
                Double emergence;
                if (iteration>=9) {
                    array = lines.skip(iteration - 9).toArray();
                    preEntropy = Double.valueOf((String) array[0]);
                    lastEntropy = Double.valueOf((String) array[9]);
                    emergence = preEntropy - lastEntropy;
                } else {
                    array = lines.toArray();
                    preEntropy = Double.valueOf((String) array[iteration-1]);
                    lastEntropy = Double.valueOf((String) array[iteration]);
                    emergence = preEntropy - lastEntropy;
                }
                switch (attribute){
                    case "position":
                        positionEmergence.add(new Tuple(iteration, emergence));
                        break;
                    case "route":
                        routeEmergence.add(new Tuple(iteration, emergence));
                        break;
                    case "pheromone":
                        pheromoneEmergence.add(new Tuple(iteration, emergence));
                        break;
                    default:
                        throw new IOException();
                }
                if ((iteration+1)%10==0) {
                    String output = String.format("%s-Emergence: ", attribute);
                    System.out.println(output + emergence);
                }
            } catch (IOException e) {
                System.out.println("Error while emergence computation");
            }
        }
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

    public static List<Double> averageValue(List<List<Tuple>> experimentList, String label){
        int experimentNum = experimentList.size();
        int iterationNum = experimentList.get(0).size();
        // average after every tenth iteration
        List<Double> averageValues = new ArrayList<>(Collections.nCopies(iterationNum,0.0));
        // an experiment comprises a list of tuples
        // each tuple represents the shortest distance until a specific iteration
        for(List<Tuple> tupleList: experimentList) {
            // sum of all values from each experiment
            for(int i=0; i<iterationNum; i++){
                Double oldValue = averageValues.get(i);
                if(tupleList.get(i).getValue() instanceof Integer) {
                    averageValues.set(i, oldValue + (Integer)tupleList.get(i).getValue());
                } else {
                    averageValues.set(i, oldValue + (Double)tupleList.get(i).getValue());
                }
            }
        }
        // each entry divided by number of experiments
        for(int i=0; i<iterationNum; i++){
            averageValues.set(i,averageValues.get(i)/experimentNum);
            System.out.println(averageValues.get(i));
        }
        writeValues(averageValues, label);
        return averageValues;
    }

    /**
     * set initial pheromone
     */
    private void acsInit() {
        try{
            String positionPath = String.format(System.getProperty("user.dir") +
                    "/logs/position/position-1-%d.log", this.acsSeed);
            String routePath = String.format(System.getProperty("user.dir") +
                    "/logs/route/route-1-%d.log", this.acsSeed);
            String pheromonePath = String.format(System.getProperty("user.dir") +
                    "/logs/pheromone/pheromone-1-%d.log", this.acsSeed);
            this.positionFile = new File(positionPath);
            this.routeFile = new File(routePath);
            this.pheromoneFile = new File(pheromonePath);
            this.positionWriter = new FileWriter(this.positionFile);
            this.routeWriter = new FileWriter(this.routeFile);
            this.pheromoneWriter = new FileWriter(this.pheromoneFile);
        } catch (Exception e) {
            System.out.printf("Error while creating file with seed %d\n", this.acsSeed);
        }
        for (int i = 0; i < pheromoneMatrix.getNumberOfCities(); i++) {
            for (int j = i + 1; j < pheromoneMatrix.getNumberOfCities(); j++) {
                pheromoneMatrix.setMatrixElement(i, j, tau);
                pheromoneMatrix.setMatrixElement(j, i, tau);
            }
        }
        // set emergence value for first 9 iterations to zero
        for(int i=0; i<1; i++){
            Tuple<Double> tuple = new Tuple<>(i,0.0);
            positionEmergence.add(tuple);
            routeEmergence.add(tuple);
            pheromoneEmergence.add(tuple);
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
        Integer startPosition = (ant+iter) % distanceMatrix.getNumberOfCities();
//        positionCounter.set(startPosition,positionCounter.get(startPosition)+1);
//        try {
//            this.positionWriter.write(String.valueOf(startPosition) + "\n");
//        } catch(IOException e) {
//            System.out.println("Error while writing start position");
//        }
        return startPosition;
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
//                this.positionWriter.write(String.valueOf(nextCity) + "\n");
//                positionCounter.set(nextCity,positionCounter.get(nextCity)+1);
            }
            localUpdate();
        }
        // ants move back to start
//        for (int k=0; k<numberAnts; k++) {
//            Integer start = tour[k].get(0);
//            tour[k].add(start);
//        }
//        localUpdate();
        globalUpdate(currentIteration);
    }

    /**
     * local updating rule using (5)
     */
    private void localUpdate() {
        for (int k=0; k<numberAnts; k++) {
            int tourLength = tour[k].size();
            int nextCity;
            int predecessorCity;
            nextCity = tour[k].get(tourLength - 1);
            predecessorCity = tour[k].get(tourLength - 2);
            // pheromone of a single edge
            Double currentPheromone = pheromoneMatrix.getValue(nextCity,predecessorCity);
            // evaporate pheromone
            currentPheromone *= (1-rho);
            // add pheromone
            currentPheromone += (rho*tau);
            pheromoneMatrix.setMatrixElement(nextCity,predecessorCity,currentPheromone);
            pheromoneMatrix.setMatrixElement(predecessorCity,nextCity,currentPheromone);
            // if all cities visited go back to start and add pheromone
            // executes only at the end of an iteration
            if(tourLength == this.distanceMatrix.getNumberOfCities()) {
                nextCity = tour[k].get(0);
                predecessorCity = tour[k].get(tourLength - 1);
                // update position array after each iteration
                // is the last visited city before moving to start
                positionCounter.set(predecessorCity,positionCounter.get(predecessorCity)+1);
                // pheromone of a single edge
                currentPheromone = pheromoneMatrix.getValue(nextCity,predecessorCity);
                // evaporate pheromone
                currentPheromone *= (1-rho);
                // add pheromone
                currentPheromone += (rho*tau);
                pheromoneMatrix.setMatrixElement(nextCity,predecessorCity,currentPheromone);
                pheromoneMatrix.setMatrixElement(predecessorCity,nextCity,currentPheromone);
            }
        }
    }

    /**
     * global updating rule using (5)
     * only the ant with best tour according distance sets pheromone
     */
    private void globalUpdate(int currentIteration) {
        int[] sum = new int[numberAnts];
        Integer bestDistance = Integer.MAX_VALUE;
        Integer bestAnt = -1;
        int numberCities = distanceMatrix.getNumberOfCities();
        // this loop determines the shortest distance of all ants
        for(int k=0; k<numberAnts; k++) {
            sum[k] = 0;
            for(int i=0; i<numberCities; i++){
                sum[k] += distanceMatrix.getValue(tour[k].get(i),tour[k].get((i+1)%numberCities));
            }
            if (sum[k]<bestDistance) {
                bestAnt = k;
                bestDistance = sum[k];
            }
        }
        // adds shortest distance of current iteration to list
//        shortestDistances.add(bestDistance);
        // if current iteration has shortest distance
        if (bestDistance < bestIterationDistance) {
            bestIterationDistance = bestDistance;
            bestIterationTour = new ArrayList<>(tour[bestAnt]);
            // adds shortest distance of current iteration to list
            shortestDistances.add(new Tuple(currentIteration,bestDistance,new ArrayList<>(tour[bestAnt])));
        } else {
            shortestDistances.add(new Tuple(currentIteration,bestIterationDistance,bestIterationTour));
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
        for(int i=0; i<numberCities; i++) {
            Integer preCity = tour[bestAnt].get(i);
            Integer sucCity = tour[bestAnt].get((i+1)%numberCities);
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
            Double result = Math.pow(1.0/distance,beta);
            pheromon *= result;
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
            Double probability = 1.0 / distance;
            probability = Math.pow(probability,beta);
            probability *= pheromon;
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

    private static void writeValues(List<Double> averageValue, String label) {
        try{
            String path;
            switch (label){
                case "distance":
                    path = System.getProperty("user.dir") + "/logs/avgDist.csv";
                    break;
                case "position":
                    path = System.getProperty("user.dir") + "/logs/emergence/position.csv";
                    break;
                case "route":
                    path = System.getProperty("user.dir") + "/logs/emergence/route.csv";
                    break;
                case "pheromone":
                    path = System.getProperty("user.dir") + "/logs/emergence/pheromone.csv";
                    break;
//                case "position-absolute":
//                    path = System.getProperty("user.dir") + "/logs/absolute/position.csv";
//                    break;
//                case "route-absolute":
//                    path = System.getProperty("user.dir") + "/logs/absolute/route.csv";
//                    break;
//                case "pheromone-absolute":
//                    path = System.getProperty("user.dir") + "/logs/absolute/pheromone.csv";
//                    break;
                default:
                    throw new IOException();
            }
//            String distPath = System.getProperty("user.dir") + "/logs/avgDist.csv";
            File file = new File(path);
            FileWriter writer = new FileWriter(file);
            int i=1;
//            CSVWriter csvWriter =
            writer.write("iteration" + "," + "value" + "\n");
            for(Double d: averageValue){
                writer.write(String.valueOf(i++) + "," + String.valueOf(d) + "\n");
            }
            writer.close();
        } catch (IOException e){
            System.out.println("Error: writing average distances/emergence");
        }
    }

    public static void writeKiviatCSV(Double position, Double route, Double pheromone){
        try{
            File file = new File(System.getProperty("user.dir") + "/logs/kiviat.csv");
            FileWriter writer = new FileWriter(file);
            writer.write("Attribute" + "," + "Value" + "\n");
            writer.write("Position" + "," + position + "\n");
            writer.write("Route" + "," + route + "\n");
            writer.write("Pheromone" + "," + pheromone + "\n");
            writer.close();
        }catch (IOException e){
            System.out.println("Error: writing kiviat csv");
        }
    }

    @Override
    public int compare(T[] arr1, T[] arr2) {
        if(arr1.length <= arr2.length){
            if (arr1.length == arr2.length) {
                for (int i=0; i<arr1.length; i++) {
                    if(arr1[i].compareTo(arr2[i]) < 0) {
                        return -1;
                    } if(arr1[i].compareTo(arr2[i]) == 0) {
                        continue;
                    } else {
                        return 1;
                    }
                }
                return 0;
            } else {
                return -1;
            }
        } else {
            return 1;
        }
    }
}
