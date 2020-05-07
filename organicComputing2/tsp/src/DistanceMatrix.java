import java.util.Random;

public class DistanceMatrix {
    private int [] [] matrix;
    private int numberOfCities;

    private void setMatrixElement(int row, int col, int value) {
        this.matrix[row][col] = value;
    }

    public DistanceMatrix(int numberOfCities) {
        this.matrix = new int[numberOfCities][numberOfCities];
        this.numberOfCities = numberOfCities;
    }

    public int getNumberOfCities() {
        return numberOfCities;
    }

    public int getDistance(int row, int col) {
        return matrix[row][col];
    }

    public void matrixInit(long seed) {
        Random distance = new Random(seed);
        int col;
        for(int row = 0; row<this.numberOfCities; row++) {
            for(col=row+1; col<this.numberOfCities; col++) {
                int value = distance.nextInt(1000)+1;
                this.setMatrixElement(row, col, value);
                this.setMatrixElement(col, row, value);
            }
        }
    }

    public void printMatrix() {
        for(int i=0; i<numberOfCities; i++) {
            for(int j=0; j<numberOfCities; j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }

    }
}
