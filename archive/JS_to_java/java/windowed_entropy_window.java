import java.util.*;

public class windowed_entropy_window {

    public static double[][] windowed_entropy_window(double[][] seq, int window_size) {
        int size = seq.length - window_size;
        double[][] ent_series = new double[size][1];

        for (int i = 0; i < size; i++) {
            double[][] window = Arrays.copyOfRange(seq, i, i + window_size);
            int[][] intWindow = new int[window.length][window[0].length];
            for (int r = 0; r < window.length; r++) {
                for (int c = 0; c < window[0].length; c++) {
                    intWindow[r][c] = (int) window[r][c];
                }
            }
            double[] entropy = Entropy.computeEntropy(intWindow);
            ent_series[i][0] = entropy[0];
        }

        double[][] entropySeries = new double[seq.length][1];

        for (int i = 0; i < window_size; i++) {
            entropySeries[i][0] = 0;
        }

        for (int i = window_size; i < seq.length; i++) {
            entropySeries[i][0] = ent_series[i - window_size][0];
        }

        return entropySeries;
    }

    public static void main(String[] args) {
        double[][] seq = {
            {1}, {1}, {0}, {0}, {0}, {1}, {1}, {1}, {1}, {1}, {1}
        };

        double[][] result = windowed_entropy_window(seq, 4);

        System.out.println("Entropy Series:");
        for (double[] row : result) {
            System.out.println(Arrays.toString(row));
        }
    }
}

