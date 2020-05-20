package generator;

import java.util.Random;

public class Matrix<T> {
    private Object [] [] matrixValues;
    private int numberOfCities;
    private static Random generator;

    public void setMatrixElement(int row, int col, T value) {
        this.matrixValues[row][col] = value;
    }

    public Matrix(int numberOfCities) {
        this.matrixValues = new Object[numberOfCities][numberOfCities];
        this.numberOfCities = numberOfCities;
    }

    public int getNumberOfCities() {
        return numberOfCities;
    }

    public T getValue(int row, int col) {
        return (T)matrixValues[row][col];
    }

    public static void initSeed(long seed) {
        Matrix.generator =new Random(seed);
    }

    public void matrixInit() {
        int col;
        for(int row = 0; row<this.numberOfCities; row++) {
            for(col=row+1; col<this.numberOfCities; col++) {
                Integer value = generator.nextInt(1000)+1;
                this.setMatrixElement(row, col, (T) value);
                this.setMatrixElement(col, row, (T) value);
            }
        }
    }

    public void printMatrix() {
        for(int i=0; i<numberOfCities; i++) {
            if(i==0) {
                int k = 0;
                // print column index
                while(k<this.getNumberOfCities()) {
                    System.out.print("\t" + k++);
                }
                System.out.println();
            }
            for(int j=0; j<numberOfCities; j++) {
                if(j==0) {
                    //print row index
                    System.out.print(i + "\t");
                }
                if (matrixValues[i][j] != null) {
                    System.out.print(matrixValues[i][j] + "\t");
                } else {
                    System.out.print("na\t");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
