import java.util.*;

public class probxy {

    public static double[] computeEdge(double[] x, int nBins) 
    {
        double minX = Arrays.stream(x).min().orElse(Double.NaN);
        double maxX = Arrays.stream(x).max().orElse(Double.NaN);
        double binwidth = (maxX - minX) / nBins;
        double[] edge = new double[nBins + 1];
        for (int i = 1; i < nBins; i++) 
        {
            edge[i] = (minX + binwidth * i) * 10000 / 10000;
        }
        edge[0] = Double.NEGATIVE_INFINITY;
        edge[nBins] = Double.POSITIVE_INFINITY;
        return edge;
    }

    public static double[][] transpose(double[][] matrix) 
    {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++) 
        {
            for (int j = 0; j < cols; j++) 
            {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    public static double[][] probxy(double[][] xy, Object... args) 
    {
        int m = xy.length;
        int n = xy[0].length;
        if (n > m) 
        {
            xy = transpose(xy);
            m = xy.length;
            n = xy[0].length;
        }

        if (n != 2) 
        {
            System.err.println("Invalid data size: XY should be two column vector");
            return new double[0][0];
        }

        double[] X = new double[m];
        double[] Y = new double[m];
        for (int i = 0; i < m; i++) 
        {
            X[i] = xy[i][0];
            Y[i] = xy[i][1];
        }

        Object nBinsX = args.length > 0 ? args[0] : 10;
        Object nBinsY = args.length > 1 ? args[1] : 10;

        double[] edgeX;
        if (nBinsX instanceof Integer) 
        {
            edgeX = computeEdge(X, (Integer) nBinsX);
            nBinsX = ((Integer) nBinsX);
        } 
        else 
        {
            double[][] binX = (double[][]) nBinsX;
            if (binX.length == 1 && binX[0].length == 1 && binX[0][0] > 0) 
            {
                edgeX = computeEdge(X, (int) binX[0][0]);
                nBinsX = (int) binX[0][0];
            } 
            else 
            {
                List<Double> flat = new ArrayList<>();
                for (int i = 0; i < binX[0].length; i++) 
                {
                    for (int j = 0; j < binX.length; j++) 
                    {
                        flat.add(binX[j][i]);
                    }
                }
                edgeX = flat.stream().mapToDouble(Double::doubleValue).toArray();
                nBinsX = edgeX.length - 1;
            }
        }

        double[] edgeY;
        if (nBinsY instanceof Integer) 
        {
            edgeY = computeEdge(Y, (Integer) nBinsY);
            nBinsY = ((Integer) nBinsY);
        } 
        else 
        {
            double[][] binY = (double[][]) nBinsY;
            if (binY.length == 1 && binY[0].length == 1 && binY[0][0] > 0) 
            {
                edgeY = computeEdge(Y, (int) binY[0][0]);
                nBinsY = (int) binY[0][0];
            } 
            else 
            {
                List<Double> flat = new ArrayList<>();
                for (int i = 0; i < binY[0].length; i++) 
                {
                    for (int j = 0; j < binY.length; j++) 
                    {
                        flat.add(binY[j][i]);
                    }
                }
                edgeY = flat.stream().mapToDouble(Double::doubleValue).toArray();
                nBinsY = edgeY.length - 1;
            }
        }

        int xBins = (Integer) nBinsX;
        int yBins = (Integer) nBinsY;
        int[][] nn = new int[xBins][yBins];

        for (int i = 0; i < xBins; i++) 
        {
            List<Double> yFound = new ArrayList<>();
            for (int j = 0; j < X.length; j++) 
            {
                if (X[j] >= edgeX[i] && X[j] < edgeX[i + 1]) 
                {
                    yFound.add(Y[j]);
                }
            }

            int[] yBinCounts = new int[edgeY.length];
            for (double yVal : yFound) 
            {
                for (int p = 0; p < edgeY.length - 1; p++) 
                {
                    if (yVal >= edgeY[p] && yVal < edgeY[p + 1]) 
                    {
                        yBinCounts[p]++;
                    }
                }
            }

            yBinCounts[yBinCounts.length - 2] += yBinCounts[yBinCounts.length - 1];
            yBinCounts = Arrays.copyOf(yBinCounts, yBinCounts.length - 1);

            for (int j = 0; j < yBins; j++) 
            {
                nn[i][j] = yBinCounts[j];
            }
        }

        double[][] pxy = new double[xBins][yBins];
        for (int i = 0; i < xBins; i++) 
        {
            for (int j = 0; j < yBins; j++) 
            {
                pxy[i][j] = Math.round((double) nn[i][j] / X.length * 10000.0) / 10000.0;
            }
        }
        return pxy;
    }

    public static void main(String[] args) 
    {
        double[][] input1 = {{1, 2}, {1, 2}, {1, 2}};
        double[][] input2 = {{2, 4}, {3, 5}, {5, 6}};
        double[][] customBinsX = {{1, 2, 3}};
        double[][] customBinsY = {{4, 5, 6}};

        System.out.println("Test 1:");
        double[][] out1 = probxy(input1, 2, 3);
        for (double[] row : out1) System.out.println(Arrays.toString(row));

        System.out.println("\nTest 2:");
        double[][] out2 = probxy(input2, customBinsX, customBinsY);
        for (double[] row : out2) System.out.println(Arrays.toString(row));
    }
}