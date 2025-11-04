import java.util.*;

public class LayeredDynamicsRMSEFunction 
{
  public static double[] LayeredDynamicsRMSEFunction(int[] series, int windowSize, int forecastingDistance, int zeta) 
  {
    int matLength = series.length - forecastingDistance - windowSize / 2;
    double[][] mat = new double[matLength][forecastingDistance];
    double[] RMSE = new double[series.length - windowSize / 2];

    for (int h = 0; h < series.length - windowSize / 2; h++) 
    {
      int currPt = series[h];

      for (int i = windowSize / 2; i < series.length - (forecastingDistance + 1); i++) 
      {
        if (!(series[i] < (currPt - zeta) || series[i] > (currPt + zeta))) 
        {
            for (int j = 0; j < forecastingDistance; j++) 
            {
                if ((i + j) < series.length) 
                {
                    mat[i][j] = series[i + j];
                }
            }
        }

        if (i == series.length - (forecastingDistance + 1) - 1) 
        {
          double[] matMean = new double[forecastingDistance];
          for (int j = 0; j < forecastingDistance; j++) 
          {
            List<Double> matcol = new ArrayList<>();
            for (int k = 0; k < matLength; k++) 
            {
                if (mat[k][j] != 0) 
                {
                    matcol.add(mat[k][j]);
                }
            }
            double sum = 0;
            for (double v : matcol) 
            {
                sum += v;
            }
            matMean[j] = matcol.size() > 0 ? sum / matcol.size() : 0;
          }

          double[] currN = new double[forecastingDistance];
          for (int k = 0; k < forecastingDistance; k++) {
              if ((h + k) < series.length) {
                  currN[k] = series[h + k];
              }
          }

          double sum = 0;
          for (int k = 0; k < forecastingDistance; k++) {
              sum += Math.sqrt(Math.pow((currN[k] - matMean[k]), 2));
          }
          RMSE[h] = Math.round((sum / forecastingDistance) * 10000.0) / 10000.0;
        }
      }
    }
    return RMSE;
  }

  public static void main(String[] args) 
  {
    int[] series = {1, 2, 3, 4, 5, 6, 3, 4, 5};
    double[] result = LayeredDynamicsRMSEFunction(series, 2, 2, 2);
    System.out.println(Arrays.toString(result));
  }
}
