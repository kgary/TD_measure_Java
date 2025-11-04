package teamDynamics;
import java.util.*;

public class ami {

    public static double corrcoef(double[] X, double[] Y, int n) {
        double sum_X = 0, sum_Y = 0, sum_XY = 0;
        double squareSum_X = 0, squareSum_Y = 0;

        for (int i = 0; i < n; i++) {
            sum_X += X[i];
            sum_Y += Y[i];
            sum_XY += X[i] * Y[i];
            squareSum_X += X[i] * X[i];
            squareSum_Y += Y[i] * Y[i];
        }

        return (n * sum_XY - sum_X * sum_Y) /
               (Math.sqrt((n * squareSum_X - sum_X * sum_X) *
                          (n * squareSum_Y - sum_Y * sum_Y)));
    }

    public static double[][] transpose(double[][] matrix) {
        double[][] transposed = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    public static Object[] ami(double[][] xy, int[][] nBins, int nLags) {
        int m = xy.length;
        int n = xy[0].length;

        if (n > m) {
            xy = transpose(xy);
            int temp = n;
            n = m;
            m = temp;
        }

        double[] x = null;
        double[] y = null;


        try 
        {
            if (n > 2) {
                throw new IllegalArgumentException();
            } else if (n == 2) {
                x = new double[m];
                y = new double[m];
                for (int i = 0; i < m; i++) {
                    x[i] = xy[i][0];
                    y[i] = xy[i][1];
                }
            } else if (m == 1 || n == 1) {
                double[][] temp = transpose(xy);
                x = temp[0];
                y = temp[0];
            }
        } catch (IllegalArgumentException e) {
                System.err.println("Invalid data: series should be univariate or bivariate");
                return null;
            }

        int nBinsRowSize = nBins.length;
        int nBinsColSize = nBins[0].length;

        if (nBinsRowSize < nBinsColSize) {
            nBins = transposeInt(nBins);
            nBinsRowSize = nBins.length;
            nBinsColSize = nBins[0].length;
        }

        int xBin, yBin;
        try 
        {
            if (nBinsRowSize > 2 || nBinsColSize > 1) {
                throw new IllegalArgumentException();
            } else if ((nBinsRowSize == 2 && n == 2)) {
                xBin = (int) Math.floor(nBins[0][0]);
                yBin = (int) Math.floor(nBins[1][0]);
            } else if ((nBinsRowSize == 1 && n == 2) || n == 1) {
                xBin = (int) Math.floor(nBins[0][0]);
                yBin = xBin;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid bin size: It should be either vector of 2 elements or scalar");
            return null;
        }

        try 
        {
            if (nLags < 0) {
                System.err.println("Invalid lag: It should be a positive scalar");
                throw new IllegalArgumentException();
            }
            if (nLags > m) {
                System.err.println("Invalid lag: It should not be greater than length of time series data");
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            return null;
        }

        nLags = (int) Math.floor(nLags);

        double[][] amis = new double[1][nLags + 1];
        double[][] corrs = new double[1][nLags + 1];

        for (int i = 0; i <= nLags; i++) {
            double[] xlag = Arrays.copyOfRange(x, 0, x.length - i);
            double[] ylag = Arrays.copyOfRange(y, i, x.length);

            Object[] probX = prob.prob(new double[][]{xlag}, xBin);
            double[] px = (double[]) probX[0];
            int xBinComputed = (int) probX[1];

            Object[] probY = prob.prob(new double[][]{ylag}, yBin);
            double[] py = (double[]) probY[0];
            int yBinComputed = (int) probY[1];

            double[][] ab = new double[xlag.length][2];
            for (int j = 0; j < xlag.length; j++) {
                ab[j][0] = xlag[j];
                ab[j][1] = ylag[j];
            }

            double[][] pxy = probxy.probxy(ab, xBinComputed, yBinComputed);

            double amixy = 0;
            for (int j = 0; j < xBinComputed; j++) {
                for (int k = 0; k < yBinComputed; k++) {
                    if (pxy[j][k] != 0) {
                        amixy += pxy[j][k] * Math.log(pxy[j][k] / (px[j] * py[k])) / Math.log(2);
                        amixy = Math.round(amixy * 10000.0) / 10000.0;
                    }
                }
            }

            amis[0][i] = amixy;
            corrs[0][i] = Math.round(corrcoef(xlag, ylag, xlag.length) * 10000.0) / 10000.0;
        }

        return new Object[]{transpose(amis), transpose(corrs)};
    }

    public static int[][] transposeInt(int[][] matrix) {
        int[][] transposed = new int[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

//     public static void main(String[] args) {
//         double[][] xy ={ };
//         int[][] nBins = {{10}};
//         int nLags = 0;

//         Object[] result = ami(xy, nBins, nLags);

//         if (result != null) {
//             double[][] amis = (double[][]) result[0];
//             double[][] corrs = (double[][]) result[1];

//             System.out.println("AMI:");
//             for (double[] row : amis) System.out.println(Arrays.toString(row));

//             System.out.println("\nCorr:");
//             for (double[] row : corrs) System.out.println(Arrays.toString(row));
//         }
//     }
}
