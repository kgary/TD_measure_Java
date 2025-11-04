package entropy;
import java.util.*;

public class GeneralizedEntropyCalculator {

    public static double[] computeWindowedEntropy(String[][][] data, int windowSize, AggregationStrategy strategy) {
        
        int totalTime = data.length;
        double[] entropyValues = new double[totalTime];

        for (int i = 0; i < windowSize - 1; i++) {
            entropyValues[i] = 0.0;
        }

        for (int i = windowSize - 1; i < totalTime; i++) {
            Map<String, Integer> stateCombinations = new HashMap<>();

            for (int t = i - windowSize + 1; t <= i; t++) {
                List<String> stateKeys = strategy.generateStateKeys(data[t]);

                for (String key: stateKeys) {
                    stateCombinations.put(key, stateCombinations.getOrDefault(key, 0) + 1);
                }
            }
            double entropy = calculateEntropy(stateCombinations, windowSize * strategy.getKeysPerTimePoint());
            entropyValues[i] = Math.round(entropy* 10000.0) / 10000.0;  
            
        }

        return entropyValues;
    }

    private static double calculateEntropy(Map<String, Integer> stateCounts, int totalCount) {
        double entropy = 0.0;
        for (int count: stateCounts.values()) {
            if (count > 0) {
                double probability = (double) count / totalCount;
                entropy += probability * (Math.log(probability) / Math.log(2));
            }
        }
        return -entropy;
    }
}
