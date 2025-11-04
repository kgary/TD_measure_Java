package teamDynamics;
import java.util.*;

public class rhist {

    public static Object[] rhist(double[][] y, Integer... args) {
        // Argument validation
        try {
            if (args.length < 0) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Insufficient no of arguments");
            return null;
        }

        int nargs = args.length;
        int x ;
        if (nargs == 0) {
            x = 10;
        } else {
            x = args[0];
        }
        boolean normalizeByWidth = nargs == 2;

        // Flatten y
        List<Double> YList = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < y.length; i++) {
            for (int j = 0; j < y[0].length; j++) {
                YList.add(y[i][j]);
                count++;
            }
        }
        double[] Y = YList.stream().mapToDouble(Double::doubleValue).toArray();
        int m = Y.length;

        double[] nn = new double[x + 1];
        double[] centers;
        double[] bins = new double[x + 2];

        // Compute bin edges
        double min = Arrays.stream(Y).min().orElse(0);
        double max = Arrays.stream(Y).max().orElse(0);
        double binWidth = (max - min) / x;

        bins[0] = Double.NEGATIVE_INFINITY;
        for (int i = 0; i <= x; i++) {
            bins[i + 1] = min + binWidth * i;
        }

        // Count frequencies
        for (int i = 0; i < bins.length - 1; i++) {
            for (int j = 0; j < Y.length; j++) {
                if (Y[j] > bins[i] && Y[j] <= bins[i + 1]) {
                    nn[i] = nn[i] + 1;
                }
            }
        }

        // Compute centers
        centers = new double[x];
        for (int i = 1; i < bins.length - 1; i++) {
            centers[i - 1] = (bins[i] + bins[i + 1]) / 2.0;
            centers[i - 1] = Math.round(centers[i - 1] * 10000.0) / 10000.0;
        }

        // Merge first two bins
        nn[1] += nn[0];
        nn = Arrays.copyOfRange(nn, 1, nn.length);

        // Normalize by count
        for (int i = 0; i < nn.length; i++) {
            nn[i] = nn[i] / m;
            nn[i] = Math.round(nn[i] * 10000.0) / 10000.0;
        }

        // Optional: normalize by bin width
        if (nargs == 2) {
            for (int i = 0; i < nn.length; i++) {
                nn[i] = nn[i] / binWidth;
                nn[i] = Math.round(nn[i] * 10000.0) / 10000.0;
            }
        }

        

        return new Object[]{nn, centers};
    }

    public static void printResult(String label, Object[] result) {
        System.out.println("\n=== " + label + " ===");
        System.out.println("nn: " + Arrays.toString((double[]) result[0]));
        System.out.println("centers: " + Arrays.toString((double[]) result[1]));
    }

    // public static void main(String[] args) {
    //     //testing prob.js
    //     //printResult("prob.js", rhist(new double[][]{{1,0,0,1,1,0,0,1,1,1}}, 3));

    //     // Base case
    //     printResult("Base case", rhist(new double[][]{{1, 2, 3}, {4, 5, 6}}, 2));

    //     // Single bin
    //     printResult("Single bin", rhist(new double[][]{{1, 2, 3, 4, 5, 6}}, 1));

    //     // More bins than values
    //     printResult("More bins than values", rhist(new double[][]{{1, 1, 2, 2, 3, 3}}, 10));

    //     // All same values
    //     printResult("All same values", rhist(new double[][]{{5, 5}, {5, 5}}, 4));

    //     // Negative values
    //     printResult("Negative values", rhist(new double[][]{{-3, -2}, {0, 1, 2}}, 3));

    //     // Edge-aligned values
    //     printResult("Edge-aligned values", rhist(new double[][]{{1, 2, 3, 4}}, 3));

    //     // Density normalization
    //     printResult("Density normalization", rhist(new double[][]{{1, 2, 3}, {4, 5, 6}}, 2, 1));

    //     // Empty input
    //     printResult("Empty input", rhist(new double[][]{{}}, 10));

    //     // Invalid input (1D-like)
    //     printResult("Invalid input (1D)", rhist(new double[][]{{1}, {2}, {3}}, 10));
    // }
}
