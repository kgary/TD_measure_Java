import java.util.*;

public class Entropy {

    public static double[] computeEntropy(int[][] X) {
        int rowSize = X.length;
        int columnSize = X[0].length;
        double[] H = new double[columnSize];

        for (int col = 0; col < columnSize; col++) {
            Map<Integer, Integer> freq = new HashMap<>();
            for (int[] row : X) {
                int val = row[col];
                freq.put(val, freq.getOrDefault(val, 0) + 1);
            }

            double sum = 0.0;
            for (int count : freq.values()) {
                double p = (double) count / rowSize;
                sum += p * (Math.log(p) / Math.log(2));
            }

            H[col] = sum == 0.0 ? 0.0 : -Math.round(sum * 10000) / 10000.0;
        }

        return H;
    }

    public static void main(String[] args) {
        int[][] X = {{1, 1},{1, 2},{1, 1},{1, 2},{1, 1},{1, 2},{1, 1},{1, 2},{1, 1},{1, 1}};
        double[] result = computeEntropy(X);
        System.out.print("Entropy: ");
        for (double h : result) {
            System.out.print(h + ",");
        }
        System.out.println();
    }
}
