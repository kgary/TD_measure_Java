import java.util.*;

public class DiscreteRecurrence {

    public static double[] DiscreteRecurrence(int[][] sequence) {
        int rowSize = sequence.length;
        int columnSize = sequence[0].length;

        // Transpose if more columns than rows
        if (columnSize > rowSize) {
            int[][] transposed = new int[columnSize][rowSize];
            for (int i = 0; i < columnSize; i++) {
                for (int j = 0; j < rowSize; j++) {
                    transposed[i][j] = sequence[j][i];
                }
            }
            sequence = transposed;
            rowSize = sequence.length;
            columnSize = sequence[0].length;
        }

        int[][] RecurMatrix = new int[rowSize][rowSize];
        for (int i = 0; i < rowSize; i++) {
            for (int j = 0; j < rowSize; j++) {
                if (sequence[i][0] == sequence[j][0]) {
                    RecurMatrix[i][j] = 1;
                }
            }
        }

        int RecurPoints = 0;
        for (int i = 0; i < rowSize; i++) {
            int columnSum = 0;
            for (int j = 0; j < rowSize; j++) {
                columnSum += RecurMatrix[j][i];
            }
            RecurPoints += columnSum - 1;
        }
        RecurPoints /= 2;

        int[][] upperTriangleMatrix = new int[rowSize][rowSize];
        for (int i = 0; i < rowSize - 1; i++) {
            for (int j = i + 1; j < rowSize; j++) {
                upperTriangleMatrix[i][j] = RecurMatrix[i][j];
            }
        }

        int[][] B = sparseDiagonals(upperTriangleMatrix);
        int r_diags = B.length;
        int c_diags = (r_diags > 0) ? B[0].length : 0;

        int DiagPoints = 0;
        for (int j = 0; j < c_diags; j++) {
            for (int i = 0; i < r_diags - 1; i++) {
                if (B[i][j] == 1 && B[i + 1][j] == 1) {
                    DiagPoints += 2;
                    if (i - 1 >= 0 && B[i - 1][j] == 1) {
                        DiagPoints -= 1;
                    }
                }
            }
        }

        double RR = Math.round((RecurPoints / ((rowSize * columnSize) / 2.0)) * 10000.0) / 10000.0;
        //double DET = (RecurPoints != 0) ? Math.round(((double) DiagPoints / RecurPoints) * 100 * 10000.0) / 10000.0 : 0.0;
        //double DET = Math.round(((double) DiagPoints / RecurPoints) * 100 * 10000.0) / 10000.0 ;
        double DET = (RecurPoints == 0) ? Double.NaN : Math.round(((double) DiagPoints / RecurPoints) * 100 * 10000.0) / 10000.0;


        return new double[]{RR, DET};
    }

    public static int[][] sparseDiagonals(int[][] grid) {
        int nrows = grid.length;
        int ncols = grid[0].length;
        int n = Math.min(nrows, ncols);

        List<int[]> diagonalsList = new ArrayList<>();
        int row = 0;
        int col = 1;

        for (int k = 0; k < ncols - 1; k++) {
            int[] diagonal = new int[n];
            int count = 0;

            for (int i = 0; i < n; i++) {
                int currentRow = row + i;
                int currentCol = col + i;
                if (currentRow < nrows && currentCol < ncols) {
                    diagonal[i] = grid[currentRow][currentCol];
                    if (diagonal[i] != 0) count++;
                }
            }

            if (count != 0) diagonalsList.add(diagonal);
            col++;
        }

        if (diagonalsList.isEmpty()) return new int[0][0];

        int[][] diagonals = diagonalsList.toArray(new int[0][]);
        int rows = diagonals[0].length;
        int cols = diagonals.length;
        int[][] spdiags = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                spdiags[i][j] = diagonals[j][i];
            }
        }

        return spdiags;
    }

    public static void main(String[] args) {
        int[][] sequence = {{1, 1, 1, 1, 0, 0, 1}};
        double[] result = DiscreteRecurrence(sequence);
        System.out.println("RR: " + result[0]);
        System.out.println("DET: " + result[1]);
    }
}
