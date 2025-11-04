import java.util.*;

public class windowed_recurrence_forward_window {

    public static double[][][] windowed_recurrence_forward_window(double[][] x, int windowSize) {
       int sizeOfColumn = x[0].length - windowSize;

        double[][] rrSeries = new double[1][sizeOfColumn];
        double[][] detSeries = new double[1][sizeOfColumn];

        for (int i = 0; i < sizeOfColumn; i++) {
            double[][] sequence = new double[1][windowSize];
            for (int j = 0; j < windowSize; j++) {
                sequence[0][j] = x[0][i + j];
            }

            int[][] intSeq = new int[1][windowSize];
            for (int j = 0; j < windowSize; j++) {
                intSeq[0][j] = (int) sequence[0][j];
            }

            double[] result = DiscreteRecurrence.DiscreteRecurrence(intSeq);
            double rr = result[0];
            double det = result[1];

            rrSeries[0][i] = rr == 0 ? 100 : rr;
            //detSeries[0][i] = (det == 0 || Double.isNaN(det)) ? 100 : det;
            //detSeries[0][i] = det;

            detSeries[0][i] = (det == 0.0) ? 100.0 : det;


            //rrSeries[0][i] = rr;
            //detSeries[0][i] = det;

        }

        int outputLength = x[0].length;
        double[][] rrFinal = new double[outputLength][1];
        double[][] detFinal = new double[outputLength][1];

        for (int i = 0; i < outputLength; i++) {
            rrFinal[i][0] = 100;
            detFinal[i][0] = 100;
        }

        for (int i = windowSize; i < outputLength; i++) {
            rrFinal[i][0] = rrSeries[0][i - windowSize];
            detFinal[i][0] = detSeries[0][i - windowSize];
        }

        return new double[][][] { rrFinal, detFinal };
    }

    public static void main(String[] args) {
        double[][] x = {{1, 1, 2, 3, 4, 5, 6, 7, 8, 9}};
        int windowSize = 4;

        double[][][] result = windowed_recurrence_forward_window(x, windowSize);

        System.out.println("RR Series:");
        for (double[] row : result[0]) {
            System.out.println(Arrays.toString(row));
        }

        System.out.println("\nDET Series:");
        for (double[] row : result[1]) {
            System.out.println(Arrays.toString(row));
        }
    }
}
