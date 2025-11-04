import java.util.*;

public class prob {

    public static boolean isZeroDistribution(double[][] vec, int nBins) 
    {
        Object[] hist = rhist.rhist(vec, nBins);
        double[] nn = (double[]) hist[0];
        for (double v : nn) 
        {
            if (v == 0.0) 
            {
                return true;
            }
        }
        return false;
    }

    public static Object[] prob(double[][] y, Integer... args) 
    {
        int maxBins = (args.length == 1) ? args[0] : 10;

        int preBin = 0;
        boolean isNotZeroBin = false;
        int iter = 0;
        int cBin = maxBins;
        int zeroBin = 0;
        int nonZeroBin = 0;

        while (preBin != cBin) 
        {
            boolean zeroDistribution = isZeroDistribution(y, cBin);
            iter++;
            if (!zeroDistribution) 
            {
                if (iter == 1) 
                {
                    break;
                }
                int tmpBin = cBin;
                nonZeroBin = cBin;
                cBin = (zeroBin + nonZeroBin) / 2;
                preBin = tmpBin;
                isNotZeroBin = true;
            } 
            else 
            {
                if (!isNotZeroBin) 
                {
                    preBin = cBin;
                    zeroBin = cBin;
                    cBin = cBin / 2;
                } 
                else 
                {
                    int tmpBin = cBin;
                    zeroBin = cBin;
                    cBin = (zeroBin + nonZeroBin) / 2;
                    preBin = tmpBin;
                }
            }
        }

        int nBins = cBin;
        Object[] py = rhist.rhist(y, nBins);
        return new Object[]{py[0], nBins};
    }

    public static void main(String[] args) 
    {
      double[][] y = {{1, 0, 0, 1, 1, 0, 0, 1, 1, 1}};
      Object[] result = prob(y, 3);
      double[] nn = (double[]) result[0];
      int nBins = (int) result[1];

      System.out.println("nn: " + Arrays.toString(nn));
      System.out.println("nBins: " + nBins);
    }
}
