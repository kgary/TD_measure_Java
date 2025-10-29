package teamDynamics;

import java.util.*;

public class EntropyProcessor {

    public static Map<String, Integer[]> processEntropyData(double[][] entropyData) {
        Map<String, Integer[]> results = new LinkedHashMap<>();

        if (entropyData == null || entropyData.length == 0) {
            System.out.println("Processing entropy data for 0 time points. Returning empty results.");
            return results;
        }

        try {
            double[] trainee1Entropy = new double[entropyData.length];
            double[] trainee2Entropy = new double[entropyData.length];
            double[] trainee3Entropy = new double[entropyData.length];
            double[] teamEntropy = new double[entropyData.length];
            
            for (int i = 0; i < entropyData.length; i++) {
                trainee1Entropy[i] = entropyData[i][0];
                trainee2Entropy[i] = entropyData[i][1];
                trainee3Entropy[i] = entropyData[i][2];
                teamEntropy[i] = entropyData[i][3];
            }
            int time1 = 0;
            int time2 = trainee1Entropy.length - 1;
            
            System.out.println("Processing entropy data for " + entropyData.length + " time points...\n");
            
            Integer[] trainee1Results = LayeredDynamicRelaxationTimes.LayeredDynamicsRelaxationTimes(time1, time2, trainee1Entropy);
            Integer[] trainee2Results = LayeredDynamicRelaxationTimes.LayeredDynamicsRelaxationTimes(time1, time2, trainee2Entropy);
            Integer[] trainee3Results = LayeredDynamicRelaxationTimes.LayeredDynamicsRelaxationTimes(time1, time2, trainee3Entropy);
            Integer[] teamResults = LayeredDynamicRelaxationTimes.LayeredDynamicsRelaxationTimes(time1, time2, teamEntropy);
            
            results.put("trainee1", trainee1Results);
            results.put("trainee2", trainee2Results);
            results.put("trainee3", trainee3Results);
            results.put("team", teamResults);

            System.out.println("trainee1: " + formatResult(trainee1Results));
            System.out.println("trainee2: " + formatResult(trainee2Results));
            System.out.println("trainee3: " + formatResult(trainee3Results));
            System.out.println("team: " + formatResult(teamResults));
            
            return results;
            
        } catch (Exception e) {
            System.err.println("Error processing entropy data: " + e.getMessage());
            e.printStackTrace();
            return results; 
        }
    }
    
    private static String formatResult(Integer[] result) {
        if (result == null) {
            return "Enaction=null, Adaptation=null, Recovery=null";
        }
        return "Enaction=" + result[0] + ", Adaptation=" + result[1] + ", Recovery=" + result[2];
    }
}