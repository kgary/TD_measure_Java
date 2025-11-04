package teamDynamics;

public class prob {

    public static boolean isZeroDistribution(double[][] vec, int nBins) 
    {
        Object[] hist = rhist.rhist(vec, nBins);
        double[] nn = (double[]) hist[0];
        int z = 0;
        for (int i = 0; i < nn.length; i++) {
            if (nn[i] == 0.0) {
                z++;
            }
        }
        if(z > 0) {
            return true;
        } 
        return false;
    }

    public static Object[] prob(double[][] y, Integer... args) 
    {
        try {
            if (args.length < 0 || args.length > 1) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            if (args.length < 0) {
                System.err.println("Insufficient no of arguments");
            } else {
                System.err.println("Too many no of arguments");
            }
            return null;
        }

        // Check if y is array (always true in Java, but keep for symmetry)
        if (y == null) {
            System.err.println("Y should be a vector");
            return null;
        }

        int maxBins;
        if (args.length == 0) {
            maxBins = 10;
        } else {
            maxBins = args[0];
        }

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
                cBin = (int) Math.floor((zeroBin + nonZeroBin) / 2.0);
                preBin = tmpBin;
                isNotZeroBin = true;
            } 
            else 
            {
                if (!isNotZeroBin) 
                {
                    preBin = cBin;
                    zeroBin = cBin;
                    cBin = (int) Math.floor(cBin / 2.0);
                } 
                else 
                {
                    int tmpBin = cBin;
                    zeroBin = cBin;
                    cBin = (int) Math.floor((zeroBin + nonZeroBin) / 2.0);
                    preBin = tmpBin;
                }
            }
        }

        int nBins = cBin;
        Object[] py = rhist.rhist(y, nBins);
        return new Object[]{py[0], nBins};
    }

    // public static void main(String[] args) 
    // {
    //   double[][] y = {{1, 0, 0, 1, 1, 0, 0, 1, 1, 1}};
    //   Object[] result = prob(y, 3);
    //   if (result != null) {
    //         double[] nn = (double[]) result[0];
    //         int nBins = (int) result[1];

    //         System.out.println("nn: " + Arrays.toString(nn));
    //         System.out.println("nBins: " + nBins);
    //     }
    // }
}
